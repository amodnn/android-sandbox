package com.example.kyokomi.todoexample;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @OnClick(R.id.button)
    void addTodo() {
        // insert
        ContentValues values = new ContentValues();
        values.clear();
        values.put(TodoContentProvider.Contract.TODO_TABLE.columns.get(1), "hoge");
        values.put(TodoContentProvider.Contract.TODO_TABLE.columns.get(2), "fuga");
        getContentResolver().insert(TodoContentProvider.Contract.TODO_TABLE.contentUri, values);

        refreshList();
    }

    void refreshList() {

    }

    @OnClick(R.id.imageButton)
    void showLGTM() {
        LGTMAsyncTask task = new LGTMAsyncTask();
        task.execute();
    }

    @InjectView(R.id.listView)
    ListView mTodoListView;
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

    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        final String[] from = {"title"};
        final int[] to = {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);
        mTodoListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(getClass().getSimpleName(), "onCreateLoader called.");
        return new CursorLoader(this, TodoContentProvider.Contract.TODO_TABLE.contentUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        Log.d(getClass().getSimpleName(), "onLoadFinished called.");
        mAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(getClass().getSimpleName(), "onLoaderReset called.");
        mAdapter.swapCursor(null);
    }
}
