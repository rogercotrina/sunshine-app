package com.rogercotrina.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static com.rogercotrina.sunshine.app.data.WeatherContract.WeatherEntry;


public class DetailsActivity extends ActionBarActivity {

    public static String DATE_KEY = "forecast_date";
    private static final String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DetailsFragment extends Fragment implements LoaderCallbacks<Cursor> {

        public static final String LOG_TAG = DetailsFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

        public DetailsFragment() {
            setHasOptionsMenu(true);
        }

        private ShareActionProvider shareActionProvider;
        private String location;
        private String forecast;

        private static final int DETAIL_LOADER = 0;

        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
        };

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(LOCATION_KEY, location);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (null != location && !location.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details, container, false);
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
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            if (null != savedInstanceState) {
                location = savedInstanceState.getString(LOCATION_KEY);
            }
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            Intent receivedIntent = getActivity().getIntent();

            // Early return.
            if (null == receivedIntent || !receivedIntent.hasExtra(DATE_KEY)) {
                return null;
            }

            String forecastDate = receivedIntent.getStringExtra(DATE_KEY);
            String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

            location = Utility.getPreferredLocation(getActivity());
            Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithDate(location, forecastDate);
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
            if (!cursor.moveToFirst()) {
                // Early return if loader did not return any cursor items.
                return;
            }

            // Bind data into views.
            String dateString = Utility.formatDate(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            ((TextView) getView().findViewById(R.id.detail_date_textview)).setText(dateString);

            String weatherDescription = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            ((TextView) getView().findViewById(R.id.detail_forecast_textview)).setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());
            String high = Utility.formatTemperature(cursor.getDouble(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_high_textview)).setText(high);
            String low = Utility.formatTemperature(cursor.getDouble(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_low_textview)).setText(low);

            forecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            if (null != shareActionProvider) {
                shareActionProvider.setShareIntent(getDefaultShareIntent());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
        }
    }
}
