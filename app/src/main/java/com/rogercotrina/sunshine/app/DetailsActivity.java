package com.rogercotrina.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;


public class DetailsActivity extends ActionBarActivity {

    public static final String SUNSHINE_HASHTAG = "#SunshineApp";

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

    public static class DetailsFragment extends Fragment {
        private DetailsFragment() {

        }
        private ShareActionProvider shareActionProvider;
        private TextView contentTextView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Intent receivedIntent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_details, container, false);
            if (null != receivedIntent) {
                String weatherDayData = receivedIntent.getStringExtra(AppConstants.WEATHER_DAY_DATA);
                contentTextView = (TextView) rootView.findViewById(R.id.details_text_data);
                contentTextView.setText(weatherDayData);
            }
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailsfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_menu_item_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (null != shareActionProvider) {
                shareActionProvider.setShareIntent(getDefaultShareIntent());
            }
        }

        private Intent getDefaultShareIntent() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, contentTextView.getText().toString() + SUNSHINE_HASHTAG);
            return intent;
        }

    }
}
