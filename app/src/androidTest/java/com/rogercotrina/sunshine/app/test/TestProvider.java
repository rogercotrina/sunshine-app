package com.rogercotrina.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;
import com.rogercotrina.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

import static com.rogercotrina.sunshine.app.data.WeatherContract.LocationEntry;
import static com.rogercotrina.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * Test Provider
 * Created by rogercotrina on 7/31/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public static String TEST_COLUMN_DATETEXT = "20141205";
    public static String TEST_LOCATION_SETTING = "99705";

    public void testDeleteDb() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140731";
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);

    }

    public void testInsertReadProvider() {


        // Create map of values
        ContentValues locationValues = getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);

        long locationRowId = ContentUris.parseId(locationUri);

        //assertTrue(locationRowId != -1);
        //Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        validateContent(locationCursor, locationValues);
        locationCursor.close();

        locationCursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null);

        validateContent(locationCursor, locationValues);
        locationCursor.close();

        ContentValues weatherValues = getWeatherContentValues(locationRowId);

        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);

        long weatherRowId = ContentUris.parseId(weatherUri);
        assertTrue(weatherRowId != -1);
        Log.d(LOG_TAG, "New row id: " + weatherRowId);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null,
                null);

        validateContent(weatherCursor, weatherValues);
        weatherCursor.close();

        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION_SETTING, TEST_COLUMN_DATETEXT),
                null,
                null,
                null,
                null,
                null);

        validateContent(weatherCursor, weatherValues);
        weatherCursor.close();

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
