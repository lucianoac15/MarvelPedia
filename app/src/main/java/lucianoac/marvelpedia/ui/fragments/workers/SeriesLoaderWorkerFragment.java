package lucianoac.marvelpedia.ui.fragments.workers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.karumi.marvelapiclient.MarvelApiException;
import com.karumi.marvelapiclient.SeriesApiClient;
import com.karumi.marvelapiclient.model.MarvelResponse;
import com.karumi.marvelapiclient.model.SeriesCollectionDto;
import com.karumi.marvelapiclient.model.SeriesDto;
import com.karumi.marvelapiclient.model.SeriesQuery;

import java.util.LinkedList;
import java.util.List;

import lucianoac.marvelpedia.MarvelPediaApplication;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;

public class SeriesLoaderWorkerFragment extends AbstractWorkerFragment<SeriesDto> {
    public static final String TAG = SeriesLoaderWorkerFragment.class.getSimpleName();

    private static final String LOG_TAG = TAG;

    private static final String ARG_CHARACTER_ID = "ivamluz.marvelshelf.character_id";

    private long mCharacterId;

    public interface TaskCallbacks {
        void onSeriesLoadingPreExecute();

        void onSeriesLoadingCancelled();

        void onSeriesLoaded(List<SeriesDto> series);
    }

    private TaskCallbacks mListener;
    private LoadSeriesTask mTask;

    public static SeriesLoaderWorkerFragment newInstance(long characterId) {
        SeriesLoaderWorkerFragment fragment = new SeriesLoaderWorkerFragment();

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
            String message = "No series cached. Fetching from API.";
            MarvelPediaLogger.debug(LOG_TAG, message);

            mTask = new LoadSeriesTask();
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            String message = String.format("Found series %s cached. Returning from cache.", mCachedResults.size());
            MarvelPediaLogger.debug(LOG_TAG, message);

            if (mListener != null) {
                mListener.onSeriesLoaded(mCachedResults);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class LoadSeriesTask extends AsyncTask<Void, Integer, List<SeriesDto>> {

        @Override
        protected void onPreExecute() {
            SeriesLoaderWorkerFragment.this.mIsLoading = true;

            if (mListener != null) {
                mListener.onSeriesLoadingPreExecute();
            }
        }

        @Override
        protected List<SeriesDto> doInBackground(Void... ignore) {
            SeriesApiClient seriesApiClient = new SeriesApiClient(MarvelPediaApplication.getInstance().getMarvelApiConfig());
            SeriesQuery query = SeriesQuery.Builder.create().addCharacter((int) mCharacterId).withOffset(0).withLimit(100).build();

            MarvelResponse<SeriesCollectionDto> response = null;
            try {
                response = seriesApiClient.getAll(query);
            } catch (MarvelApiException e) {
                MarvelPediaLogger.error(LOG_TAG, e);
            }

            if (response != null) {
                return response.getResponse().getSeries();
            } else {
                MarvelPediaLogger.debug(LOG_TAG, "response is null");
                return new LinkedList<>();
            }
        }

        @Override
        protected void onCancelled() {
            SeriesLoaderWorkerFragment.this.mIsLoading = false;

            if (mListener != null) {
                mListener.onSeriesLoadingCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<SeriesDto> series) {
            SeriesLoaderWorkerFragment.this.mIsLoading = false;
            SeriesLoaderWorkerFragment.this.mCachedResults = series;

            if (mListener != null) {
                mListener.onSeriesLoaded(series);
            }
        }
    }
}
