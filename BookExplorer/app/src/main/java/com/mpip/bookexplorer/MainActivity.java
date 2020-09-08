package com.mpip.bookexplorer;

import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
    LinearLayout result;
    CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        home.setVisibility(View.VISIBLE);
        result.setVisibility(View.INVISIBLE);

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
        drawerLayout = (DrawerLayout) findViewById(R.id.mDrawerLayout);
        nav = (NavigationView) findViewById(R.id.navView);
        userHeader=(TextView)nav.getHeaderView(0).findViewById(R.id.txtUserEmail);
        displayNameHeader=(TextView) nav.getHeaderView(0).findViewById(R.id.txtUserDisplayName);
        userImage=(ImageView)nav.getHeaderView(0).findViewById(R.id.userImage);


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







}
