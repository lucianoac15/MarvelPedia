package lucianoac.marvelpedia.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import lucianoac.marvelpedia.data.MPContract;
import lucianoac.marvelpedia.data.model.Character;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;

public class FavoritesManager {
    private static final String LOG_TAG = FavoritesManager.class.getSimpleName();

    private Context mContext;
    private BookmarkCallbacks mBookmarkCallbacks;

    public FavoritesManager(Context context, BookmarkCallbacks bookmarkCallbacks) {
        mContext = context;
        mBookmarkCallbacks = bookmarkCallbacks;
    }

    public void toggleBookmark(final long characterId) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Uri uri = MPContract.CharacterEntry.buildCharacterUri(characterId);

                Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();

                Character character = Character.fromCursor(cursor);
                int newIsBookmarked = character.isBookmarked() ? 0 : 1;

                ContentValues updateValues = new ContentValues();
                updateValues.put(MPContract.CharacterEntry.COLUMN_IS_BOOKMARK, newIsBookmarked);

                String selectionClause = String.format("%s = ?", MPContract.CharacterEntry.COLUMN_CHARACTER_ID);
                String[] selectionArgs = {String.valueOf(characterId)};

                int rowsUpdated = mContext.getContentResolver().update(
                        MPContract.CharacterEntry.CONTENT_URI,
                        updateValues,
                        selectionClause,
                        selectionArgs
                );

                if (rowsUpdated > 0) {
                    MarvelPediaLogger.debug(LOG_TAG, String.format("Character %s updated with is_bookmarked = %s", character.getId(), newIsBookmarked));

                    return (newIsBookmarked == 1);
                } else {
                    MarvelPediaLogger.debug(LOG_TAG, String.format("Character %s was not updated. is_bookmarked is still %s", character.getId(), character.isBookmarked()));

                    return character.isBookmarked();
                }
            }

            @Override
            protected void onPostExecute(Boolean isBookmarked) {
                super.onPostExecute(isBookmarked);

                if (mBookmarkCallbacks != null) {
                    mBookmarkCallbacks.onBookmarkToogled(isBookmarked);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface BookmarkCallbacks {
        void onBookmarkToogled(boolean isBookmarked);
    }
}
