package com.rogercotrina.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rogercotrina.sunshine.app.data.WeatherContract;

/**
 * Created by rogercotrina on 9/1/14.
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    public static String DATE_KEY = "forecast_date";

    private static final String LOCATION_KEY = "location";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    private ShareActionProvider shareActionProvider;
    private String location;
    private String forecast;
    private String dateString;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY, location);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Using arguments bundle instead of intents since the activity will pass them now.
        Bundle args = getArguments();
        if (null != args && args.containsKey(DetailsActivity.DATE_KEY) && null != location && !location.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get arguments bundle
        Bundle args = getArguments();
        if (null != args) {
            dateString = args.getString(DetailsActivity.DATE_KEY);
        }

        if (null != savedInstanceState) {
            location = savedInstanceState.getString(LOCATION_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailsfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_menu_item_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before, we can go ahead and share the intent.
        if (null != forecast) {
            shareActionProvider.setShareIntent(getDefaultShareIntent());
        }
    }

    private Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != savedInstanceState) {
            location = savedInstanceState.getString(LOCATION_KEY);
        }
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        location = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, dateString);
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (null != cursor && cursor.moveToFirst()) {
            // Read weather condition ID from cursor
            int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            // Read date from cursor and update views for day of week and date
            String date = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            // Read description from cursor and update view
            String description = cursor.getString(cursor.getColumnIndex(
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            mDescriptionView.setText(description);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
            mHighTempView.setText(highString);

            // Read low temperature from cursor and update view
            double low = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
            mLowTempView.setText(lowString);

            // Read humidity from cursor and update view
            float humidity = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view
            float windSpeedStr = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDirStr = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            // Read pressure from cursor and update view
            float pressure = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            forecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

            if (null != shareActionProvider) {
                shareActionProvider.setShareIntent(getDefaultShareIntent());
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
