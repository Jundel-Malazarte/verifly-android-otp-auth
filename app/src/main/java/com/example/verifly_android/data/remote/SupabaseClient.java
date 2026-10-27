package com.example.verifly_android.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.verifly_android.data.model.ChecklistItem;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    private static final String PREFS_NAME = "verifly_supabase_prefs";
    private static final String KEY_SUPABASE_URL = "supabase_url";
    private static final String KEY_SUPABASE_KEY = "supabase_key";

    private static final String DEFAULT_URL = "https://your-project.supabase.co";
    private static final String DEFAULT_KEY = "your-anon-key";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static SupabaseClient instance;
    private final OkHttpClient httpClient;
    private final SharedPreferences prefs;
    private String supabaseUrl;
    private String supabaseKey;

    private SupabaseClient(Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.httpClient = new OkHttpClient.Builder().build();

        this.supabaseUrl = prefs.getString(KEY_SUPABASE_URL, DEFAULT_URL);
        this.supabaseKey = prefs.getString(KEY_SUPABASE_KEY, DEFAULT_KEY);
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
        }
        return instance;
    }

    public void saveConfig(String url, String key) {
        if (url != null && !url.trim().isEmpty()) {
            this.supabaseUrl = url.trim().replaceAll("/+$", "");
            prefs.edit().putString(KEY_SUPABASE_URL, this.supabaseUrl).apply();
        }
        if (key != null && !key.trim().isEmpty()) {
            this.supabaseKey = key.trim();
            prefs.edit().putString(KEY_SUPABASE_KEY, this.supabaseKey).apply();
        }
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.equals(DEFAULT_URL) &&
                supabaseKey != null && !supabaseKey.equals(DEFAULT_KEY);
    }

    public void syncUser(User user, SimpleCallback callback) {
        if (!isConfigured() || user == null) {
            if (callback != null) callback.onResult(false, "Supabase cloud not configured or null user");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("id", user.getId());
            json.put("full_name", user.getFullName());
            json.put("email", user.getEmail());
            json.put("phone_number", user.getPhoneNumber());
            json.put("address", user.getAddress());
            json.put("is_verified", user.isVerified());
            json.put("created_at", user.getCreatedAt());

            postOrUpsert("users", json.toString(), callback);
        } catch (Exception e) {
            if (callback != null) callback.onResult(false, e.getMessage());
        }
    }

    public void syncCustomer(Customer customer, SimpleCallback callback) {
        if (!isConfigured() || customer == null) {
            if (callback != null) callback.onResult(false, "Supabase cloud not configured or null customer");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("id", customer.getId());
            json.put("name", customer.getName());
            json.put("email", customer.getEmail());
            json.put("phone", customer.getPhone());
            json.put("address", customer.getAddress());
            json.put("status", customer.getStatus());
            json.put("notes", customer.getNotes());
            json.put("created_at", customer.getCreatedAt());

            postOrUpsert("customers", json.toString(), callback);
        } catch (Exception e) {
            if (callback != null) callback.onResult(false, e.getMessage());
        }
    }

    public void syncChecklistItem(ChecklistItem item, SimpleCallback callback) {
        if (!isConfigured() || item == null) {
            if (callback != null) callback.onResult(false, "Supabase cloud not configured or null item");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("id", item.getId());
            json.put("customer_id", item.getCustomerId());
            json.put("title", item.getTitle());
            json.put("category", item.getCategory());
            json.put("is_completed", item.isCompleted());
            json.put("created_at", item.getCreatedAt());

            postOrUpsert("customer_checklists", json.toString(), callback);
        } catch (Exception e) {
            if (callback != null) callback.onResult(false, e.getMessage());
        }
    }

    private void postOrUpsert(String table, String jsonBody, SimpleCallback callback) {
        String url = supabaseUrl + "/rest/v1/" + table;

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Prefer", "resolution=merge-duplicates")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (callback != null) callback.onResult(false, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = response.isSuccessful();
                String message = success ? "Synced successfully" : "HTTP " + response.code();
                response.close();
                if (callback != null) callback.onResult(success, message);
            }
        });
    }

    public interface SimpleCallback {
        void onResult(boolean success, String message);
    }
}
