package com.example.kyokomi.todoexample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    @OnClick(R.id.imageButton)
    void showLGTM() {
        LGTMAsyncTask task = new LGTMAsyncTask();
        task.execute();
    }

    @InjectView(R.id.imageButton)
    ImageView mImageView;

    public class LGTMAsyncTask extends AsyncTask<String, Integer, Document> {
        @Override
        protected Document doInBackground(String... url) {
            Document document = null;
            try {
                document = Jsoup.connect("http://www.lgtm.in/g").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            if (document != null) {
                Elements elements = document.select("#imageUrl");
                Log.d("", elements.get(0).val());

                Picasso.with(MainActivity.this).load(elements.get(0).val()).into(mImageView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

//        Request request = new Request.Builder()
//                .url("http://www.lgtm.in/g")
//                .get()
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                Log.e("", e.getLocalizedMessage(), e);
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                String htmlResponse = response.body().string();
//
//                Log.d("", htmlResponse);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
