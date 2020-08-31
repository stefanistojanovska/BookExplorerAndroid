package com.mpip.bookexplorer;

import android.app.Application;
import android.content.ClipData;
import android.view.MenuItem;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginCheck extends Application {
    MenuItem item;
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            //do something
        }
    }
}
