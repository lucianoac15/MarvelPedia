package lucianoac.marvelpedia.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MPProvider extends ContentProvider {
    private static final String LOG_TAG = MPProvider.class.getSimpleName();

    private static final int CHARACTER = 100;
    private static final int CHARACTER_ID = 110;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private MPDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new MPDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CHARACTER:
                cursor = createAllCharactersQueryCursor(projection, selection, selectionArgs, sortOrder, db);
                break;
            case CHARACTER_ID:
                long _id = ContentUris.parseId(uri);
                cursor = createCharacterByIdQueryCursor(projection, sortOrder, db, _id);
                break;
            default:
                throwErrorForInvalidUri(uri);
                return null;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor createAllCharactersQueryCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, SQLiteDatabase db) {
        return db.query(
                MPContract.CharacterEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor createCharacterByIdQueryCursor(String[] projection, String sortOrder, SQLiteDatabase db, long _id) {
        return db.query(
                MPContract.CharacterEntry.TABLE_NAME,
                projection,
                MPContract.CharacterEntry._ID + " = ?",
                new String[]{String.valueOf(_id)},
                null,
                null,
                sortOrder
        );
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CHARACTER:
                return MPContract.CharacterEntry.CONTENT_TYPE;
            case CHARACTER_ID:
                return MPContract.CharacterEntry.CONTENT_ITEM_TYPE;
            default:
                throwErrorForInvalidUri(uri);
                return null;
        }
    }

    private void throwErrorForInvalidUri(Uri uri) {
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }


    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = 0;

        switch (sUriMatcher.match(uri)) {
            case CHARACTER:
                rowsUpdated = db.update(MPContract.CharacterEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
                break;
            default:
                throwErrorForInvalidUri(uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {
        String content = MPContract.CONTENT_AUTHORITY;

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(content, MPContract.PATH_CHARACTER, CHARACTER);
        matcher.addURI(content, MPContract.PATH_CHARACTER + "/#", CHARACTER_ID);

        return matcher;
    }
}
