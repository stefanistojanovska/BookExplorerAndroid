package com.mpip.bookexplorer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
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
import com.google.firebase.database.*;
import com.mpip.bookexplorer.Adapters.CustomListAdapter;
import com.mpip.bookexplorer.Models.Book;
import com.mpip.bookexplorer.Models.FetchBooksByIsbn;
import com.squareup.picasso.Picasso;

import java.util.*;
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
    RelativeLayout wishlist;
    static LinearLayout result;
    static ConstraintLayout details;
    LinearLayout wishListMessageView;
    CustomListAdapter adapter;
    Button btnInfo;
    Button btnWishlist;
    TextView wishlistMessage;
    int lastView;//0->results; 1->wishlist; 2->home
    Boolean change=false;


    //data for details
    static TextView detailsTitle;
    static ImageView detailsPoster;
    static TextView detailsAuthors;
    static TextView detailsIsbn;
    static TextView detailsPublisher;
    static TextView detailsDate;
    static TextView detailsPageCount;
    static TextView detailsDescription;
    static Button btnWishlist2;
    static ScrollView scrollView;
    static RelativeLayout wishlistStat;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initHome();

        home.setVisibility(View.VISIBLE);
        result.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        wishlist.setVisibility(View.INVISIBLE);
        wishListMessageView.setVisibility(View.INVISIBLE);



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
                drawerLayout.closeDrawers();
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

                    //update current details item
                    btnWishlist.setText("★ Wishlist");
                    if(wishlist.getVisibility()==View.VISIBLE || wishListMessageView.getVisibility()==View.VISIBLE)
                    {
                        home.setVisibility(View.VISIBLE);
                        result.setVisibility(View.INVISIBLE);
                        details.setVisibility(View.INVISIBLE);
                        wishlist.setVisibility(View.INVISIBLE);
                        wishListMessageView.setVisibility(View.INVISIBLE);
                    }


                }
                else if(item.toString().equals("Home"))
                {
                    home.setVisibility(View.VISIBLE);
                    result.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    wishlist.setVisibility(View.INVISIBLE);
                    wishListMessageView.setVisibility(View.INVISIBLE);
                    //drawerLayout.closeDrawers();
                }
                else if(item.toString().equals("Wishlist"))
                {

                        populateWishlist();


                }
                return true;
            }
        });




    }
    private void populateWishlist() {


        lastView=1;
        home.setVisibility(View.INVISIBLE);
        result.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        //
        final RecyclerView recyclerView =  findViewById(R.id.recyclerViewWishlist);

        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {

            wishlist.setVisibility(View.INVISIBLE);
            wishListMessageView.setVisibility(View.VISIBLE);
            wishlistMessage.setText("You need to be logged in for this activity!");


        }
        else
        {
            //get isbns
            //TODO: FETCH FROM DATABASE
            final List<String> isbns=new ArrayList<>();
            final DatabaseReference refUser=FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    if(dataSnapshot.exists())
                    {
                        home.setVisibility(View.INVISIBLE);
                        details.setVisibility(View.INVISIBLE);
                        result.setVisibility(View.INVISIBLE);
                        wishlist.setVisibility(View.VISIBLE);
                        wishListMessageView.setVisibility(View.INVISIBLE);

                        isbns.clear();
                        for(DataSnapshot isbnSnapshot:dataSnapshot.getChildren())
                        {
                            isbns.add(isbnSnapshot.getKey());
                        }
                        System.out.println(isbns);
                        //fetch books
                        try {
                            List<Book> data = new FetchBooksByIsbn().execute(isbns).get();
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                            adapter=new CustomListAdapter(data);
                            recyclerView.setAdapter(adapter);

                        }
                        catch (Exception e)
                        {
                            e.getMessage();
                        }


                    }
                    else
                    {
                        //empty wishlist
                        wishlist.setVisibility(View.INVISIBLE);
                        wishListMessageView.setVisibility(View.VISIBLE);
                        wishlistMessage.setText("Your wishlist is empty!");

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            refUser.addListenerForSingleValueEvent(eventListener);



        }

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter=new CustomListAdapter(data);
        recyclerView.setAdapter(adapter);

    }

    public void detailsCheck()
    {
        if(details.getVisibility()==View.VISIBLE)
        {
            //check if visible
            btnWishlist.setText("★ Wishlist");
            if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            {
                String isbn=detailsIsbn.getText().toString().substring(6);
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(isbn);
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            btnWishlist.setText("✅ Wishlist");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                ref.addListenerForSingleValueEvent(eventListener);

            }


        }
    }


    @Override
    public void onBackPressed() {
        if(details.getVisibility()==View.VISIBLE && lastView==0)
        {
            details.setVisibility(View.INVISIBLE);
            result.setVisibility(View.VISIBLE);

        }
        else if(details.getVisibility()==View.VISIBLE && lastView==1)
        {
            populateWishlist();

        } if(lastView==2)
        {
            home.setVisibility(View.VISIBLE);
            details.setVisibility(View.INVISIBLE);

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

                    lastView=0;
                    home.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    result.setVisibility(View.VISIBLE);
                    wishlist.setVisibility(View.INVISIBLE);
                    wishlistMessage.setVisibility(View.INVISIBLE);
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
                detailsCheck();
                if(lastView==1)
                {
                    home.setVisibility(View.VISIBLE);
                    wishlist.setVisibility(View.INVISIBLE);
                    wishListMessageView.setVisibility(View.INVISIBLE);
                }

            }

        }
    }

    public void initViews()
    {
        home=(LinearLayout) findViewById(R.id.Home);
        result=(LinearLayout) findViewById(R.id.Results);
        details=(ConstraintLayout) findViewById(R.id.Details);
        wishlist=(RelativeLayout) findViewById(R.id.Wishlist);
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
        btnWishlist=(Button) findViewById(R.id.btnWishlist);
        btnWishlist2=(Button) findViewById(R.id.btnWishlist);
        wishlistMessage=(TextView) findViewById(R.id.wishlistMessage);
        wishlistStat=(RelativeLayout) findViewById(R.id.Wishlist);
        wishListMessageView=(LinearLayout) findViewById(R.id.WishlistMessageView);
       progressBar=(ProgressBar) findViewById(R.id.progressbar);

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

        if(result.getVisibility()==View.VISIBLE)
        {
            result.setVisibility(View.INVISIBLE);
        }
        if(wishlistStat.getVisibility()==View.VISIBLE)
        {
            wishlistStat.setVisibility(View.INVISIBLE);
        }


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
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            final String isbn = b.getISBN();
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(isbn);

            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    if (dataSnapshot.exists()) {
                        btnWishlist2.setText("✅ Wishlist");
                    } else {
                        btnWishlist2.setText("★ Wishlist");
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            ref.addListenerForSingleValueEvent(eventListener);

        }
        else
        {
            btnWishlist2.setText("★ Wishlist");
        }
    }


    public void addToWishList(View view) {
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            Toast toast=Toast.makeText(this,"You have to be logged in for this activity!",Toast.LENGTH_SHORT);
            View v=toast.getView();
            v.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
            toast.show();
        }
        else
        {
           final String isbn=detailsIsbn.getText().toString().substring(6);
            final String title=detailsTitle.getText().toString();
            //update vo db ---> ako ne postoi go kreira, ako postoi pravi update
            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            DatabaseReference refBook=ref.child(isbn);

            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    if(dataSnapshot.exists())
                    {
                        //remove from wishlist
                        dataSnapshot.getRef().removeValue();
                        btnWishlist.setText("★ Wishlist");
                        Toast toast=Toast.makeText(MainActivity.this,"Book successfully removed!",Toast.LENGTH_SHORT);
                        View v=toast.getView();
                        v.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        toast.show();


                    }
                    else
                    {
                        //add to wishlist
                        HashMap map=new HashMap();
                        map.put(isbn,title);
                        ref.updateChildren(map);
                        btnWishlist.setText("✅ Wishlist");
                        Toast toast=Toast.makeText(MainActivity.this,"Book successfully added!",Toast.LENGTH_SHORT);
                        View v=toast.getView();
                        v.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        toast.show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            refBook.addListenerForSingleValueEvent(eventListener);

        }
    }

    public void initHome()
    {
        //init views
        ImageView bsPoster1=(ImageView)findViewById(R.id.bestPoster1);
        ImageView bsPoster2=(ImageView)findViewById(R.id.bestPoster2);
        ImageView bsPoster3=(ImageView)findViewById(R.id.bestPoster3);
        ImageView recPoster1=(ImageView)findViewById(R.id.recPoster1);
        ImageView recPoster2=(ImageView)findViewById(R.id.recPoster2);
        ImageView recPoster3=(ImageView)findViewById(R.id.recPoster3);
        TextView bsTitle1=(TextView)findViewById(R.id.bestTitle1);
        TextView bsTitle2=(TextView)findViewById(R.id.bestTitle2);
        TextView bsTitle3=(TextView)findViewById(R.id.bestTitle3);
        TextView recTitle1=(TextView)findViewById(R.id.recTitle1);
        TextView recTitle2=(TextView)findViewById(R.id.recTitle2);
        TextView recTitle3=(TextView)findViewById(R.id.recTitle3);
        Button btnBest1=(Button)findViewById(R.id.bestBtn1);
        Button btnBest2=(Button)findViewById(R.id.bestBtn2);
        Button btnBest3=(Button)findViewById(R.id.bestBtn3);
        Button btnRec1=(Button)findViewById(R.id.recBtn1);
        Button btnRec2=(Button)findViewById(R.id.recBtn2);
        Button btnRec3=(Button)findViewById(R.id.recBtn3);

        List<String> data=new ArrayList<>();
        data.add("9780441020157");
        data.add("9781447213246");
        data.add("9780446573665");
        data.add("0520913825");
        data.add("0472067842");
        data.add("3642020887");
        try {
            List<Book> bookData = new FetchBooksByIsbn().execute(data).get();
            final Book b1=bookData.get(0);
            Picasso.get().load(b1.getPoster()).into(bsPoster1);
            bsTitle1.setText(b1.getTitle());

            final Book b2=bookData.get(1);
            Picasso.get().load(b2.getPoster()).into(bsPoster2);
            bsTitle2.setText(b2.getTitle());

            final Book b3=bookData.get(2);
            Picasso.get().load(b3.getPoster()).into(bsPoster3);
            bsTitle3.setText(b3.getTitle());

            final Book r1=bookData.get(3);
            Picasso.get().load(r1.getPoster()).into(recPoster1);
            recTitle1.setText(r1.getTitle());

            final Book r2=bookData.get(4);
            Picasso.get().load(r2.getPoster()).into(recPoster2);
            recTitle2.setText(r2.getTitle());

            final Book r3=bookData.get(5);
            Picasso.get().load(r3.getPoster()).into(recPoster3);
            recTitle3.setText(r3.getTitle());

            //on click listeners
            btnBest1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(b1);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });
            btnBest2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(b2);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });
            btnBest3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(b3);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });
            btnRec1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(r1);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });
            btnRec2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(r2);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });
            btnRec3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDetails(r3);
                    home.setVisibility(View.INVISIBLE);
                    lastView=2;
                }
            });

        }
        catch (Exception e)
        {
            e.getMessage();
        }


    }
}
