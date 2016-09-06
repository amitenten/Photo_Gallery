package com.augmentis.ayp.photogallery;

import android.content.ClipData;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";

    /**
     * get url มาจากตัวเว็บที่เราจะใช้งาน
     * @param urlSpec url ที่รับมา
     * @return out ออกมาเป็น ByteArray
     * @throws IOException ...
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        // เชื่อมต่อ url
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();// คือ data ที่ web ส่งมา

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()+ ": with " + urlSpec);
            }

            int bytesRead = 0;

            byte[] buffer = new byte[2048];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer,0,bytesRead);
            }

            out.close();

            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * รับ url จาก bytes array มาเป็น string
     * @param urlSpec UrlBytes
     * @return URL String
     * @throws IOException ...
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //
    private static final String FLICKER_URL = "https://api.flickr.com/services/rest/";

    private static final String API_KEY = "f35a4b506eef958f81023b14e5b01524";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    /**
     *
     * @param method
     * @param param
     * @return
     * @throws IOException
     */
    private String buildUri(String method, String ... param) throws IOException{
        Uri baseUrl = Uri.parse(FLICKER_URL);
        Uri.Builder builder = baseUrl.buildUpon();

        builder.appendQueryParameter("method",method);
        builder.appendQueryParameter("api_key",API_KEY);
        builder.appendQueryParameter("format","json");
        builder.appendQueryParameter("nojsoncallback","1");
        builder.appendQueryParameter("extras","url_s,url_z,geo");

        if (METHOD_SEARCH.equalsIgnoreCase(method)) {
            builder.appendQueryParameter("text", param[0]);
        }
        if (param.length > 1) {
            builder.appendQueryParameter("lat",param[1]);
            builder.appendQueryParameter("lon",param[2]);
        }
        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG,"Run URL: " + url);

        return url;
    }

    public void fetchPhoto(List<GalleryItem> items, String url) throws IOException, JSONException{

            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items,jsonStr);
            }
    }

    /**
     * Search photo then put into <b>items</b>
     * @param items array target
     * @param key to search
     */
    public void searchPhotos(List<GalleryItem> items, String key) {
        try {
            String url = buildUri(METHOD_SEARCH, key);
            fetchPhoto(items, url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

    /**
     *
     * @param items
     * @param key
     * @param lat
     * @param lon
     */
    public void searchPhotos(List<GalleryItem> items, String key, String lat, String lon) {
        try {
            String url = buildUri(METHOD_SEARCH, key, lat, lon);
            fetchPhoto(items, url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

    /**
     * get photo ปัจจุบันของเว็บ
     * @param items รูปที่ get จากเว็บ
     */
    public void getRecentPhotos(List<GalleryItem> items ) {
        try {
            String url = buildUri(METHOD_GET_RECENT);
            fetchPhoto(items, url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

    /**
     * json form ที่ใช้ติดต่อกับ web
     * @param newGalleryItemList สร้าง GalleryItemList
     * @param jsonBodyStr
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr) throws IOException,JSONException {
        JSONObject jsonBody = new JSONObject(jsonBodyStr);// convert String to JSON
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

//        JSONArray photoListJson = new JSONObject(jsonBodyStr).getJSONObject("photos").getJSONArray("photo"); //เหมือน 3 บันทัดข้างบน

        for (int i = 0; i < photoListJson.length(); i++) {

            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));
            item.setOwner(jsonPhotoItem.getString("owner"));

            if(!jsonPhotoItem.has("url_s")) {
                continue;
            }
            item.setUrl(jsonPhotoItem.getString("url_s"));

            if(!jsonPhotoItem.has("url_z")) {
                continue;
            }
            item.setBigSizeUrl(jsonPhotoItem.getString("url_z"));

            item.setLat(jsonPhotoItem.getString("latitude"));
            item.setLon(jsonPhotoItem.getString("longitude"));

            newGalleryItemList.add(item);

        }
    }

    /**
     * เรียกไอเทมขึ้นมา โดยใช้ url ที่รับมา
     * @param url
     * @return jsonStr
     * @throws IOException
     */
    private String queryItem(String url) throws IOException {

        Log.i(TAG,"Run URL: " + url);
        String jsonString = getUrlString(url);

        Log.i(TAG,"Search: Received JSON: "+ jsonString);
        return jsonString;
    }
}
