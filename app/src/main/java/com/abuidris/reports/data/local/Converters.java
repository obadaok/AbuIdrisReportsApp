package com.abuidris.reports.data.local;

import androidx.room.TypeConverter;
import com.abuidris.reports.data.model.ExpenseItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromExpenseItemList(List<ExpenseItem> items) {
        if (items == null) return "[]";
        JSONArray arr = new JSONArray();
        for (ExpenseItem item : items) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("description", item.description != null ? item.description : "");
                obj.put("amount", item.amount);
            } catch (JSONException ignored) {}
            arr.put(obj);
        }
        return arr.toString();
    }

    @TypeConverter
    public static List<ExpenseItem> toExpenseItemList(String json) {
        List<ExpenseItem> result = new ArrayList<>();
        if (json == null || json.isEmpty()) return result;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ExpenseItem item = new ExpenseItem();
                item.description = obj.optString("description", "");
                item.amount = obj.optLong("amount", 0);
                result.add(item);
            }
        } catch (JSONException ignored) {}
        return result;
    }
}
