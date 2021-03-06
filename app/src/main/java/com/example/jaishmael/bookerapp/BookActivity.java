package com.example.jaishmael.bookerapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class BookActivity extends Activity {
    public static String title;
    public static String author;
    public static String isbn;
    public static String year;
    public static String coverid;
    public static ArrayList tags;
    public static Book book;
    TextView authorTextView;
    TextView titleTextView;
    TextView isbnTextView;
    TextView yearTextView;
    TextView descTextView;
    Button amazon;
    Button home;
    Button more;
    myDBHandler mDBHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        setBookInfo();
        ActionBar actionBar = super.getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayShowHomeEnabled(true);
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void setBookInfo(){
        book = InfoActivity.getBook();
        title = book.getTitle();
        author = book.getAuthor();
        isbn = book.getIsbn();
        year = book.getYear();
        coverid = book.getCoverid();
        tags = book.getTags();
        Object tag2 = null;
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        authorTextView = (TextView) findViewById(R.id.authorNameTextView);
        authorTextView.setText(author);
        yearTextView = (TextView) findViewById(R.id.textViewYear);
        yearTextView.setText("Year: "+ year);
        isbnTextView = (TextView) findViewById(R.id.isbnTextView);
        isbnTextView.setText("  ISBN: "+ isbn);
        descTextView = (TextView) findViewById(R.id.descTextView);
        String desc = "Tags: ";
        if (tags.size() != 0) {
            tag2 = tags.get(0);
            String tag2Name = tag2.toString();
            desc = "Tags: ";
            desc += tag2Name;
            for (int i = 0; i < tags.size(); i++) {
                Object tag = tags.get(i);
                String tagName = tag.toString();
                desc += ", " + tagName;
            }
        }
        desc += ".";
        descTextView.setText(desc);
        try {
            new GetCoverPic().execute(coverid);
        }
        catch (Exception e){}
        amazon = (Button) findViewById(R.id.amazonButton);
        amazon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://play.google.com/store/search?q="+title+"&c=books&hl=en";
                Uri uriUrl = Uri.parse(url);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        more = (Button) findViewById(R.id.moreByAuthor);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookActivity.this, InfoActivity.class);
                intent.putExtra("query", author);
                BookActivity.this.startActivity(intent);
            }
        });
        home = (Button) findViewById(R.id.homeButton);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookActivity.this, HomeActivity.class);
                //intent.putExtra("query", author);
                BookActivity.this.startActivity(intent);
            }
        });
    }

    private class GetCoverPic extends AsyncTask<String, Void, Bitmap> {
        private Exception e;
        ImageView img = (ImageView)findViewById(R.id.imageViewCover);

        protected Bitmap doInBackground(String... query) {
            Bitmap bitmap = null;
            try {
                String id = query[0];
                URL url = new URL ("http://covers.openlibrary.org/b/olid/" + id + "-M.jpg");
                HttpGet httpRequest = null;
                httpRequest = new HttpGet((url.toURI()));
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
                HttpEntity entity = response.getEntity();
                BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                InputStream input = b_entity.getContent();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bm) {
            img.setImageBitmap(bm);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
}

