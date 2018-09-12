package lucianoac.marvelpedia.ui.fragments.workers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.LinkedList;
import java.util.List;

public class AbstractWorkerFragment<T> extends Fragment {
    protected boolean mIsLoading = false;

    protected List<T> mCachedResults;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mCachedResults = new LinkedList<>();
    }

    public void load() {
        if (isLoading()) {
            String message = String.format("%s is already loading contents.", this.getClass().getSimpleName());
            throw new IllegalStateException(message);
        }
    }

    public boolean hasCachedResults() {
        return (mCachedResults != null && !mCachedResults.isEmpty());
    }

    public List<T> getCachedResults() {
        return mCachedResults;
    }

    public boolean isLoading() {
        return mIsLoading;
    }
}
