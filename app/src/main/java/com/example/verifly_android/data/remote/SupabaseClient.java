package com.example.verifly_android.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.verifly_android.BuildConfig;
import com.example.verifly_android.data.db.DatabaseHelper;
import com.example.verifly_android.data.model.ChecklistItem;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    private static final String PREFS_NAME = "verifly_supabase";
    private static final String KEY_SUPABASE_URL = "supabase_url";
    private static final String KEY_SUPABASE_KEY = "supabase_key";
    private static final String KEY_CONN_STRING = "supabase_conn_string";

    public static final String DEFAULT_URL = BuildConfig.SUPABASE_URL;
    public static final String DEFAULT_PUBLISHABLE_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final String DEFAULT_SECRET_KEY = BuildConfig.SUPABASE_SECRET_KEY;
    public static final String DEFAULT_CONN_STRING = BuildConfig.SUPABASE_CONN_STRING;

    public static final String DB_HOST = "db.pektmhxuvaibowiaebnh.supabase.co";
    public static final int DB_PORT = 5432;
    public static final String DB_NAME = "postgres";
    public static final String DB_USER = "postgres";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static SupabaseClient instance;
    private final Context appContext;
    private final OkHttpClient httpClient;
    private final SharedPreferences prefs;
    private String supabaseUrl;
    private String supabaseKey;
    private String connectionString;

    private SupabaseClient(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.httpClient = new OkHttpClient.Builder().build();

        this.supabaseUrl = prefs.getString(KEY_SUPABASE_URL, DEFAULT_URL);
        this.supabaseKey = prefs.getString(KEY_SUPABASE_KEY, DEFAULT_SECRET_KEY);
        this.connectionString = prefs.getString(KEY_CONN_STRING, DEFAULT_CONN_STRING);
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

    public void saveConnectionString(String connStr) {
        if (connStr != null && !connStr.trim().isEmpty()) {
            this.connectionString = connStr.trim();
            prefs.edit().putString(KEY_CONN_STRING, this.connectionString).apply();
        }
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.trim().isEmpty() &&
                supabaseKey != null && !supabaseKey.trim().isEmpty();
    }

    // --- FETCH METHODS FROM SUPABASE CLOUD ---

    public interface FetchCustomersCallback {
        void onResult(boolean success, List<Customer> customers, String message);
    }

    public void fetchCustomers(FetchCustomersCallback callback) {
        if (!isConfigured()) {
            if (callback != null) callback.onResult(false, null, "Supabase cloud not configured");
            return;
        }

        String url = supabaseUrl + "/rest/v1/customers?select=*";
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Accept", "application/json")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (callback != null) callback.onResult(false, null, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String bodyStr = response.body().string();
                        JSONArray array = new JSONArray(bodyStr);
                        List<Customer> fetched = new ArrayList<>();
                        DatabaseHelper db = DatabaseHelper.getInstance(appContext);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String id = obj.optString("id", UUID.randomUUID().toString());
                            String name = obj.optString("name", "");
                            String email = obj.optString("email", "");
                            String phone = obj.optString("phone", "");
                            String address = obj.optString("address", "");
                            String status = obj.optString("status", "Active");
                            String notes = obj.optString("notes", "");
                            long createdAt = parseTimestamp(obj.opt("created_at"));

                            Customer c = new Customer(id, name, email, phone, address, status, notes, createdAt);
                            db.insertCustomer(c);
                            fetched.add(c);
                        }
                        response.close();
                        if (callback != null) callback.onResult(true, fetched, "Fetched " + fetched.size() + " customers from Supabase Cloud");
                    } else {
                        String err = "HTTP " + response.code();
                        response.close();
                        if (callback != null) callback.onResult(false, null, err);
                    }
                } catch (Exception e) {
                    response.close();
                    if (callback != null) callback.onResult(false, null, e.getMessage());
                }
            }
        });
    }

    public interface FetchUsersCallback {
        void onResult(boolean success, List<User> users, String message);
    }

    public void fetchUsers(FetchUsersCallback callback) {
        if (!isConfigured()) {
            if (callback != null) callback.onResult(false, null, "Supabase cloud not configured");
            return;
        }

        String url = supabaseUrl + "/rest/v1/users?select=*";
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Accept", "application/json")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (callback != null) callback.onResult(false, null, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String bodyStr = response.body().string();
                        JSONArray array = new JSONArray(bodyStr);
                        List<User> fetched = new ArrayList<>();
                        DatabaseHelper db = DatabaseHelper.getInstance(appContext);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String id = obj.optString("id", UUID.randomUUID().toString());
                            String fullName = obj.optString("full_name", "");
                            String email = obj.optString("email", "");
                            String phone = obj.optString("phone_number", "");
                            String address = obj.optString("address", "");
                            String password = obj.optString("password", "");
                            boolean isVerified = obj.optBoolean("is_verified", false);
                            long createdAt = parseTimestamp(obj.opt("created_at"));

                            User u = new User(id, fullName, email, phone, address, password, isVerified, createdAt);
                            db.insertUser(u);
                            fetched.add(u);
                        }
                        response.close();
                        if (callback != null) callback.onResult(true, fetched, "Fetched " + fetched.size() + " users from Supabase Cloud");
                    } else {
                        String err = "HTTP " + response.code();
                        response.close();
                        if (callback != null) callback.onResult(false, null, err);
                    }
                } catch (Exception e) {
                    response.close();
                    if (callback != null) callback.onResult(false, null, e.getMessage());
                }
            }
        });
    }

    // --- SYNC METHODS TO SUPABASE CLOUD ---

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

    private long parseTimestamp(Object val) {
        if (val == null) return System.currentTimeMillis();
        if (val instanceof Number) {
            long num = ((Number) val).longValue();
            if (num < 10000000000L) {
                return num * 1000L;
            }
            return num;
        } else if (val instanceof String) {
            String str = (String) val;
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return System.currentTimeMillis();
            }
        }
        return System.currentTimeMillis();
    }

    public interface SimpleCallback {
        void onResult(boolean success, String message);
    }
}
