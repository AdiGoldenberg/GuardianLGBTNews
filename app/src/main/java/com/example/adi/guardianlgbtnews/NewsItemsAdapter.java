package com.example.adi.guardianlgbtnews;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * This is a custom adapter placing the title, section and date of publishing of each NewsItem
 * in the given array to the appropriate view in the list_item layout.
 */

public class NewsItemsAdapter extends ArrayAdapter<NewsItem> {
    // Constructor
    public NewsItemsAdapter(Activity context, List<NewsItem> newsItemsList) {
        super(context, 0, newsItemsList);
    }

    /**
     * This override of the getView method verifies that there is an inflated list item view to work on,
     * extracts the object of the current NewsItem of the array to display,
     * and places its title, section and date in the appropriate TextViews.
     *
     * @param position    - the position in the newsItemsList array to be displayed
     * @param convertView - the view to be recycled (can be null)
     * @param parent      - the parent into which the view should be inserted to
     * @return - the updated list item view with the NewsItem info
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        // Get the data of the current NewsItem
        NewsItem currentItem = getItem(position);

        // Update the title
        TextView titleText = (TextView) convertView.findViewById(R.id.title);
        titleText.setText(currentItem.getTitle());

        // Update the section
        TextView sectionText = (TextView) convertView.findViewById(R.id.section);
        sectionText.setText(currentItem.getSection());

        // Update the date
        TextView dateText = (TextView) convertView.findViewById(R.id.date);
        dateText.setText(currentItem.getDate());

        //Return the newly-inflated/recycled view after the values were updated
        return convertView;
    }

}
