package com.example.adi.guardianlgbtnews;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving data from the Guardian url
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Guardian dataset in the given URL and return the JSON response as String.
     */
    public static List<NewsItem> fetchNewsItems(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Parse the JSON response and build a NewsItems list if it's not null or empty
        List<NewsItem> newsItemsList = extractNewsItems(jsonResponse);
        // Return the list of NewsItems
        return newsItemsList;
    }

    /**
     * Return a list of {@link NewsItem} objects that has been built up from parsing a JSON response.
     *
     * @param jsonString the JSON string received from the http request
     */
    private static List<NewsItem> extractNewsItems(String jsonString) {
        // If the jsonString is empty return null
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding NewsItems to
        List<NewsItem> newsItems = new ArrayList<>();

        // Try to parse the jsonString. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Convert the jsonString into a JSONObject
            JSONObject root = new JSONObject(jsonString);
            // Replace the root JSONObject with the "response" JSONObject within it
            root = root.getJSONObject("response");
            // Extract “results” JSONArray
            JSONArray resultsArray = root.optJSONArray("results");
            // If there are items in the results array
            if (resultsArray.length() > 0) {
                // Loop through each result in the array
                for (int i = 0; i < resultsArray.length(); i++) {
                    // Get news item JSONObject at position i
                    JSONObject currentItem = resultsArray.getJSONObject(i);
                    // Get “webTitle” for the item's title
                    String currentTitle = currentItem.getString("webTitle");
                    // If it contains the author's name - omit it
                    if (currentTitle.contains(" | ")){
                        currentTitle = currentTitle.substring(0, currentTitle.indexOf(" | "));
                    }
                    // Get “sectionName” for the item's section
                    String currentSection = currentItem.getString("sectionName");
                    // Get “webUrl” for the item's section
                    String currentUrl = currentItem.getString("webUrl");
                    // Get “byline” for the item's author name (from the "fields" JSONObject).
                    // If missing set to "".
                    String currentAuthor = currentItem.getJSONObject("fields").getString("byline");
                    if (currentAuthor == null){
                        currentAuthor = "";
                    }
                    // Get “webPublicationDate” for the item's publishing date. If missing set to "".
                    String currentDate = currentItem.getString("webPublicationDate").substring(0, 10);
                    if (currentDate == null){
                        currentDate = "";
                    }
                    // Create a NewsItem java object from the data and add it to the list
                    newsItems.add(i, new NewsItem(currentTitle, currentSection, currentUrl, currentAuthor, currentDate));
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return newsItems;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the NewsItems JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
