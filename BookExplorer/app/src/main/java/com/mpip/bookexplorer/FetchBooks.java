package com.mpip.bookexplorer;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchBooks extends AsyncTask<String, Void, List<String>> {
    List<String> titles;



    public FetchBooks() {
        titles = new ArrayList<>();
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        //fetch
        try {
            String data=NetworkUtils.getBookInfo(strings[0]);
            JSONObject jsonObject=new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject book=jsonArray.getJSONObject(i);
                JSONObject info=book.getJSONObject("volumeInfo");
                titles.add(info.getString("title"));
            }
        }catch (Exception e)
        {
            Log.d("RESULT","ERROR PARSING");
        }
        return titles;


    }




}
