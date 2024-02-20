package com.kalpesh.wallpapers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kalpesh.wallpapers.Adapters.CuratedAdapter;
import com.kalpesh.wallpapers.Listeners.CuratedResponseListener;
import com.kalpesh.wallpapers.Listeners.OnRecyclerClickedListener;
import com.kalpesh.wallpapers.Listeners.SearchResponseListener;
import com.kalpesh.wallpapers.Models.CuratedApiResponse;
import com.kalpesh.wallpapers.Models.Photo;
import com.kalpesh.wallpapers.Models.SearchApiResponse;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnRecyclerClickedListener {

    RecyclerView recyclerView_home;
    CuratedAdapter adapter;
    ProgressDialog dialog;
    RequestManager manager;
    FloatingActionButton fab_next,fab_prev,fab_prev2;
    int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.purple_700)));

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");

        manager = new RequestManager(this);
        manager.getCuratedWallpapers(listener,"1");

        fab_next = findViewById(R.id.fab_next);
        fab_prev = findViewById(R.id.fab_prev);
        fab_prev2 = findViewById(R.id.fab_prev2);

        fab_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_prev2.setVisibility(View.INVISIBLE);
                String next_page = String.valueOf(page+1);
                manager.getCuratedWallpapers(listener,next_page);
                dialog.show();
            }
        });

        fab_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page>1){
                    fab_prev2.setVisibility(View.INVISIBLE);
                    String prev_page = String.valueOf(page-1);
                    manager.getCuratedWallpapers(listener,prev_page);
                    dialog.show();

                    if (page == 1){
                        fab_prev2.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        fab_prev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "This is first page! You can't go on previous page", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart",true);
        if (firstStart){
            showStartDialog();
        }
    }

    private void showStartDialog(){
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Important Message")
                .setMessage("Wallpapers in this app are getting from pexels.com \nApp version 1.2.2")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart",false);
        editor.apply();
    }

    private final CuratedResponseListener listener = new CuratedResponseListener() {
        @Override
        public void onFetch(CuratedApiResponse response, String message) {
            dialog.dismiss();
            if (response.getPhotos().isEmpty()){
                Toast.makeText(MainActivity.this, "Image Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
            page = response.getPage();
            showData(response.getPhotos());
        }

        @Override
        public void onError(String message) {
            dialog.dismiss();
            Toast.makeText(MainActivity.this,"No internet connection!", Toast.LENGTH_SHORT).show();
        }
    };

    private void showData(List<Photo> photos) {
        recyclerView_home = findViewById(R.id.recyclerView_home);
        recyclerView_home.setHasFixedSize(true);
        recyclerView_home.setLayoutManager(new GridLayoutManager(this,3));

        adapter = new CuratedAdapter(MainActivity.this, photos,this);
        recyclerView_home.setAdapter(adapter);
    }

    @Override
    public void onClick(Photo photo) {
        startActivity(new Intent(MainActivity.this,WallPaperActivity.class)
        .putExtra("photo",photo));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Wallpapers");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                manager.searchCuratedWallpapers(searchResponseListener,"1", query);
                dialog.show();
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    private final SearchResponseListener searchResponseListener = new SearchResponseListener() {
        @Override
        public void onFetch(SearchApiResponse response, String message) {
            dialog.dismiss();
            if (response.getPhotos().isEmpty()){
                Toast.makeText(MainActivity.this, "Image Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
            showData(response.getPhotos());
        }

        @Override
        public void onError(String message) {
            dialog.dismiss();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };
}