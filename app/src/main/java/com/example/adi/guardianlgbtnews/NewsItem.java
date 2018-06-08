package com.example.adi.guardianlgbtnews;

/**
 * Objects of this class holds a News Item.
 */

public class NewsItem {
    //States
    private String mTitle;
    private String mSection;
    private String mDate;
    private String mUrl;

    // Constructor
    public NewsItem(String title, String section, String url, String date) {
        mTitle = title;
        mSection = section;
        mUrl = url;
        mDate = date;
    }

    // Get methods
    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDate() {
        return mDate;
    }
}
