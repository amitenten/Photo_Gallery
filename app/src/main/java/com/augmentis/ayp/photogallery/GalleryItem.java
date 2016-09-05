package com.augmentis.ayp.photogallery;

import android.net.Uri;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class GalleryItem {

    private String mId;
    public String mTitle;
    public String mUrl;
    private String bigSizeUrl;
    private String mOwner;
    private String mLat;
    private String mLon;

    public String getLat() {
        return mLat;
    }

    public void setLat(String Lat) {
        mLat = Lat;
    }

    public String getLon() {
        return mLon;
    }

    public void setLon(String Lon) {
        mLon = Lon;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName() {
        return getTitle();
    }

    public void setName(String name) {
        setTitle(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GalleryItem) {
            //is GalleryIem too
            GalleryItem that = (GalleryItem) obj;

            return that.mId != null && this.mId != null && that.mId.equals(mId);
        }
            return false;
    }

    public void setBigSizeUrl(String bigSizeUrl) {
        this.bigSizeUrl = bigSizeUrl;
    }

    public String getBigSizeUrl() {
        return bigSizeUrl;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public String getOwner() {
        return mOwner;
    }

    private static final String PHOTO_URL_PREFIX = "http://www.flickr.com/photos/";

    public Uri getPhotoUri() {
        return Uri.parse(PHOTO_URL_PREFIX).buildUpon() //Return builder
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
