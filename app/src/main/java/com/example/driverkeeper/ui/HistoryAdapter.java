package com.example.driverkeeper.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.driverkeeper.Utils;
import com.example.driverkeeper.data.DailyLog;
import com.example.driverkeeper.databinding.ItemHistoryBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<DailyLog> items = new ArrayList<>();
    private final Set<Long> expanded = new HashSet<>();

    public void setItems(List<DailyLog> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DailyLog log = items.get(position);
        ItemHistoryBinding b = h.b;

        b.tvDate.setText(Utils.displayDate(log.date));
        double pa = log.profitActual();
        b.tvSummary.setText("Lợi nhuận thực tế: " + Utils.money(pa));
        b.tvSummary.setTextColor(Utils.profitColor(b.tvSummary, pa));

        b.tvIncome.setText("Thu nhập: " + Utils.money(log.incomeToday())
                + "  (App " + Utils.money(log.appRevenue)
                + " + Tips " + Utils.money(log.tipsAndExtra) + ")");
        b.tvBonus.setText("Thưởng dự kiến: " + Utils.money(log.expectedBonus));
        b.tvExpense.setText("Chi tiêu: " + Utils.money(log.totalExpense())
                + "  (Xăng " + Utils.money(log.fuelExpense)
                + ", Ăn " + Utils.money(log.foodExpense)
                + ", Khác " + Utils.money(log.sumOtherExpenses()) + ")");

        double pt = log.profitToday();
        b.tvProfitToday.setText("Lợi nhuận: " + Utils.money(pt));
        b.tvProfitToday.setTextColor(Utils.profitColor(b.tvProfitToday, pt));
        b.tvProfitActual.setText("Lợi nhuận thực tế: " + Utils.money(pa));
        b.tvProfitActual.setTextColor(Utils.profitColor(b.tvProfitActual, pa));

        boolean open = expanded.contains(log.id);
        b.layoutDetail.setVisibility(open ? View.VISIBLE : View.GONE);
        b.ivArrow.setRotation(open ? 180f : 0f);

        b.getRoot().setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            long id = items.get(pos).id;
            if (!expanded.remove(id)) expanded.add(id);
            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemHistoryBinding b;
        VH(ItemHistoryBinding b) { super(b.getRoot()); this.b = b; }
    }
}