package com.rogercotrina.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    public static final String TAG = "MainActivity";

    private boolean isTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Figure out if details fragment layout is present.
        if (findViewById(R.id.weather_detail_container) != null) {
            isTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, new DetailsFragment())
                        .commit();
            }
        } else {
            isTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        if (id == R.id.action_show_in_map) {
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMap() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String locationValue = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Uri geoLocation = Uri.parse("geo:0,0?q=" + locationValue);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocation);
        // Find an app that can supports this intent filter, start activity if found.
        if (null != mapIntent.resolveActivity(getPackageManager())) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onItemSelected(String date) {
        if (isTwoPane) {
            // Here need to replace the details fragment with the respective selected item.
            Bundle args = new Bundle();
            args.putString(DetailsActivity.DATE_KEY, date);

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, fragment).commit();

        } else {
            // Otherwise just use an intent to start a new activity.
            Intent intent = new Intent(this, DetailsActivity.class).putExtra(DetailsActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }
}
