package com.example.verifly_android.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.verifly_android.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "verifly_auth.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_VERIFIED = "is_verified";
    public static final String COLUMN_CREATED_AT = "created_at";

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
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_IS_VERIFIED + " INTEGER DEFAULT 0, "
                + COLUMN_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public synchronized boolean insertUser(User user) {
        if (user == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "");
        values.put(COLUMN_PHONE_NUMBER, user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "");
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

    public synchronized boolean updateUser(User user) {
        if (user == null || user.getId() == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "");
        values.put(COLUMN_PHONE_NUMBER, user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "");
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_IS_VERIFIED, user.isVerified() ? 1 : 0);

        int rows = db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{user.getId()});
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
        String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        boolean isVerified = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VERIFIED)) == 1;
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));

        return new User(id, fullName, email, phone, password, isVerified, createdAt);
    }
}
