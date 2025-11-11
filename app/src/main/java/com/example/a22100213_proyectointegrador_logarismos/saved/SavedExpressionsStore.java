package com.example.a22100213_proyectointegrador_logarismos.saved;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public final class SavedExpressionsStore {
    private static final String PREFS = "saved_exprs_prefs";
    private static final String KEY = "list";
    private static final int MAX = 100;
    private final SharedPreferences sp;

    public SavedExpressionsStore(Context c) {
        sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<SavedExpression> getAll() {
        String raw = sp.getString(KEY, "[]");
        ArrayList<SavedExpression> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(raw);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.add(new SavedExpression(
                        o.optString("id",""),
                        o.optString("expr",""),
                        o.optString("latex",""),
                        o.optLong("ts",0L)
                ));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public void add(String expr, String latex) {
        if (expr == null) expr = "";
        if (latex == null) latex = "";
        String raw = sp.getString(KEY, "[]");
        try {
            JSONArray old = new JSONArray(raw);
            String id = hash(expr.isEmpty() ? latex : expr);
            for (int i = 0; i < old.length(); i++) {
                if (id.equals(old.getJSONObject(i).optString("id"))) return;
            }
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("expr", expr);
            o.put("latex", latex);
            o.put("ts", System.currentTimeMillis());

            JSONArray a = new JSONArray();
            a.put(o);
            for (int i = 0; i < old.length() && i < MAX - 1; i++) a.put(old.get(i));
            sp.edit().putString(KEY, a.toString()).apply();
        } catch (Exception ignored) {}
    }

    public void remove(String id) {
        String raw = sp.getString(KEY, "[]");
        try {
            JSONArray a = new JSONArray(raw);
            JSONArray b = new JSONArray();
            for (int i = 0; i < a.length(); i++) {
                if (!id.equals(a.getJSONObject(i).optString("id"))) b.put(a.get(i));
            }
            sp.edit().putString(KEY, b.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x: d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(s.hashCode());
        }
    }
}
