package com.mpip.bookexplorer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
    RelativeLayout progress;
    int numberOfItems;



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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initHome();
        numberOfItems=0;


        home.setVisibility(View.VISIBLE);
        result.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        wishlist.setVisibility(View.INVISIBLE);
        wishListMessageView.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);



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
                    numberOfItems=0;
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
                    progress.setVisibility(View.INVISIBLE);
                    //drawerLayout.closeDrawers();
                }
                else if(item.toString().equals("Wishlist"))
                {
                    progress.setVisibility(View.VISIBLE);
                    home.setVisibility(View.INVISIBLE);
                    result.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    wishlist.setVisibility(View.INVISIBLE);
                    wishListMessageView.setVisibility(View.INVISIBLE);
                        populateWishlist(false);


                }
                return true;
            }
        });




    }
    private void populateWishlist(boolean isBack) {

       final boolean[] flag = {false}; //0-> recyclerView  1-> messageView
        final RecyclerView recyclerView =  findViewById(R.id.recyclerViewWishlist);
        lastView=1;
        //wishlist.setVisibility(View.INVISIBLE);
        if(!isBack)
        {
            wishListMessageView.setVisibility(View.VISIBLE);
            wishlistMessage.setText("Loading...");
        }
        else if(numberOfItems==0)
        {
            recyclerView.setVisibility(View.INVISIBLE);
            wishListMessageView.setVisibility(View.VISIBLE);
            wishlistMessage.setText("Your wishlist is empty!");
        }




        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {

            wishlistMessage.setVisibility(View.VISIBLE);
            wishlistMessage.setText("You need to be logged in for this activity!");
            flag[0] =true;


        }
        else{
            //get isbns
            //TODO: FETCH FROM DATABASE

            final List<String> isbns=new ArrayList<>();
            final DatabaseReference refUser=FirebaseDatabase.getInstance().getReference().child("WISHLIST").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    if(dataSnapshot.exists())
                    {
                        flag[0] =false;

                        isbns.clear();
                        for(DataSnapshot isbnSnapshot:dataSnapshot.getChildren())
                        {
                            isbns.add(isbnSnapshot.getKey());
                        }

                        System.out.println(isbns);
                        //fetch books
                        try {
                            List<Book> data = new FetchBooksByIsbn().execute(isbns).get();
                            numberOfItems=data.size();
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                            adapter=new CustomListAdapter(data);
                            recyclerView.setAdapter(adapter);
                            wishListMessageView.setVisibility(View.INVISIBLE);


                        }
                        catch (Exception e)
                        {
                            e.getMessage();
                        }


                    }
                    else
                    {
                        wishlistMessage.setVisibility(View.VISIBLE);
                        wishlistMessage.setText("Your wishlist is empty!");

                        flag[0] =true;

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            refUser.addListenerForSingleValueEvent(eventListener);


        }

        progress.setVisibility(View.INVISIBLE);
        if(flag[0])
            wishListMessageView.setVisibility(View.VISIBLE);
        else
            wishlist.setVisibility(View.VISIBLE);
        System.out.println(flag[0]);

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
        if(lastView==0)
        {
            details.setVisibility(View.INVISIBLE);
            result.setVisibility(View.VISIBLE);

        }
        else if(lastView==1)
        {
            progress.setVisibility(View.VISIBLE);
            home.setVisibility(View.INVISIBLE);
            result.setVisibility(View.INVISIBLE);
            details.setVisibility(View.INVISIBLE);
            wishlist.setVisibility(View.INVISIBLE);
            wishListMessageView.setVisibility(View.INVISIBLE);
            populateWishlist(true);

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
                numberOfItems=0;
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
       progress=(RelativeLayout) findViewById(R.id.Progress);

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
                        numberOfItems--;


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
                        numberOfItems++;

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

        List<Book> books=new ArrayList<>();
        List<String> tmpL=new ArrayList<>();
        tmpL.add("Charlaine Harris");
        Book tmp=new Book("Dead in the family", tmpL,
                "Telepathic waitress Sookie Stackhouse contends with the outcome of the Fae War, her feelings for vampire Eric Northman, and the Shifter community going public.",
                352,"http://books.google.com/books/content?id=KFuLDQAAQBAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api",
                "9780441020157","2011","Penguin");
        books.add(tmp);

        tmpL=new ArrayList<>();
        tmpL.add("W. Bruce Cameron");
        tmp=new Book("A Dog's Purpose", tmpL,
"The phenomenal New York Times Number One bestseller about the unbreakable bond between a dog and their human. Now a major film starring Dennis Quaid. This is the remarkable story of one endearing dog's search for his purpose over the course of several lives. More than just another charming dog story, A Dog's Purpose touches on the universal quest for an answer to life's most basic question: Why are we here? Surprised to find himself reborn as a rambunctious golden-haired puppy after a tragically short life as a stray mutt, Bailey's search for his new life's meaning leads him into the loving arms of eight-year-old Ethan. During their countless adventures, Bailey joyously discovers how to be a good dog. But this life as a family pet is not the end of Bailey's journey. Reborn as a puppy yet again, Bailey wonders – will he ever find his purpose? Heartwarming, insightful, and often laugh-out-loud funny, W. Bruce Cameron's A Dog's Purpose is not only the emotional and hilarious story of a dog's many lives, but also a dog's-eye commentary on human relationships and the unbreakable bonds between man and man's best friend. This moving and beautifully crafted story teaches us that love never dies, and that every creature on earth is born with a purpose.",
                336,"http://books.google.com/books/content?id=5q8xkLvesdEC&printsec=frontcover&img=1&zoom=5&source=gbs_api",
                "9781447213246","2011-12-22","Pan Macmillan");
        books.add(tmp);

        tmpL=new ArrayList<>();
        tmpL.add("Steve Martin");
        tmp=new Book("An Object of Beauty", tmpL,
"Lacey Yeager is young, captivating, and ambitious enough to take the NYC art world by storm. Groomed at Sotheby's and hungry to keep climbing the social and career ladders put before her, Lacey charms men and women, old and young, rich and even richer with her magnetic charisma and liveliness. Her ascension to the highest tiers of the city parallel the soaring heights--and, at times, the dark lows--of the art world and the country from the late 1990s through today.",
                320,"http://books.google.com/books/content?id=_2BzIHoeil8C&printsec=frontcover&img=1&zoom=5&source=gbs_api",
                "9780446573665","2010-11-23","Grand Central Publishing");
        books.add(tmp);

        tmpL=new ArrayList<>();
        tmpL.add("David B. Morris");
        tmp=new Book("The Culture of Pain", tmpL,
"This is a book about the meanings we make out of pain. The greatest surprise I encountered in discussing this topic over the past ten years was the consistency with which I was asked a single unvarying question: Are you writing about physical pain or mental pain? The overwhelming consistency of this response convinces me that modern culture rests upon and underlying belief so strong that it grips us with the force of a founding myth. Call it the Myth of Two Pains. We live in an era when many people believe--as a basic, unexamined foundation of thought--that pain comes divided into separate types: physical and mental. These two types of pain, so the myth goes, are as different as land and sea. You feel physical pain if your arm breaks, and you feel mental pain if your heart breaks. Between these two different events we seem to imagine a gulf so wide and deep that it might as well be filled by a sea that is impossible to navigate.",
                354,"http://books.google.com/books/content?id=PhJ2LOp4BW4C&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api",
                "0520913825","1991-09-09","Univ of California Press");
        books.add(tmp);

        tmpL=new ArrayList<>();
        tmpL.add("Laura M. Ahearn");
        tmp=new Book("Invitations to Love", tmpL,
"A discussion of the implications of the emergence of love-letter correspondences for social relations in Nepal",
                295,"http://books.google.com/books/content?id=VsdAA8fmL88C&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api",
                "0472067842","2001","University of Michigan Press");
        books.add(tmp);

        tmpL=new ArrayList<>();
        tmpL.add("Giora Shaviv");
        tmp=new Book("The Life of Stars", tmpL,
"It is the stars, The stars above us, govern our conditions. William Shakespeare, King Lear A Few Words about What, Why and How The structure of the stars in general, and the Sun in particular, has been the subject of extensivescienti?cresearchanddebateforoveracentury.Thediscoveryofquantum theoryduringthe?rsthalfofthenineteenthcenturyprovidedmuchofthetheoretical background needed to understand the making of the stars and how they live off their energysource. Progress in the theoryof stellar structurewasmade through extensive discussions and controversies between the giants of the ?elds, as well as brilliant discoveries by astronomers. In this book, we shall carefully expose the building of the theory of stellar structure and evolution, and explain how our understanding of the stars has emerged from this background of incessant debate. About hundred years were required for astrophysics to answer the crucial ques tions: What is the energy source of the stars? How are the stars made? How do they evolve and eventually die? The answers to these questions have profound im plications for astrophysics, physics, and biology, and the question of how we our selves come to be here. While we already possess many of the answers, the theory of stellar structure is far from being complete, and there are many open questions, for example, concerning the mechanisms which trigger giant supernova explosions. Many internal hydrodynamic processes remain a mystery. Yet some global pictures can indeed be outlined, and this is what we shall attempt to do here.",
                504,"http://books.google.com/books/content?id=tb5WFYlNbeAC&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api",
                "3642020887","2009-10-03","Springer Science & Business Media");
        books.add(tmp);

        final Book b1=books.get(0);
        Picasso.get().load(b1.getPoster()).into(bsPoster1);
        bsTitle1.setText(b1.getTitle());

        final Book b2=books.get(1);
        Picasso.get().load(b2.getPoster()).into(bsPoster2);
        bsTitle2.setText(b2.getTitle());

        final Book b3=books.get(2);
        Picasso.get().load(b3.getPoster()).into(bsPoster3);
        bsTitle3.setText(b3.getTitle());

        final Book r1=books.get(3);
        Picasso.get().load(r1.getPoster()).into(recPoster1);
        recTitle1.setText(r1.getTitle());

        final Book r2=books.get(4);
        Picasso.get().load(r2.getPoster()).into(recPoster2);
        recTitle2.setText(r2.getTitle());

        final Book r3=books.get(5);
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
}
