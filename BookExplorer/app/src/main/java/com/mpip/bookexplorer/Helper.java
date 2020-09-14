package com.mpip.bookexplorer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Helper {
    final  static List<String> isbns=new ArrayList<>();
   static List<String> tmp=new ArrayList<>();
    public static void getIsbns()
    {
        final DatabaseReference refUser= FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {  isbns.clear();
                    for(DataSnapshot isbnSnapshot:dataSnapshot.getChildren())
                    {
                        isbns.add(isbnSnapshot.getKey());
                        //tmp.add(isbnSnapshot.getKey());
                        //System.out.println(isbnSnapshot.getKey());
                    }
                    getData(isbns);
                   // passData(isbns);
                    // System.out.println("ISBN-->"+isbns);
                    //tmp=isbns;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        refUser.addListenerForSingleValueEvent(eventListener);


    }
    public static void getData(List<String> data)
    {
        for (String s:data)
            tmp.add(s);
        System.out.println(tmp+"DAT");
    }
    public static List<String> getAsync()
    {
        getIsbns();
        System.out.println(tmp+"AA");
        return  tmp;
    }

}
