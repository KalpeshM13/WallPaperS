package com.kalpesh.wallpapers;

import android.Manifest;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kalpesh.wallpapers.Models.Photo;
import com.squareup.picasso.Picasso;

public class WallPaperActivity extends AppCompatActivity {

    ImageView imageView_wallpaper;
    FloatingActionButton fab_download,fab_share;
    Button fab_wallpaper;
    Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_paper);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.purple_700)));
        getSupportActionBar().setTitle("Specific Wallpaper");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab_download = findViewById(R.id.fab_download);
        fab_wallpaper = findViewById(R.id.fab_wallpaper);
        fab_share = findViewById(R.id.fab_share);
        imageView_wallpaper = findViewById(R.id.imageView_wallpaper);

        photo = (Photo) getIntent().getSerializableExtra("photo");
        Picasso.get().load(photo.getSrc().getOriginal()).into(imageView_wallpaper);

        fab_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadWallpaper();
            }
        });

        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWallpaper();
            }
        });

        fab_wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(WallPaperActivity.this);
                Bitmap bitmap = ((BitmapDrawable) imageView_wallpaper.getDrawable()).getBitmap();

                try{
                    wallpaperManager.setBitmap(bitmap);
                    Toast.makeText(WallPaperActivity.this, "Wallpaper Set!", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(WallPaperActivity.this, "Couldn't set Wallpaper", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void shareWallpaper() {
        BitmapDrawable drawable = (BitmapDrawable) imageView_wallpaper.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"WallpaperS" + photo.getPhotographer(),null);
        Uri uri = Uri.parse(bitmapPath);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,"Got this Wallpaper from WallpaperS");
        Intent.createChooser(intent,"Share using...");
        startActivity(intent);

    }

    private void downloadWallpaper() {
        if (ContextCompat.checkSelfPermission(WallPaperActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(WallPaperActivity.this, "Permission already granted!", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = null;
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            Uri uri = Uri.parse(photo.getSrc().getLarge());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("WallpaperS_" + photo.getPhotographer())
                    .setMimeType("image/jpeg")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "WallpaperS" + photo.getPhotographer() + ".jpg");

            downloadManager.enqueue(request);

            Toast.makeText(WallPaperActivity.this, "Downloading Started", Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Storage permission is required to Download Wallpaper")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(WallPaperActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WallPaperActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                downloadWallpaper();
            } else {
                Toast.makeText(WallPaperActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();

            }
        }
    }

}
