package com.mpip.bookexplorer;

import android.os.AsyncTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.mpip.bookexplorer.Models.Book;

import java.util.ArrayList;
import java.util.List;

public class IsbnAsync extends AsyncTask<Void,Void,List<String>> {
   List<String> isbns=new ArrayList<>();
     List<String> tmp=new ArrayList<>();
    public IsbnAsync() {

    }
    @Override
    protected List<String> doInBackground(Void... voids) {

       isbns=Helper.getAsync();

        //ystem.out.println(tmp);
        System.out.println("ISBN2-->"+isbns);
        System.out.println("tmp-->"+tmp);
        return isbns;
    }
    public void passData(List<String> isbns)
    {

    }
}
