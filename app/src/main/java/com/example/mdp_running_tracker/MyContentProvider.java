package com.example.mdp_running_tracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        Log.d("g53mdp", uri.toString() + " " + uriMatcher.match(uri));

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case 2:
                selection = "_ID" + uri.getLastPathSegment();
            case 1:
                Log.d("g53mdp", "runs query");
                return db.query("runs", projection, selection, selectionArgs, null, null, sortOrder);
            case 3:
                Log.d("g53mdp", "runs query");
                return db.query("runs", projection, selection, selectionArgs, null, null, sortOrder);
            case 5:
                Log.d("g53mdp", "run coordinates query");
                return db.rawQuery("select r._id, r.name, rc.coordinate_id, c.latitude, c.longitude "+
                                "from runs r "+
                                "join run_coordinates rc on (r._id = rc.run_id)"+
                                "join coordinates c on (rc.coordinate_id = c._id) where r._id == ?",
                                selectionArgs);
            case 8:
                Log.d("g53mdp", "run coordinates special query");
                return db.rawQuery("select r._id, r.name, rc.coordinate_id, c.latitude, c.longitude "+
                                "from runs r "+
                                "join run_coordinates rc on (r._id = ri.run_id)"+
                                "join coordinates c on (rc.coordinate_id = c._id) where c._id == ?",
                        selectionArgs);

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String contentType;

        if(uri.getLastPathSegment() == null) {
            contentType = ContentContract.CONTENT_TYPE_MULTIPLE;
        } else {
            contentType = ContentContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // using uri put content values into database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            case 1:
                tableName = "runs";
                Log.d("g53mdp", "query uri mathched");
                break;
            case 3:
                tableName = "coordinates";
                break;
            case 5:
                tableName = "run_coordinates";
                break;
            default:
                tableName = "runs";
                break;
        }
            long id = db.insert(tableName, null, values);
            db.close();
            Uri nu = ContentUris.withAppendedId(uri, id);
            Log.d("g53mdp", nu.toString());
            getContext().getContentResolver().notifyChange(nu, null);
            return nu;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            // Deletes from database depending on uri and selection
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int numDeleted = 0;
            switch (uriMatcher.match(uri)){
                case 1:
                    numDeleted = db.delete("runs", selection, selectionArgs);
                    break;
                case 3:
                    numDeleted = db.delete("coordinates", selection, selectionArgs);
                    break;
                case 5:
                    numDeleted = db.delete("run_coordinates", selection, selectionArgs);
                    break;
                default:
                    break;
            }
            return numDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d("g53mdp", uri.toString() + " " + uriMatcher.match(uri));

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch(uriMatcher.match(uri)){
            case 1:
                // updates rating basically, possibly redundant
                return db.update("runs", values, selection, selectionArgs);
            default:
                return 0;
        }
    }
}
