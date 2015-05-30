package com.example.kyokomi.todoexample;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TodoDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @InjectView(R.id.detailList)
    ListView mDetailView;

    SimpleCursorAdapter mAdapter;

    @InjectView(R.id.imageButton)
    ImageButton mImageButton;

    @OnClick(R.id.imageButton)
    void showLGTM() {
        LGTMAsyncTask task = new LGTMAsyncTask();
        task.execute();
    }

    @OnClick(R.id.deletedButton)
    void onClickDeletedButton() {
        // TODO: 確認ダイアログ出すとかしたほうが？そもそもリスト表示のほうでやりたさ
//        getContentResolver().delete(TodoContentProvider.Contract.TODO_TABLE.contentUri, "id", new String[]{"", ""});
        Toast.makeText(this, "削除した", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.completedButton)
    void onClickCompletedButton() {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(TodoContentProvider.Contract.TODO_DETAIL_TABLE.columns.get(1), 1); // TODO: あとで
        values.put(TodoContentProvider.Contract.TODO_DETAIL_TABLE.columns.get(2), new Date().getTime());
        values.put(TodoContentProvider.Contract.TODO_DETAIL_TABLE.columns.get(3), "lgtm");
        getContentResolver().insert(TodoContentProvider.Contract.TODO_DETAIL_TABLE.contentUri, values);

        Toast.makeText(this, "達成した", Toast.LENGTH_SHORT).show();
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);
        ButterKnife.inject(this);

        initLoader();
    }

    private void initLoader() {
        final String[] from = {"lgtm"};
        final int[] to = {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);
        mDetailView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(getClass().getSimpleName(), "onCreateLoader called.");
        // TODO: あとで
//        return new CursorLoader(this, TodoContentProvider.Contract.TODO_DETAIL_TABLE.contentUri, null, BaseColumns._ID, new String[]{"1"}, null);
        return new CursorLoader(this, TodoContentProvider.Contract.TODO_DETAIL_TABLE.contentUri, null, null, null, null);
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
