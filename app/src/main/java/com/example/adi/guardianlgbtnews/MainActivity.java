package com.example.adi.guardianlgbtnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsItem>> {
    // States
    private static final String REQUEST_URL =
            "https://content.guardianapis.com/search";
    private static final int NEWSITEMS_LOADER_ID = 1;
    private NewsItemsAdapter mAdapter;
    private TextView mEmptyView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the object references for the ProgressBar and the TextView for the empty list notification
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mEmptyView = (TextView) findViewById(R.id.empty_view);

        // Get the object references for the ListView
        ListView ListView = (ListView) findViewById(R.id.list);
        // Create a new NewsItemsAdapter
        mAdapter = new NewsItemsAdapter(this, new ArrayList<NewsItem>());
        // Set the adapter on the ListView so the list can be populated in the UI
        ListView.setAdapter(mAdapter);

        // Set an onItemClickListener on the news items list
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent, parse the url into Uri and pass it to the intent
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdapter.getItem(position).getUrl()));
                // If there is an app that can handle the request, send it
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Call setEmptyView() on the list so that it shows the empty_view instead of the list if empty.
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        ListView.setEmptyView(mEmptyView);

        // Get the status of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        // If there is network access create a Loader.
        if (isConnected) {
            // Create an NewsItemsLoader making an http request in the background thread, parsing
            // the JSON response and extracting NewsItems features into an array).
            // Then in the main thread, the UI would be updated.
            getSupportLoaderManager().initLoader(NEWSITEMS_LOADER_ID, null, this);
        } else {
            // If there is no network access hide the ProgressBar and set the EmptyView text to notify the user
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setText(R.string.no_network_access);
        }
    }

    // This method handles the LoaderCallback of Loader creation by creating a NewsItemsLoader
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {
        // Get the default SharedPreference
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve the String values from the num of articles preferences.
        // The second parameter is the default value for this preference.
        String numArticles = sharedPrefs.getString(
                getString(R.string.settings_num_articles_key),
                getString(R.string.settings_num_articles_default));

        // Retrieve the String values from the order by preferences.
        // Again the second parameter is the default value for this preference.
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        // Break apart the REQUEST_URL to its parameter
        Uri baseUri = Uri.parse(REQUEST_URL);

        // Prepare the parsed baseUri so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();
//            "https://content.guardianapis.com/search?q=LGBT&show-fields=byline&api-key=test";

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter("q", "LGBT");
        uriBuilder.appendQueryParameter("show-fields", "byline");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("page-size", numArticles);
        uriBuilder.appendQueryParameter("api-key", "test");

        // Return the completed uri
        return new NewsItemsLoader(this, uriBuilder.toString());
    }

    // This method handles the LoaderCallback called when the NewsItemsLoader finished loading the
    // NewsItems data by updating the UI. If the there are no NewsItems found, it would hide the
    // list and show a textView notifying the user of that.
    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> data) {
        // Hide the progress bar indicator
        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of Earthquakes, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }

        // Change the text of the mEmptyView to notify the user that there were no earthquakes found.
        mEmptyView.setText(R.string.empty_list);
    }

    // This method handles the LoaderCallback of Loader resetting by clearing the mAdapter
    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        mAdapter.clear();
    }

    // This method inflates the options menu when the activity is launched
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // This method opens the SettingsActivity when the Settings item is selected in the m=options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Objects of this inner class are Loaders, managed by the LoaderManager, that are able to
     * perform http requests in a background thread, and return a list of NewsItems from the
     * REQUEST_URL.
     */
    private static class NewsItemsLoader extends AsyncTaskLoader<List<NewsItem>> {
        // States
        private String mUrl;

        // A constructor setting the URL to be worked on as a state variable
        public NewsItemsLoader(Context context, String url) {
            super(context);
            // Set mUrl to be the URL
            mUrl = url;
        }

        // This method is called by the initLoader, and triggers the newly created Loader to start
        // running the loadInBackground code.
        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        // In the background thread, if the REQUEST_URL isn't null, make an http request, parse the
        // JSON response, extract the Earthquakes data and return it in a list of earthquakes.
        @Override
        public List<NewsItem> loadInBackground() {
            if (mUrl == null) {
                return null;
            } else {
                return QueryUtils.fetchNewsItems(mUrl);
            }
        }
    }
}
