package com.example.kyokomi.todoexample;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TodoContentProvider extends ContentProvider {
    public TodoContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        checkUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int count = db.delete(uri.getPathSegments().get(0), appendSelection(uri, selection),
                appendSelectionArgs(uri, selectionArgs));
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        final int code = sUriMatcher.match(uri);
        for (final Contract contract : Contract.values()) {
            if (code == contract.allCode) {
                return contract.mimeTypeForMany;
            } else if (code == contract.byIdCode) {
                return contract.mimeTypeForOne;
            }
        }
        throw new IllegalArgumentException("unknown uri : " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        checkUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insertOrThrow(uri.getPathSegments().get(0), null, values);
        Uri returnUri = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(returnUri, null);
        return returnUri;
    }

    /**
     * URIのauthority.
     */
    private static final String AUTHORITY = "com.kyokomi.example.todoexample";

    /**
     * SQLiteデータベースのファイル名.
     */
    private static final String SQLITE_FILENAME = "lgtmtodo.sqlite";

    /**
     * SQLiteOpenHelperのインスタンス.
     */
    private SQLite mOpenHelper;

    public enum Contract {
        /**
         * TABLE1テーブル.
         */
        TODO_TABLE(BaseColumns._ID, "title", "detail");

        Contract(final String... columns) {
            this.columns = Collections.unmodifiableList(Arrays.asList(columns));
        }

        /**
         * テーブル名. enum定数を小文字にしたものとする.
         */
        private final String tableName = name().toLowerCase();

        /**
         * テーブル全体のデータに対して処理をしに行く時のコード.
         */
        private final int allCode = ordinal() * 10;

        /**
         * 対象IDのデータに対して処理をしに行く時のコード.
         */
        private final int byIdCode = ordinal() * 10 + 1;

        /**
         * そのテーブル固有のCONTENT_URI表現. コンテンツリゾルバからこれを使用してアクセスする.
         */
        public final Uri contentUri = Uri.parse("content://" + AUTHORITY + "/" + tableName);

        /**
         * MIMEタイプ（単数）.
         */
        public final String mimeTypeForOne = "vnd.android.cursor.item/vnd.kyokomi." + tableName;

        /**
         * MIMEタイプ（複数）.
         */
        public final String mimeTypeForMany = "vnd.android.cursor.dir/vnd.kyokomi." + tableName;

        /**
         * カラムのリスト.
         */
        public final List<String> columns;
    }

    @Override
    public boolean onCreate() {
        final int version;
        try {
            version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        mOpenHelper = new SQLite(getContext(), SQLITE_FILENAME, null, version);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        checkUri(uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(uri.getPathSegments().get(0), projection, appendSelection(uri, selection),
                appendSelectionArgs(uri, selectionArgs), null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private String appendSelection(Uri uri, String selection) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 1) {
            return selection;
        }
        return BaseColumns._ID + " = ?" + (selection == null ? "" : " AND (" + selection + ")");
    }

    /**
     * Uriで_idの指定があった場合, selectionArgsにそれを連結して返す.
     *
     * @param uri           Uri
     * @param selectionArgs 絞り込み条件の引数
     * @return _idの条件が連結されたselectionArgs
     */
    private String[] appendSelectionArgs(Uri uri, String[] selectionArgs) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 1) {
            return selectionArgs;
        }
        if (selectionArgs == null || selectionArgs.length == 0) {
            return new String[]{pathSegments.get(1)};
        }
        String[] returnArgs = new String[selectionArgs.length + 1];
        returnArgs[0] = pathSegments.get(1);
        System.arraycopy(selectionArgs, 0, returnArgs, 1, selectionArgs.length);
        return returnArgs;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        checkUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int count = db.update(uri.getPathSegments().get(0), values, appendSelection(uri, selection),
                appendSelectionArgs(uri, selectionArgs));
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, Contract.TODO_TABLE.tableName, Contract.TODO_TABLE.allCode);
        sUriMatcher.addURI(AUTHORITY, Contract.TODO_TABLE.tableName + "/#", Contract.TODO_TABLE.byIdCode);
    }

    /**
     * 対象Uriがこのコンテンツプロバイダで扱えるUriパターンかどうかを検証する.
     *
     * @throws IllegalArgumentException このコンテンツプロバイダで扱えるUriパターンでなかった場合
     */
    private void checkUri(Uri uri) {
        final int code = sUriMatcher.match(uri);
        for (final Contract contract : Contract.values()) {
            if (code == contract.allCode) {
                return;
            } else if (code == contract.byIdCode) {
                return;
            }
        }
        throw new IllegalArgumentException("unknown uri : " + uri);
    }

    /**
     * SQLiteを扱うクラス. ContentProvider内で使用されるに留まる.
     *
     * @author Hideyuki Kojima
     */
    private static class SQLite extends SQLiteOpenHelper {

        /**
         * コンストラクタ.
         *
         * @param context コンテキスト
         * @param name    SQLiteファイル名
         * @param factory CursorFactory
         * @param version DBバージョン
         */
        public SQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE todo_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, detail TEXT)");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: 本来は移行用のコードを書く
        }
    }
}
