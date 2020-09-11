package com.mpip.bookexplorer;

import android.os.AsyncTask;
import android.util.Log;
import com.mpip.bookexplorer.Models.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchBooks extends AsyncTask<String, Void, List<Book>> {
    List<String> titles;
    List<Book> bookResults=new ArrayList<>();
    Book bookTmp;



    public FetchBooks() {
        titles = new ArrayList<>();
    }

    @Override
    protected List<Book> doInBackground(String... strings) {

        try {
            //fetch
            String data=NetworkUtils.getBookInfo(strings[0]);
            //parse
            JSONObject jsonObject=new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject book=jsonArray.getJSONObject(i);
                JSONObject info=book.getJSONObject("volumeInfo");

                //CREATE BOOK OBJECT---->
                String title=info.getString("title");
                String poster="https://scontent.fskp4-2.fna.fbcdn.net/v/t1.15752-9/119042000_421418825502739_5480645885155497263_n.png?_nc_cat=110&_nc_sid=b96e70&_nc_ohc=BNXWgWupV9IAX-xVWx-&_nc_ht=scontent.fskp4-2.fna&oh=05e706a2956c7ccfe6ab6a9496dca48c&oe=5F7A10E8";
                String desc="No description available...";
                int pageCount=0;
                String isbn="Not available";
                String date="Not available";
                String publisher="Not available";
                List<String> authors=new ArrayList<>();

                try
                {
                    poster=info.getJSONObject("imageLinks").getString("smallThumbnail");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }
                try
                {
                    desc=info.getString("description");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }
                try
                {
                    pageCount=info.getInt("pageCount");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }

                try
                {
                    isbn=info.getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }

                try
                {
                    date=info.getString("publishedDate");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }
                try
                {
                    publisher=info.getString("publisher");
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }
                try
                {
                    //GET AUTHORS
                    JSONArray jsonAuthors=info.getJSONArray("authors");
                    for(int j=0;j<jsonAuthors.length();j++)
                        authors.add(jsonAuthors.getString(j));
                }
                catch (Exception e)
                {
                    Log.d("ERROR","PARSING-->"+e.getMessage());
                }



                bookTmp=new Book(title,authors,desc,pageCount,poster,isbn,date,publisher);
                bookResults.add(bookTmp);

            }
            //Book b=bookResults.get(2);
            //Log.d("DATA",b.getTitle()+"\n"+b.getDescription()+"\n"+b.getISBN()+"\n"+b.getPageCount()+"\n"+b.getPublisher());

        }catch (Exception e)
        {
            Log.d("ERROR","PARSING-->"+e.getMessage());
        }
        return bookResults;


    }




}
