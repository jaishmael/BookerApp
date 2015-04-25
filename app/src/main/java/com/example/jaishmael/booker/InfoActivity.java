package com.example.jaishmael.booker;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;


public class InfoActivity extends Activity {
    public static String author;
    ListView mBookList;
    TextView authorText;
    ArrayList<Book> al;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ActionBar actionBar = super.getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayShowHomeEnabled(true);
        ImageView iV = (ImageView) findViewById(R.id.imageView2);
        author = HomeActivity.getAuthor();
        mBookList = (ListView) findViewById(R.id.bookslistView);
        authorText = (TextView) findViewById(R.id.authorTextView);
        authorText.setTypeface(HomeActivity.getFont());
        authorText.setText(author);
        Log.d("***APPANAME:", "" + author);
        String data = "";




        try {
            data = new GetInfo().execute(author).get();
        }
        catch (Exception e){
        }
        bookSearch(data);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void bookSearch(String data) {
        al = new ArrayList<Book>();
        ArrayList tags = new ArrayList();
        String authorname = "",isbn = "",booktitle = "",year = "", cover = "", authorpic = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jb = jsonObject.getJSONArray("docs");
            for (int i = 0; i < jb.length(); i++){
                JSONObject jarr = jb.getJSONObject(i);

                try {
                    booktitle = jarr.getString("title");//Title
                    booktitle = booktitle.replaceAll("\",", "");
                    Log.d("***APPANAME:", "" + booktitle);
                }catch (Exception e){}

                try{
                    cover = jarr.getJSONArray("edition_key").getString(0);
                    cover = cover.replaceAll("\",", "");
                }catch (Exception e){ Log.d("***APPANAME:", "Failed to get Cover for " + booktitle);}

                try{
                    authorpic = jarr.getJSONArray("author_key").getString(0);
                    authorpic = authorpic.replaceAll("\",", "");
                }catch (Exception e){ Log.d("***APPANAME:", "Failed to get authorpic for " + booktitle);}

                try{
                    isbn = jarr.getJSONArray("isbn").getString(0); //ISBN
                    isbn = isbn.replaceAll("\",", "");
                }catch (Exception e){ Log.d("***APPANAME:", "Failed to get ISBN for " + booktitle);}

                try{
                    authorname = jarr.getJSONArray("author_name").getString(0);//Author
                    authorname = authorname.replaceAll("\",", "");
                }catch (Exception e){Log.d("***APPANAME:", "Failed to get Authorname for " + booktitle);}

                try{
                    JSONArray tagsarray = jarr.getJSONArray("subject"); //tags
                    for (int y = 0; y<tagsarray.length(); y++){
                        String tagname = tagsarray.getString(y);
                        tagname = tagname.replaceAll("\",", "");
                        tags.add(tagname);
                    }
                }catch (Exception e){Log.d("***APPANAME:", "Failed to get tags for " + booktitle);}

                try{
                    year = jarr.getString("first_publish_year");
                    year = year.replaceAll("\",", "");
                }catch (Exception e){Log.d("***APPANAME:", "Failed to get year for " + booktitle);}

                Book newbook = new Book(booktitle,authorname,isbn,year, cover,tags);
                if(!al.contains(newbook)) {
                    al.add(newbook);
                }
            }
            new GetAuthorPic().execute(authorpic);

            //updatelist(al);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetAuthorPic extends AsyncTask<String, Void, Bitmap> {
        private Exception e;
        ImageView img = (ImageView)findViewById(R.id.imageView2);

        protected Bitmap doInBackground(String... query) {
            Bitmap bitmap = null;
            try {
                String id = query[0];
                Log.d("***APPANAME:", "Authorpic id:  " + id);
                URL url = new URL ("http://covers.openlibrary.org/a/olid/" + id + "-M.jpg");
                Log.d("***APPANAME:", "Authorpic url:  " + url);
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


    private class GetInfo extends AsyncTask<String, Void, String> {
        private Exception e;

        protected String doInBackground(String... query) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            String search = query[0].replaceAll("\\s+", "%20");
            String request = "http://openlibrary.org/search.json?author=" + search;
            request = request + "&jscmd=data&format=json";


            HttpGet httpGet = new HttpGet(request);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e("APP", "Failed to download file");

                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
}