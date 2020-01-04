package com.example.mdp_running_tracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyContentProvider extends ContentProvider {
    private DBHelper dbHelper = null;

    private static final UriMatcher uriMatcher;

    static {
        // URIs for different tables
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ContentContract.AUTHORITY, "runs", 1);
        uriMatcher.addURI(ContentContract.AUTHORITY, "runs/#",2);
        uriMatcher.addURI(ContentContract.AUTHORITY, "coordinates",3);
        uriMatcher.addURI(ContentContract.AUTHORITY, "coordinates/#",4);
        uriMatcher.addURI(ContentContract.AUTHORITY, "run_coordinates",5);
        uriMatcher.addURI(ContentContract.AUTHORITY, "run_coordinates/#",6);
        uriMatcher.addURI(ContentContract.AUTHORITY, "run_coordinates_special",8);
        uriMatcher.addURI(ContentContract.AUTHORITY, "*",7);
    }
    @Override
    public boolean onCreate() {
        Log.d("g53mdp", "contentProvider onCreate");
        this.dbHelper = new DBHelper(this.getContext(), "mydb", null, 8);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
