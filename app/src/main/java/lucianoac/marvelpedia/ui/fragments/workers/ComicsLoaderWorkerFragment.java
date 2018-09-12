package lucianoac.marvelpedia.ui.fragments.workers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.karumi.marvelapiclient.ComicApiClient;
import com.karumi.marvelapiclient.MarvelApiException;
import com.karumi.marvelapiclient.model.ComicDto;
import com.karumi.marvelapiclient.model.ComicsDto;
import com.karumi.marvelapiclient.model.ComicsQuery;
import com.karumi.marvelapiclient.model.MarvelResponse;

import java.util.LinkedList;
import java.util.List;

import lucianoac.marvelpedia.BuildConfig;
import lucianoac.marvelpedia.MarvelPediaApplication;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;

public class ComicsLoaderWorkerFragment extends AbstractWorkerFragment<ComicDto> {
    public static final String TAG = ComicsLoaderWorkerFragment.class.getSimpleName();

    private static final String LOG_TAG = TAG;

    private static final String ARG_CHARACTER_ID = String.format(".character_id", BuildConfig.APPLICATION_ID);

    private long mCharacterId;

    public interface TaskCallbacks {
        void onComicsLoadingPreExecute();

        void onComicsLoadingCancelled();

        void onComicsLoaded(List<ComicDto> comics);
    }

    private TaskCallbacks mListener;
    private LoadComicsTask mTask;

    public static ComicsLoaderWorkerFragment newInstance(long characterId) {
        ComicsLoaderWorkerFragment fragment = new ComicsLoaderWorkerFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_CHARACTER_ID, characterId);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (TaskCallbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCharacterId = getArguments().getLong(ARG_CHARACTER_ID);
        }

        load();
    }

    @Override
    public void load() {
        super.load();

        if (mCachedResults == null || mCachedResults.isEmpty()) {
            String message = "No comics cached. Fetching from API.";
            MarvelPediaLogger.debug(LOG_TAG, message);

            mTask = new LoadComicsTask();
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            String message = String.format("Found comics %s cached. Returning from cache.", mCachedResults.size());
            MarvelPediaLogger.debug(LOG_TAG, message);

            if (mListener != null) {
                mListener.onComicsLoaded(mCachedResults);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class LoadComicsTask extends AsyncTask<Void, Integer, List<ComicDto>> {

        @Override
        protected void onPreExecute() {
            ComicsLoaderWorkerFragment.this.mIsLoading = true;

            if (mListener != null) {
                mListener.onComicsLoadingPreExecute();
            }
        }

        @Override
        protected List<ComicDto> doInBackground(Void... ignore) {
            ComicApiClient comicApiClient = new ComicApiClient(MarvelPediaApplication.getInstance().getMarvelApiConfig());
            ComicsQuery query = ComicsQuery.Builder.create().addCharacter((int) mCharacterId).withOffset(0).withLimit(20).build();

            MarvelResponse<ComicsDto> response = null;
            try {
                response = comicApiClient.getAll(query);
            } catch (MarvelApiException e) {
                MarvelPediaLogger.error(LOG_TAG, e);
            }

            if (response != null) {
                return response.getResponse().getComics();
            } else {
                MarvelPediaLogger.debug(LOG_TAG, "response is null");
                return new LinkedList<>();
            }
        }

        @Override
        protected void onCancelled() {
            ComicsLoaderWorkerFragment.this.mIsLoading = false;

            if (mListener != null) {
                mListener.onComicsLoadingCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<ComicDto> comics) {
            ComicsLoaderWorkerFragment.this.mIsLoading = false;
            ComicsLoaderWorkerFragment.this.mCachedResults = comics;

            if (mListener != null) {
                mListener.onComicsLoaded(comics);
            }
        }
    }
}
