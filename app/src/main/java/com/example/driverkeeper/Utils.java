package com.example.driverkeeper;

import android.view.View;

import com.google.android.material.color.MaterialColors;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    private Utils() {}

    public static final int GREEN = 0xFF2E7D32;
    public static final int RED = 0xFFC62828;

    public static String format(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d);
    }

    public static String today() { return format(new Date()); }

    /** yyyy-MM-dd -> dd/MM/yyyy */
    public static String displayDate(String iso) {
        String[] p = iso.split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }

    public static String money(double v) {
        DecimalFormat df = new DecimalFormat("#,###",
                new DecimalFormatSymbols(new Locale("vi", "VN")));
        return df.format(Math.round(v)) + " đ";
    }

    public static double parse(CharSequence s) {
        if (s == null || s.length() == 0) return 0;
        try { return Double.parseDouble(s.toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /** 0 -> chuỗi rỗng (hiện hint "0"), số nguyên không có ".0" */
    public static String toInput(double v) {
        if (v == 0) return "";
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    public static int profitColor(View v, double profit) {
        if (profit > 0) return GREEN;
        if (profit < 0) return RED;
        return MaterialColors.getColor(v,
                com.google.android.material.R.attr.colorOnSurface);
    }
}