package com.augmentis.ayp.photogallery;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by Wilailux on 8/16/2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class FlickrFetcherAndroidTest {

    private static final String TAG = "FetcherAndroidTest";
    private FlickrFetcher mFlickrFetcher;
    @Before
    public void setUp() throws Exception {
        mFlickrFetcher = new FlickrFetcher();
    }

    @Test
    public void testGetUrlString() throws Exception {
        String htmlResult = mFlickrFetcher.getUrlString("https://www.augmentis.biz/");

        System.out.println(htmlResult);
        assertThat(htmlResult,containsString("IT Professional Services"));
    }


    @Test
    public void testGetRecrnt() throws Exception {
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        mFlickrFetcher.getRecentPhotos(galleryItemsList);

        Log.d(TAG,"test Search : size = " +galleryItemsList.size());
        assertThat(galleryItemsList.size(),not(0));
    }

    @Test
    public void testSearch() throws Exception {
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        mFlickrFetcher.searchPhotos(galleryItemsList, "bird");

        Log.d(TAG,"test Search : size = " +galleryItemsList.size());
        assertThat(galleryItemsList.size(),not(0));
    }
    @Test
    public void testGetRecentBigSize() throws Exception {
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        mFlickrFetcher.getRecentPhotos(galleryItemsList);

        Log.d(TAG,"test Search : size = " +galleryItemsList.size());
        assertThat(galleryItemsList.size(),not(0));
        assertThat(galleryItemsList.get(0).getBigSizeUrl(),notNullValue());
        assertThat(galleryItemsList.get(0).getOwner(),notNullValue());
    }
}