package com.rogercotrina.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rogercotrina.sunshine.app.data.WeatherContract;

import java.util.Date;

import static com.rogercotrina.sunshine.app.data.WeatherContract.LocationEntry;
import static com.rogercotrina.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * Main List Fragment.
 * Created by rogercotrina on 7/16/14.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter listAdapter;

    private static final int FORECAST_LOADER = 0;
    private String location;
    private ListView listView;
    private int positionInList = ListView.INVALID_POSITION;

    private boolean useTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String [] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    // Indices tightly coupled to FORECAST_COLUMNS.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    /**
     * Callback for activities that use this fragment and must implement.
     */
    public interface Callback {
        public void onItemSelected(String date);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment can handle menu events now.
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != location && !location.equalsIgnoreCase(Utility.getPreferredLocation(getActivity()))) {
            // Restart loader if location preference has changed.
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listAdapter = new ForecastAdapter(getActivity(), null, 0);

        listView= (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = listAdapter.getCursor();
                if (null != cursor && cursor.moveToPosition(position)) {
                    ((Callback) getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
                positionInList = position;
            }
        });

        if (null != savedInstanceState && savedInstanceState.containsKey(SELECTED_KEY)) {
            positionInList = savedInstanceState.getInt(SELECTED_KEY);
        }

        listAdapter.setUseTodayLayout(useTodayLayout);

        return rootView;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
        if (null != listAdapter) {
            listAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Only show data after today.
        String startDate = WeatherContract.getDbDateString(new Date());
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        location = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        // Create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor dataCursor) {
        listAdapter.swapCursor(dataCursor);
        if (positionInList != ListView.INVALID_POSITION) {
            // smoothly restore to position in listview.
            listView.smoothScrollToPosition(positionInList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        listAdapter.swapCursor(null);
    }

    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(location);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (positionInList != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, positionInList);
        }
        super.onSaveInstanceState(outState);
    }
}