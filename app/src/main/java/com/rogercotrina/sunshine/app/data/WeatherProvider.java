package com.rogercotrina.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static com.rogercotrina.sunshine.app.data.WeatherContract.*;

/**
 * Content Provider for Weather Data.
 * Created by rogercotrina on 8/3/14.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static UriMatcher uriMatcher = buildUriMatcher();

    private WeatherDbHelper dbHelper;
    private static final SQLiteQueryBuilder weatherByLocationSettingQueryBuilder;

    static {
        weatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        weatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    private static final String locationSettingSelection = LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String locationSettingWithStartDateSelection = LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND "
            + WeatherEntry.COLUMN_DATETEXT + " >= ? ";
    private static final String locationSettingWithDateSelection = LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND "
            + WeatherEntry.COLUMN_DATETEXT + " = ? ";

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER, WEATHER);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_LOCATION, LOCATION);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_LOCATION + "/#", LOCATION_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = locationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = locationSettingWithStartDateSelection;
        }

        return weatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingWithDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (null == date) {
            selection = locationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, date};
            selection = locationSettingWithDateSelection;
        }

        return weatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        final int match = uriMatcher.match(uri);
        switch (match) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                cursor = getWeatherByLocationSettingWithDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                cursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather/"
            case WEATHER: {
                cursor = dbHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                cursor = dbHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "location/"
            case LOCATION: {
                cursor = dbHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            // "weather/"
            case WEATHER: {
                long _id = database.insert(WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            }
            // "location/"
            case LOCATION: {
                long _id = database.insert(LocationEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = LocationEntry.buildLocationUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     *
     * @param uri
     * @param selection - not passing a selection will delete all rows in table.
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsAffected;
        switch (match) {
            // "weather/"
            case WEATHER: {
                rowsAffected = deleteEntry(database, WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // "location/"
            case LOCATION: {
                rowsAffected = deleteEntry(database, LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        if (null == selection || 0 != rowsAffected) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    private int deleteEntry(SQLiteDatabase database, String tableName, String selection, String[] selectionArgs) {
        int rowsAffected = database.delete(tableName, selection, selectionArgs);
        if (rowsAffected < 0) {
            throw new SQLiteException("Failed to delete row in table: " + tableName);
        }
        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsAffected;
        switch (match) {
            // "weather/"
            case WEATHER: {
                rowsAffected = updateEntry(database, values, WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // "location/"
            case LOCATION: {
                rowsAffected = updateEntry(database, values, LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    private int updateEntry(SQLiteDatabase database, ContentValues values, String tableName, String selection, String[] selectionArgs) {
        int rowsAffected = database.update(tableName, values, selection, selectionArgs);
        if (rowsAffected < 0) {
            throw new SQLiteException("Failed to updated row in table: " + tableName);
        }
        return rowsAffected;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER: {
                database.beginTransaction();
                int rowsAffected = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = database.insert(WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsAffected++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return rowsAffected;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }
}
