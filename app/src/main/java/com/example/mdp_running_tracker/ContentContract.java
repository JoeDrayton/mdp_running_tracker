package com.example.mdp_running_tracker;

import android.net.Uri;

public class ContentContract {
    // Different URI/field name strings for consistency with the content provider
    public static final String AUTHORITY = "com.example.mdp_running_tracker.MyContentProvider";

    public static final Uri RUNS_URI = Uri.parse("content://"+AUTHORITY+"/runs");
    public static final Uri COORDINATES_URI = Uri.parse("content://"+AUTHORITY+"/coordinates");
    public static final Uri RUN_COORDINATES = Uri.parse("content://"+AUTHORITY+"/run_coordinates");
    public static final Uri RUN_COORDINATES_SPECIAL = Uri.parse("content://"+AUTHORITY+"/run_coordinates_special");

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String RATING = "rating";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String RUN_ID = "run_id";
    public static final String COORDINATE_ID = "coordinate_id";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/MyContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/MyContentProvider.data.text";
}
