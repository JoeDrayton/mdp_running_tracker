package com.example.mdp_running_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
        Log.d("g53mdp", "DBHelper made");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("g53mdp", "database initialised");
        db.execSQL("CREATE TABLE runs (" +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(128) NOT NULL, " +
                "description VARCHAR(128) NOT NULL, " +
                "time VARCHAR(128) NOT NULL," +
                "date VARCHAR(128) NOT NULL," +
                "distance FLOAT NOT NULL," +
                "weather VARCHAR(128) NOT NULL," +
                "avgspeed DOUBLE NOT NULL, " +
                "image VARCHAR(128)," +
                "rating INTEGER" +
                "); ");
        db.execSQL("CREATE TABLE coordinates ( " +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "latitude DOUBLE NOT NULL," +
                "longitude DOUBLE NOT NULL" +
                "); ");

        db.execSQL("CREATE TABLE run_coordinates ( " +
                "run_id INT NOT NULL, " +
                "coordinate_id INT NOT NULL, " +
                "CONSTRAINT fk1 FOREIGN KEY (run_id) REFERENCES runs (_id), " +
                "CONSTRAINT fk2 FOREIGN KEY (coordinate_id) REFERENCES coordinates (_id), " +
                "CONSTRAINT _id PRIMARY KEY (run_id, coordinate_id) " +
                "); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS runs");
        db.execSQL("DROP TABLE IF EXISTS coordinates");
        db.execSQL("DROP TABLE IF EXISTS run_coordinates");
        onCreate(db);
    }
}
