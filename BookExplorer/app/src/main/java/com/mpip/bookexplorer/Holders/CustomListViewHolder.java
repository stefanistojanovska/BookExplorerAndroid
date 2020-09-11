package com.mpip.bookexplorer.Holders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mpip.bookexplorer.MainActivity;
import com.mpip.bookexplorer.Models.Book;
import com.mpip.bookexplorer.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class CustomListViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    private TextView textTitle;
    private TextView textAuthors;
    private TextView textDesc;
    public Button btnInfo;


    public CustomListViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.poster);
        textTitle = itemView.findViewById(R.id.textTitle);
        textAuthors=itemView.findViewById(R.id.textAuthors);
        textDesc=itemView.findViewById(R.id.textDescription);
        btnInfo=(Button) itemView.findViewById(R.id.btnInfo);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textDesc.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    public void setData(final Book book)  {
        textTitle.setText(book.getTitle());

        //Format authors
        List<String> authors=book.getAuthors();
        String authorData="Unknown author";
        if(authors.size()==1)
            authorData=authors.get(0);
        else if(authors.size()>0)
        {
            StringBuilder sb=new StringBuilder();
            for(String author:authors)
            {
                sb.append(author).append(", ");
            }
            String tmp=sb.toString();
            tmp=tmp.substring(0,tmp.length()-2);
            authorData=tmp;
        }
        if(authorData.length()>40)
            authorData=authorData.substring(0,40)+"..."; //za da bidat samo eden red
        textAuthors.setText(authorData);

        String d=book.getDescription();
        if(d.length()>150)
            d=d.substring(0,150)+"...";

        textDesc.setText(d);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getDetails(book);
            }
        });

    }


}
