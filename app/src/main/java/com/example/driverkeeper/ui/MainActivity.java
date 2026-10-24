package com.example.driverkeeper.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.driverkeeper.Utils;
import com.example.driverkeeper.data.AppDatabase;
import com.example.driverkeeper.data.DailyLog;
import com.example.driverkeeper.data.DailyLogDao;
import com.example.driverkeeper.data.OtherExpense;
import com.example.driverkeeper.databinding.ActivityMainBinding;
import com.example.driverkeeper.databinding.ItemOtherExpenseBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DailyLogDao dao;
    private DailyLog today;
    private boolean loading = true;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable saveRunnable = this::saveNow;
    private HistoryAdapter historyAdapter;

    private final TextWatcher watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void afterTextChanged(Editable s) { onFormChanged(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        dao = AppDatabase.get(this).dailyLogDao();

        historyAdapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(historyAdapter);

        binding.etRevenue.addTextChangedListener(watcher);
        binding.etTips.addTextChangedListener(watcher);
        binding.etBonus.addTextChangedListener(watcher);
        binding.etFuel.addTextChangedListener(watcher);
        binding.etFood.addTextChangedListener(watcher);

        binding.btnAddExpense.setOnClickListener(v -> {
            addOtherRow("", 0);
            onFormChanged();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (today == null || !today.date.equals(Utils.today())) {
            loadToday();
        } else {
            loadHistory();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!loading) {
            handler.removeCallbacks(saveRunnable);
            saveNow();
        }
    }

    // ---------- Load / tạo bản ghi hôm nay ----------
    private void loadToday() {
        loading = true;
        final String date = Utils.today();
        AppDatabase.IO.execute(() -> {
            DailyLog log = dao.getByDate(date);
            if (log == null) {
                log = new DailyLog();
                log.date = date;
                log.id = dao.insert(log);
            }
            final DailyLog result = log;
            runOnUiThread(() -> {
                today = result;
                bindToForm(result);
                loading = false;
                renderResult();
            });
            loadHistory();
        });
    }

    private void bindToForm(DailyLog log) {
        binding.tvToday.setText("Hôm nay – " + Utils.displayDate(log.date));
        binding.etRevenue.setText(Utils.toInput(log.appRevenue));
        binding.etTips.setText(Utils.toInput(log.tipsAndExtra));
        binding.etBonus.setText(Utils.toInput(log.expectedBonus));
        binding.etFuel.setText(Utils.toInput(log.fuelExpense));
        binding.etFood.setText(Utils.toInput(log.foodExpense));

        binding.containerOther.removeAllViews();
        for (OtherExpense e : log.parseOtherExpenses()) addOtherRow(e.name, e.amount);
    }

    private void addOtherRow(String name, double amount) {
        ItemOtherExpenseBinding row = ItemOtherExpenseBinding.inflate(
                getLayoutInflater(), binding.containerOther, false);
        row.etName.setText(name);
        row.etAmount.setText(Utils.toInput(amount));
        row.etName.addTextChangedListener(watcher);
        row.etAmount.addTextChangedListener(watcher);
        row.btnRemove.setOnClickListener(v -> {
            binding.containerOther.removeView(row.getRoot());
            onFormChanged();
        });
        binding.containerOther.addView(row.getRoot());
    }

    // ---------- Thu thập form, tính toán, tự lưu ----------
    private void onFormChanged() {
        if (loading || today == null) return;
        collect();
        renderResult();
        handler.removeCallbacks(saveRunnable);
        handler.postDelayed(saveRunnable, 400);
    }

    /** Tạo bản ghi mới (cùng id/date) từ giá trị trên form. */
    private void collect() {
        DailyLog log = new DailyLog();
        log.id = today.id;
        log.date = today.date;
        log.appRevenue = Utils.parse(binding.etRevenue.getText());
        log.tipsAndExtra = Utils.parse(binding.etTips.getText());
        log.expectedBonus = Utils.parse(binding.etBonus.getText());
        log.fuelExpense = Utils.parse(binding.etFuel.getText());
        log.foodExpense = Utils.parse(binding.etFood.getText());

        List<OtherExpense> others = new ArrayList<>();
        for (int i = 0; i < binding.containerOther.getChildCount(); i++) {
            ItemOtherExpenseBinding row =
                    ItemOtherExpenseBinding.bind(binding.containerOther.getChildAt(i));
            String name = String.valueOf(row.etName.getText()).trim();
            double amount = Utils.parse(row.etAmount.getText());
            if (!name.isEmpty() || amount != 0) others.add(new OtherExpense(name, amount));
        }
        log.writeOtherExpenses(others);
        today = log;
    }

    private void saveNow() {
        if (today == null) return;
        final DailyLog snapshot = today;
        AppDatabase.IO.execute(() -> dao.update(snapshot));
    }

    private void renderResult() {
        if (today == null) return;
        double pt = today.profitToday();
        double pa = today.profitActual();
        binding.tvProfitToday.setText(Utils.money(pt));
        binding.tvProfitToday.setTextColor(Utils.profitColor(binding.tvProfitToday, pt));
        binding.tvProfitActual.setText(Utils.money(pa));
        binding.tvProfitActual.setTextColor(Utils.profitColor(binding.tvProfitActual, pa));
    }

    // ---------- Lịch sử ----------
    private void loadHistory() {
        final String date = Utils.today();
        AppDatabase.IO.execute(() -> {
            List<DailyLog> all = dao.getBefore(date);
            final List<DailyLog> shown = new ArrayList<>();
            for (DailyLog l : all) if (l.hasData()) shown.add(l);
            runOnUiThread(() -> {
                historyAdapter.setItems(shown);
                binding.tvEmptyHistory.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    // ---------- Menu ----------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.example.driverkeeper.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.example.driverkeeper.R.id.action_stats) {
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}