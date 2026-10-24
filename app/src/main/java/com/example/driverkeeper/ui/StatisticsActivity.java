package com.example.driverkeeper.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.driverkeeper.R;
import com.example.driverkeeper.Utils;
import com.example.driverkeeper.data.AppDatabase;
import com.example.driverkeeper.data.DailyLog;
import com.example.driverkeeper.data.DailyLogDao;
import com.example.driverkeeper.databinding.ActivityStatisticsBinding;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private DailyLogDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        dao = AppDatabase.get(this).dailyLogDao();

        binding.headerExpense.setOnClickListener(v -> {
            boolean open = binding.detailExpense.getVisibility() == View.VISIBLE;
            binding.detailExpense.setVisibility(open ? View.GONE : View.VISIBLE);
        });

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnWeek) loadThisWeek();
            else if (checkedId == R.id.btnMonth) loadThisMonth();
            else if (checkedId == R.id.btnCustom) pickCustomRange();
        });
        binding.toggleGroup.check(R.id.btnWeek);
    }

    private void loadThisWeek() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String start = Utils.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 6);
        load(start, Utils.format(c.getTime()));
    }

    private void loadThisMonth() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        String start = Utils.format(c.getTime());
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        load(start, Utils.format(c.getTime()));
    }

    private void pickCustomRange() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn khoảng ngày")
                        .build();
        picker.addOnPositiveButtonClickListener(sel -> {
            if (sel.first == null || sel.second == null) return;
            // MaterialDatePicker trả về millis theo UTC
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            load(sdf.format(new Date(sel.first)), sdf.format(new Date(sel.second)));
        });
        picker.show(getSupportFragmentManager(), "range");
    }

    private void load(String start, String end) {
        binding.tvRange.setText("Từ " + Utils.displayDate(start) + " đến " + Utils.displayDate(end));
        AppDatabase.IO.execute(() -> {
            List<DailyLog> logs = dao.getBetween(start, end);
            double income = 0, bonus = 0, fuel = 0, food = 0, other = 0;
            for (DailyLog l : logs) {
                income += l.incomeToday();
                bonus += l.expectedBonus;
                fuel += l.fuelExpense;
                food += l.foodExpense;
                other += l.sumOtherExpenses();
            }
            final double fIncome = income, fBonus = bonus, fFuel = fuel, fFood = food, fOther = other;
            runOnUiThread(() -> render(fIncome, fBonus, fFuel, fFood, fOther));
        });
    }

    private void render(double income, double bonus, double fuel, double food, double other) {
        double expense = fuel + food + other;
        double profitNet = income - expense;
        double profitActual = income + bonus - expense;

        binding.tvTotalIncome.setText(Utils.money(income));
        binding.tvTotalActualIncome.setText(Utils.money(income + bonus));
        binding.tvTotalBonus.setText(Utils.money(bonus));
        binding.tvTotalExpense.setText(Utils.money(expense));
        binding.tvFuel.setText(Utils.money(fuel));
        binding.tvFood.setText(Utils.money(food));
        binding.tvOther.setText(Utils.money(other));

        binding.tvProfitNet.setText(Utils.money(profitNet));
        binding.tvProfitNet.setTextColor(Utils.profitColor(binding.tvProfitNet, profitNet));
        binding.tvProfitActual.setText(Utils.money(profitActual));
        binding.tvProfitActual.setTextColor(Utils.profitColor(binding.tvProfitActual, profitActual));
    }
}