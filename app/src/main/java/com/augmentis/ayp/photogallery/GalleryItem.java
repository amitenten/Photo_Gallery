package com.augmentis.ayp.photogallery;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class GalleryItem {

    private String mId;
    public String mTitle;
    public String mUrl;

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
}
