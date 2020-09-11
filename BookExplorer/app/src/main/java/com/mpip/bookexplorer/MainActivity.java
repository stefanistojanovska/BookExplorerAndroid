package com.mpip.bookexplorer;

import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mpip.bookexplorer.Adapters.CustomListAdapter;
import com.mpip.bookexplorer.Models.Book;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    List<AuthUI.IdpConfig> providers;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    NavigationView nav;
    TextView userHeader;
    TextView displayNameHeader;
    ImageView userImage;
    EditText input;
    List<String> titles=new ArrayList<>();
    List<Book> data=new ArrayList<>();
    LinearLayout home;
    static LinearLayout result;
    static ConstraintLayout details;
    CustomListAdapter adapter;
    Button btnInfo;
    static ScrollView scrollView;

    //data for details
    static TextView detailsTitle;
    static ImageView detailsPoster;
    static TextView detailsAuthors;
    static TextView detailsIsbn;
    static TextView detailsPublisher;
    static TextView detailsDate;
    static TextView detailsPageCount;
    static TextView detailsDescription;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        home.setVisibility(View.VISIBLE);
        result.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);

        providers= Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );


        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {

            editMenuGroup(true);
        }
        else editMenuGroup(false);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if(item.toString().equals("Log in"))
                {
                    showSignInOptions();
                }
                else if(item.toString().equals("Log out"))
                {
                    AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           editMenuGroup(false);
                            showSignInOptions();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                    userHeader.setText("");
                }
                else if(item.toString().equals("Home"))
                {
                    home.setVisibility(View.INVISIBLE);
                    result.setVisibility(View.VISIBLE);
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });




    }


    @Override
    public void onBackPressed() {
        if(details.getVisibility()==View.VISIBLE)
        {
            details.setVisibility(View.INVISIBLE);
            result.setVisibility(View.VISIBLE);

        }
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setTheme(R.style.MyTheme).build(),7117);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem item=menu.findItem(R.id.search_icon);
        SearchView searchView=(SearchView) item.getActionView();
        searchView.setQueryHint("Enter a book name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    data= new FetchBooks().execute(query).get();
                    //showdata

                    home.setVisibility(View.INVISIBLE);
                    result.setVisibility(View.VISIBLE);
                    RecyclerView recyclerView =  findViewById(R.id.recyclerViewResults);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                    adapter=new CustomListAdapter(data);
                    recyclerView.setAdapter(adapter);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //titles=books.getBooks();
                //System.out.println("MAIN------------>"+data.get(0).getTitle());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==7117)
        {
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK)
            {
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this,"Welcome, "+user.getEmail(),Toast.LENGTH_SHORT).show();
                editMenuGroup(true);

            }

        }
    }

    public void initViews()
    {
        home=(LinearLayout) findViewById(R.id.Home);
        result=(LinearLayout) findViewById(R.id.Results);
        details=(ConstraintLayout) findViewById(R.id.Details);
        drawerLayout = (DrawerLayout) findViewById(R.id.mDrawerLayout);
        nav = (NavigationView) findViewById(R.id.navView);
        userHeader=(TextView)nav.getHeaderView(0).findViewById(R.id.txtUserEmail);
        displayNameHeader=(TextView) nav.getHeaderView(0).findViewById(R.id.txtUserDisplayName);
        userImage=(ImageView)nav.getHeaderView(0).findViewById(R.id.userImage);
        btnInfo=(Button) findViewById(R.id.btnInfo);
        detailsTitle=(TextView) findViewById(R.id.detailsTitle);
        detailsPoster=(ImageView) findViewById(R.id.detailsPoster);
        detailsAuthors=(TextView) findViewById(R.id.detailsAuthors);
        detailsIsbn=(TextView) findViewById(R.id.detailsIsbn);
        detailsPublisher=(TextView) findViewById(R.id.detailsPublisher);
        detailsDate=(TextView) findViewById(R.id.detailsDate);
        detailsPageCount=(TextView) findViewById(R.id.detailsCount);
        detailsDescription=(TextView) findViewById(R.id.detailsDescription);
        scrollView=(ScrollView)findViewById(R.id.scroll);
       // details.canScrollVertically(Verti)

    }

    //LOG IN LOG OUT BUTTON
    public void editMenuGroup(Boolean isLoggedIn)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(isLoggedIn)
        {
            nav.getMenu().setGroupVisible(R.id.loginGroup,false);
            nav.getMenu().setGroupVisible(R.id.logoutGroup,true);
            userHeader.setText(user.getEmail());
            displayNameHeader.setText(user.getDisplayName());
            if(user.getPhotoUrl()!=null)
                Picasso.get().load(user.getPhotoUrl()).resize(50,50).transform(new CircleTransform()).into(userImage);
            else
                Picasso.get().load("https://www.warmerwaters.co.uk/wp-content/uploads/2015/08/user-profile-icon.png").resize(50,50).transform(new CircleTransform()).into(userImage);

        }
        else
        {
            nav.getMenu().setGroupVisible(R.id.loginGroup,true);
            nav.getMenu().setGroupVisible(R.id.logoutGroup,false);
            userHeader.setText("You are not logged in!");
            displayNameHeader.setText("");

            Picasso.get().load("https://thumbs.dreamstime.com/b/incognito-icon-question-mark-vector-icon-protection-symbol-vector-stock-illustration-incognito-icon-question-mark-vector-icon-181081062.jpg").transform(new CircleTransform()).into(userImage);
        }
    }


    public static void getDetails(Book b)
    {
        result.setVisibility(View.INVISIBLE);
        details.setVisibility(View.VISIBLE);

        scrollView.scrollTo(0,scrollView.getTop());
        detailsTitle.setText(b.getTitle());
        //format authors
        List<String> authors=b.getAuthors();
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
        String authorDataHtml = "<b>" + "Author/s:  "+ "</b> " + authorData;
        detailsAuthors.setText(Html.fromHtml(authorDataHtml));

        Picasso.get().load(b.getPoster()).into(detailsPoster);

        String isbnHtml="<b>" + "ISBN:  "+ "</b> " + b.getISBN();
        detailsIsbn.setText(Html.fromHtml(isbnHtml));

        String publisherHtml="<b>" + "Publisher:  "+ "</b> " + b.getPublisher();
        detailsPublisher.setText(Html.fromHtml(publisherHtml));

        String dateHtml="<b>" + "Date published:  "+ "</b> " + b.getDatePublished();
        detailsDate.setText(Html.fromHtml(dateHtml));

        String countHtml="<b>" + "Page count:  "+ "</b> ";
        if(b.getPageCount()==0)countHtml+="/";
        else countHtml+=b.getPageCount();
        detailsPageCount.setText(Html.fromHtml(countHtml));

        detailsDescription.setText(b.getDescription());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            detailsDescription.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }


    }




}
