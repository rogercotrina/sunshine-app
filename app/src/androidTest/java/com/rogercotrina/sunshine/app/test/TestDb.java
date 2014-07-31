package com.rogercotrina.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import com.rogercotrina.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

import static com.rogercotrina.sunshine.app.data.WeatherContract.LocationEntry;
import static com.rogercotrina.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * Created by rogercotrina on 7/31/14.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase database = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, database.isOpen());
        database.close();
    }

    public void testInsertReadDb() {

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Create map of values
        ContentValues locationValues = getLocationContentValues();

        long locationRowId = database.insert(LocationEntry.TABLE_NAME, null, locationValues);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor locationCursor = database.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        validateContent(locationCursor, locationValues);


        ContentValues weatherValues = getWeatherContentValues(locationRowId);

        long weatherRowId = database.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);
        Log.d(LOG_TAG, "New row id: " + weatherRowId);

        Cursor weatherCursor = database.query(
                WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        validateContent(weatherCursor, weatherValues);

        dbHelper.close();

    }

    private void validateContent(Cursor cursor, ContentValues contentValues) {
        assertTrue(cursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int columnIndex = cursor.getColumnIndex(columnName);
            assertTrue(columnIndex != -1);

            String actualValue = cursor.getString(columnIndex);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, actualValue);
        }

    }

    private ContentValues getWeatherContentValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }

    private ContentValues getLocationContentValues() {
        ContentValues locationValues = new ContentValues();
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        locationValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        locationValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        locationValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);
        return locationValues;
    }
}
