package com.example.kyokomi.todoexample;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TodoRegisterActivity extends AppCompatActivity {

    @InjectView(R.id.titleText)
    TextView mTitleTextView;

    @InjectView(R.id.detailText)
    TextView mDetailTextView;

    @OnClick(R.id.registerButton)
    void registerTodo() {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(TodoContentProvider.Contract.TODO_TABLE.columns.get(1), mTitleTextView.getText().toString());
        values.put(TodoContentProvider.Contract.TODO_TABLE.columns.get(2), mDetailTextView.getText().toString());
        getContentResolver().insert(TodoContentProvider.Contract.TODO_TABLE.contentUri, values);

        Toast.makeText(this, "登録成功", Toast.LENGTH_SHORT).show();

        // TODO: とりあえずList画面飛ぶ
        Intent intent = new Intent(this, TodoListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_register);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_register, menu);
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
        } else if (id == R.id.action_list) {
            Intent intent = new Intent(this, TodoListActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
