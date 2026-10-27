package com.example.verifly_android.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.verifly_android.data.model.ChecklistItem;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "verifly_auth.db";
    private static final int DATABASE_VERSION = 3;

    // Table USERS
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_VERIFIED = "is_verified";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Table CUSTOMERS
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_CUST_ID = "id";
    public static final String COLUMN_CUST_NAME = "name";
    public static final String COLUMN_CUST_EMAIL = "email";
    public static final String COLUMN_CUST_PHONE = "phone";
    public static final String COLUMN_CUST_ADDRESS = "address";
    public static final String COLUMN_CUST_STATUS = "status";
    public static final String COLUMN_CUST_NOTES = "notes";
    public static final String COLUMN_CUST_CREATED_AT = "created_at";

    // Table CHECKLISTS
    public static final String TABLE_CHECKLISTS = "customer_checklists";
    public static final String COLUMN_CHK_ID = "id";
    public static final String COLUMN_CHK_CUST_ID = "customer_id";
    public static final String COLUMN_CHK_TITLE = "title";
    public static final String COLUMN_CHK_CATEGORY = "category";
    public static final String COLUMN_CHK_IS_COMPLETED = "is_completed";
    public static final String COLUMN_CHK_CREATED_AT = "created_at";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " TEXT PRIMARY KEY, "
                + COLUMN_FULL_NAME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PHONE_NUMBER + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_IS_VERIFIED + " INTEGER DEFAULT 0, "
                + COLUMN_CREATED_AT + " INTEGER"
                + ")";

        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + " ("
                + COLUMN_CUST_ID + " TEXT PRIMARY KEY, "
                + COLUMN_CUST_NAME + " TEXT NOT NULL, "
                + COLUMN_CUST_EMAIL + " TEXT, "
                + COLUMN_CUST_PHONE + " TEXT, "
                + COLUMN_CUST_ADDRESS + " TEXT, "
                + COLUMN_CUST_STATUS + " TEXT, "
                + COLUMN_CUST_NOTES + " TEXT, "
                + COLUMN_CUST_CREATED_AT + " INTEGER"
                + ")";

        String CREATE_CHECKLISTS_TABLE = "CREATE TABLE " + TABLE_CHECKLISTS + " ("
                + COLUMN_CHK_ID + " TEXT PRIMARY KEY, "
                + COLUMN_CHK_CUST_ID + " TEXT, "
                + COLUMN_CHK_TITLE + " TEXT NOT NULL, "
                + COLUMN_CHK_CATEGORY + " TEXT, "
                + COLUMN_CHK_IS_COMPLETED + " INTEGER DEFAULT 0, "
                + COLUMN_CHK_CREATED_AT + " INTEGER"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CUSTOMERS_TABLE);
        db.execSQL(CREATE_CHECKLISTS_TABLE);

        seedDefaultChecklists(db);
    }

    private void seedDefaultChecklists(SQLiteDatabase db) {
        String[] defaultTitles = {
                "Identity & Government ID Verification",
                "Mobile Phone OTP Verification",
                "Email Verification & Confirmation",
                "Proof of Address Document",
                "Terms of Service & Consent Signed"
        };
        String[] defaultCategories = {
                "Identity", "Security", "Contact", "Compliance", "Legal"
        };

        for (int i = 0; i < defaultTitles.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CHK_ID, "chk_default_" + (i + 1));
            values.put(COLUMN_CHK_CUST_ID, "");
            values.put(COLUMN_CHK_TITLE, defaultTitles[i]);
            values.put(COLUMN_CHK_CATEGORY, defaultCategories[i]);
            values.put(COLUMN_CHK_IS_COMPLETED, i == 1 ? 1 : 0);
            values.put(COLUMN_CHK_CREATED_AT, System.currentTimeMillis() - (i * 3600000L));
            db.insert(TABLE_CHECKLISTS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_CUSTOMERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOMERS + " ("
                    + COLUMN_CUST_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_CUST_NAME + " TEXT NOT NULL, "
                    + COLUMN_CUST_EMAIL + " TEXT, "
                    + COLUMN_CUST_PHONE + " TEXT, "
                    + COLUMN_CUST_ADDRESS + " TEXT, "
                    + COLUMN_CUST_STATUS + " TEXT, "
                    + COLUMN_CUST_NOTES + " TEXT, "
                    + COLUMN_CUST_CREATED_AT + " INTEGER"
                    + ")";

            String CREATE_CHECKLISTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CHECKLISTS + " ("
                    + COLUMN_CHK_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_CHK_CUST_ID + " TEXT, "
                    + COLUMN_CHK_TITLE + " TEXT NOT NULL, "
                    + COLUMN_CHK_CATEGORY + " TEXT, "
                    + COLUMN_CHK_IS_COMPLETED + " INTEGER DEFAULT 0, "
                    + COLUMN_CHK_CREATED_AT + " INTEGER"
                    + ")";

            db.execSQL(CREATE_CUSTOMERS_TABLE);
            db.execSQL(CREATE_CHECKLISTS_TABLE);
            seedDefaultChecklists(db);
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_CUST_ADDRESS + " TEXT");
            } catch (Exception ignored) {}
        }
    }

    // --- USERS METHODS ---

    public synchronized boolean insertUser(User user) {
        if (user == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "");
        values.put(COLUMN_PHONE_NUMBER, user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "");
        values.put(COLUMN_ADDRESS, user.getAddress() != null ? user.getAddress().trim() : "");
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_IS_VERIFIED, user.isVerified() ? 1 : 0);
        values.put(COLUMN_CREATED_AT, user.getCreatedAt() > 0 ? user.getCreatedAt() : System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public synchronized boolean isEmailRegistered(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String cleaned = email.trim().toLowerCase();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                "LOWER(" + COLUMN_EMAIL + ") = ?",
                new String[]{cleaned}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public synchronized boolean isPhoneRegistered(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String cleaned = phone.trim();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_PHONE_NUMBER + " = ?",
                new String[]{cleaned}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public synchronized boolean isIdentifierRegistered(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return false;
        return isEmailRegistered(identifier) || isPhoneRegistered(identifier);
    }

    @Nullable
    public synchronized User getUserByEmailOrPhone(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String cleaned = identifier.trim().toLowerCase();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "LOWER(" + COLUMN_EMAIL + ") = ? OR " + COLUMN_PHONE_NUMBER + " = ?";
        String[] selectionArgs = new String[]{cleaned, identifier.trim()};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    @Nullable
    public synchronized User getUserById(String id) {
        if (id == null || id.isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + " = ?", new String[]{id}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    public synchronized boolean updateUserVerification(String userId, boolean isVerified) {
        if (userId == null || userId.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_VERIFIED, isVerified ? 1 : 0);

        int rows = db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{userId});
        return rows > 0;
    }

    public synchronized List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, COLUMN_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                userList.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return userList;
    }

    private User cursorToUser(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
        String address = cursor.getColumnIndex(COLUMN_ADDRESS) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)) : "";
        String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        boolean isVerified = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VERIFIED)) == 1;
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));

        return new User(id, fullName, email, phone, address, password, isVerified, createdAt);
    }

    // --- CUSTOMERS METHODS ---

    public synchronized boolean insertCustomer(Customer customer) {
        if (customer == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUST_ID, customer.getId());
        values.put(COLUMN_CUST_NAME, customer.getName());
        values.put(COLUMN_CUST_EMAIL, customer.getEmail());
        values.put(COLUMN_CUST_PHONE, customer.getPhone());
        values.put(COLUMN_CUST_ADDRESS, customer.getAddress());
        values.put(COLUMN_CUST_STATUS, customer.getStatus());
        values.put(COLUMN_CUST_NOTES, customer.getNotes());
        values.put(COLUMN_CUST_CREATED_AT, customer.getCreatedAt());

        long result = db.insertWithOnConflict(TABLE_CUSTOMERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public synchronized List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, null, null, null, null, COLUMN_CUST_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_PHONE));
                String address = cursor.getColumnIndex(COLUMN_CUST_ADDRESS) != -1 ? cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_ADDRESS)) : "";
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_STATUS));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUST_NOTES));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CUST_CREATED_AT));

                list.add(new Customer(id, name, email, phone, address, status, notes, createdAt));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    // --- CHECKLIST METHODS ---

    public synchronized boolean insertChecklistItem(ChecklistItem item) {
        if (item == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHK_ID, item.getId());
        values.put(COLUMN_CHK_CUST_ID, item.getCustomerId() != null ? item.getCustomerId() : "");
        values.put(COLUMN_CHK_TITLE, item.getTitle());
        values.put(COLUMN_CHK_CATEGORY, item.getCategory());
        values.put(COLUMN_CHK_IS_COMPLETED, item.isCompleted() ? 1 : 0);
        values.put(COLUMN_CHK_CREATED_AT, item.getCreatedAt());

        long result = db.insertWithOnConflict(TABLE_CHECKLISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public synchronized boolean updateChecklistItemCompletion(String id, boolean isCompleted) {
        if (id == null || id.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHK_IS_COMPLETED, isCompleted ? 1 : 0);

        int rows = db.update(TABLE_CHECKLISTS, values, COLUMN_CHK_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    public synchronized List<ChecklistItem> getAllChecklistItems() {
        List<ChecklistItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHECKLISTS, null, null, null, null, null, COLUMN_CHK_CREATED_AT + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHK_ID));
                String custId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHK_CUST_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHK_TITLE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHK_CATEGORY));
                boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHK_IS_COMPLETED)) == 1;
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHK_CREATED_AT));

                list.add(new ChecklistItem(id, custId, title, category, completed, createdAt));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return list;
    }
}
