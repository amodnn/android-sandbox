package com.example.kyokomi.todoexample;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TodoDetailActivity extends AppCompatActivity {

    @InjectView(R.id.imageButton)
    ImageButton mImageButton;

    @OnClick(R.id.imageButton)
    void showLGTM() {
        LGTMAsyncTask task = new LGTMAsyncTask();
        task.execute();
    }

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

                Picasso.with(TodoDetailActivity.this).load(elements.get(0).val()).into(mImageButton);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_detail, menu);
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
        } else if (id == R.id.action_register) {
            Intent intent = new Intent(this, TodoRegisterActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_list) {
            Intent intent = new Intent(this, TodoListActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
