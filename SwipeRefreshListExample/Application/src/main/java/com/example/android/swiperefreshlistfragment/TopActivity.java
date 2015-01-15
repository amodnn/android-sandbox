package com.example.android.swiperefreshlistfragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kyokomi on 15/01/15.
 */
public class TopActivity extends Activity {
    private static final String TAG = TopActivity.class.getSimpleName();

    // TODO: 正式版で出すときはPRODUCTIONにする
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    protected EvernoteSession mEvernoteSession;

    @InjectView(R.id.listView1)
    ListView mListView1;
    @InjectView(R.id.listView2)
    ListView mListView2;
    @InjectView(R.id.listView3)
    ListView mListView3;
    @InjectView(R.id.listView4)
    ListView mListView4;

    Map<String, ListView> mBookMap;

    // TODO: あとで専用のListAdapterにする
    private <T> ArrayAdapter<T> createListAdapter(T[] noteNames) {
        return new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                noteNames);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ButterKnife.inject(this);

        mBookMap = new HashMap<>();
        mBookMap.put("backlog", mListView1);
        mBookMap.put("todo", mListView2);
        mBookMap.put("in progress", mListView3);
        mBookMap.put("done", mListView4);

        //Set up the Evernote Singleton Session
        mEvernoteSession = EvernoteSession.getInstance(this,
                getString(R.string.consumer_key),
                getString(R.string.consumer_secret),
                EVERNOTE_SERVICE, false);

        // TODO: とりあえず
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                getResources().getStringArray(R.array.esdk__main_list));
        mListView1.setAdapter(mAdapter);
        mListView2.setAdapter(mAdapter);
        mListView3.setAdapter(mAdapter);
        mListView4.setAdapter(mAdapter);

        if (!mEvernoteSession.isLoggedIn()) {
            return;
        }

        try {
            hoge();
        } catch (TTransportException e) {
            Log.e(TAG, "Error notebooks", e);
        }
    }

    public interface FindNoteListener {
        void Success(List<CharSequence> noteNameList);
    }

    private void hoge() throws TTransportException {
        mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
            @Override
            public void onSuccess(final List<Notebook> notebooks) {
                for (final Notebook notebook : notebooks) {
                    if (!mBookMap.containsKey(notebook.getName())) {
                        continue;
                    }

                    findNotes(notebook, new FindNoteListener() {
                        @Override
                        public void Success(List<CharSequence> noteNameList) {
                            mBookMap.get(notebook.getName()).setAdapter(createListAdapter(noteNameList.toArray()));
                        }
                    });
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e(TAG, "Error listing notebooks", exception);
                Toast.makeText(getApplicationContext(), R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
                //                removeDialog(DIALOG_PROGRESS);
            }
        });
    }

    private void findNotes(Notebook notebook, final FindNoteListener findNoteListener) {
        AsyncNoteStoreClient storeClient = null;
        try {
            storeClient = mEvernoteSession.getClientFactory().createNoteStoreClient();
        } catch (TTransportException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        if (storeClient == null) {
            return;
        }

        NoteFilter filter = new NoteFilter();
        filter.setNotebookGuid(notebook.getGuid());

        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);
        storeClient.findNotesMetadata(filter, 0, 10, spec, new OnClientCallback<NotesMetadataList>() {
            @Override
            public void onSuccess(NotesMetadataList data) {
                List<CharSequence> titleList = Lists.transform(data.getNotes(), new Function<NoteMetadata, CharSequence>() {
                    @Override
                    public CharSequence apply(NoteMetadata input) {
                        return input.getTitle();
                    }
                });

                findNoteListener.Success(titleList);
            }

            @Override
            public void onException(Exception exception) {
                Log.e(TAG, "Error listing notebooks", exception);
                Toast.makeText(getApplicationContext(), R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUi();
    }

    @OnClick(R.id.login)
    void login() {
        mEvernoteSession.authenticate(this);
    }

    @OnClick(R.id.logout)
    void logout() {
        try {
            mEvernoteSession.logOut(this);
        } catch (InvalidAuthenticationException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        updateAuthUi();
    }

    /**
     * Update the UI based on Evernote authentication state.
     */
    private void updateAuthUi() {
        //show login button if logged out
        Button loginButton = ButterKnife.findById(this, R.id.login);
        loginButton.setEnabled(!mEvernoteSession.isLoggedIn());

        //Show logout button if logged in
        Button logoutButton = ButterKnife.findById(this, R.id.logout);
        logoutButton.setEnabled(mEvernoteSession.isLoggedIn());

        //disable clickable elements until logged in
//        mListView.setEnabled(mEvernoteSession.isLoggedIn());
    }

    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    updateAuthUi();
                }
                break;
        }
    }
}
