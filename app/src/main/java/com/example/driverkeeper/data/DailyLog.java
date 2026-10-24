package com.example.driverkeeper.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "daily_logs",
        indices = {@Index(value = {"date"}, unique = true)})
public class DailyLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String date = "";          // yyyy-MM-dd

    public double appRevenue;
    public double tipsAndExtra;
    public double expectedBonus;
    public double fuelExpense;
    public double foodExpense;
    public String otherExpensesJson = "[]";

    private static final Gson GSON = new Gson();

    public List<OtherExpense> parseOtherExpenses() {
        try {
            List<OtherExpense> list = GSON.fromJson(otherExpensesJson,
                    new TypeToken<List<OtherExpense>>() {}.getType());
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void writeOtherExpenses(List<OtherExpense> list) {
        otherExpensesJson = GSON.toJson(list);
    }

    public double sumOtherExpenses() {
        double s = 0;
        for (OtherExpense e : parseOtherExpenses()) s += e.amount;
        return s;
    }

    // ---- Công thức ----
    public double incomeToday()   { return appRevenue + tipsAndExtra; }
    public double incomeActual()  { return appRevenue + tipsAndExtra + expectedBonus; }
    public double totalExpense()  { return fuelExpense + foodExpense + sumOtherExpenses(); }
    public double profitToday()   { return incomeToday() - totalExpense(); }
    public double profitActual()  { return incomeActual() - totalExpense(); }

    public boolean hasData() {
        return incomeActual() > 0 || totalExpense() > 0;
    }
}