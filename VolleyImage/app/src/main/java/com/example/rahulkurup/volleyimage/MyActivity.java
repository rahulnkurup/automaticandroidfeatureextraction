package com.example.rahulkurup.volleyimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MyActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private TextView tv;
    private TextView tv3;
    private Button button;
    private PlayStoreResources googlePlayUrls = new PlayStoreResources();
    String mPath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/";
    private String fileName = "screenshot";
    private ProgressBar progressBar;
    private List<String> imageUrlList = new ArrayList<String>();
    private List<String>appNameList = new ArrayList<String>();
    private String url;
    private StringRequest req;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        button = (Button) findViewById(R.id.button);
        tv = (TextView) findViewById(R.id.textView);
        tv3 = (TextView) findViewById(R.id.textView3);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        List<String> stringList = googlePlayUrls.getUrls();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.google_play_apps_urls, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                req = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String data) {

                                Document doc = Jsoup.parse(data);
                                progressBar.setVisibility(View.VISIBLE);
                                progressBar.animate();
                                Elements images = doc.getElementsByAttributeValueContaining("class", "full-screenshot");
                                for (Element image : images) {
                                    try {

                                        String src = image.attr("src");
                                        Log.i("imageurl", src);
                                        imageUrlList.add(src);
                                        new ImageDownloader().execute();
                                    } catch (Exception e) {
                                        tv.setText("Error in reading & storing images: " + e.getMessage());
                                    }


                                }

                            }
                        }

                        ,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                tv.setText("Something Wrong with Volley.");
                            }
                        }
                );
                VolleySingleton.getInstance(MyActivity.this).addToRequestQueue(req);

            }

        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

       tv.setText("");
       url = parent.getItemAtPosition(position).toString();
       String[] spinnerUrl = url.split("id=",2);
        Toast.makeText(MyActivity.this,
                spinnerUrl[1], Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class ImageDownloader extends AsyncTask<Void, Void, Void> {

        public void onSelfDestruct()
        {
            cancel(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            downloadBitmap();
            if (isCancelled())
                try{
                onSelfDestruct();}
                catch(Exception E)
                {
                    finish();
                }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Async-Example", "onPreExecute Called");



        }

        @Override
        protected void onPostExecute(Void voids) {
            Log.i("Async-Example", "onPostExecute Called");
            progressBar.setVisibility(View.INVISIBLE);
            tv.setText("The process has now completed. Check for the folder on storage.");
            super.onPostExecute(voids);

        }
    }


    private void downloadBitmap() {

        String[] pieceName = url.split("id=", 2);
        String name = pieceName[1];
            File screenShotDir = new File(mPath + "/AppStoreCaptures/" + name + "/");
            if (!screenShotDir.exists()) {
                screenShotDir.mkdirs();
            }
            int i = 0;
            for (i = 0; i < 5; i++) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet getRequest = new HttpGet(imageUrlList.get(i).toString());
                try {

                    HttpResponse response = client.execute(getRequest);
                    Log.i(url, imageUrlList.get(i).toString());

                    final int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != HttpStatus.SC_OK) {
                        Log.w("ImageDownloader", "Error " + statusCode +
                                " while retrieving bitmap for " + fileName);

                    }

                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream inputStream = null;

                        try {
                            // getting contents from the stream
                            inputStream = entity.getContent();

                            // decoding stream data back into image Bitmap that android understands
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            File screenShot = new File(screenShotDir, fileName + "_" + i + ".jpg");
                            screenShot.createNewFile();
                            FileOutputStream out = new FileOutputStream(screenShot);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                            out.flush();
                            out.close();


                        } finally {
                            if (inputStream != null) {
                                inputStream.close();
                            }


                        }
                    }
                } catch (Exception e) {
                    // You Could provide a more explicit error message for IOException
                    getRequest.abort();
                    Log.e("ImageDownloader", "Something went wrong while" +
                            " retrieving bitmap from " + imageUrlList.get(i).toString() + e.toString());
                }


            }
        }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
