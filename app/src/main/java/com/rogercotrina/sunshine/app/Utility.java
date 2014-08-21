package com.rogercotrina.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rogercotrina.sunshine.app.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Common utility class with static methods.
 * Created by rogercotrina on 8/14/14.
 */
public class Utility {

    public static String getPreferredLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_temperature_key),
                context.getString(R.string.pref_temperature_units_metric)).
                equals(context.getString(R.string.pref_temperature_units_metric));
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = (9*temperature/5)+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDbDateFromString(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
