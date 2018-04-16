package com.ien.snowdrop.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FlickrServerFetcher {
    private static final String TAG = "FlickrServerFetcher";
    private static final String API_KEY_FLICKR = "08d40bde6d31142e79ca826915480141";

    public String getJSONString(String UrlSpec) throws IOException{

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(UrlSpec)
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();
        return result;
    }

    public List<GalleryItem> fetchItems(){
        List<GalleryItem> galleryItems = new ArrayList<>();
        try {
            String url = Uri.parse("https://up.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY_FLICKR)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")            //запрос ссылки на миниатюру
                    .build().toString();
            String jsonString = getJSONString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(galleryItems, jsonBody);
        }
        catch (IOException ioe){
            Log.e(TAG, "Ошибка загрузких данных", ioe);
        }
        catch (JSONException joe){
            Log.e(TAG, "Ошибка парсинга JSON", joe);
        }
        return galleryItems;
    }
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");                  //в photos лежит массив фоток photo
        JSONArray photosJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i=0;i<photosJsonArray.length();i++){                                     //пробегаем по всем фоткам
            JSONObject photoJsonObject = photosJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));                            //Получаем из Json ID
            item.setCaption(photoJsonObject.getString("title"));                    //Получаем title

            if (!photoJsonObject.has("url_s")){                                     //Объекты без ссылок на фотографии не интересуют, фильтруем
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);

        }

    }
}
