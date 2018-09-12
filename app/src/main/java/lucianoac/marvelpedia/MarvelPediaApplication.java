package lucianoac.marvelpedia;

import android.app.Application;
import android.net.Uri;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.karumi.marvelapiclient.MarvelApiConfig;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import java.util.concurrent.TimeUnit;

import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;

public class MarvelPediaApplication extends Application {

    private Tracker mTracker;
    protected MarvelApiConfig mMarvelApiConfig;
    private Picasso mPicasso;
    private static final String LOG_TAG = MarvelPediaApplication.class.getSimpleName();
    private static MarvelPediaApplication sInstance;
    private static final int SIZE_10_MB = 10 * 1024 * 1024;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }


    public static MarvelPediaApplication getInstance() {
        MarvelPediaLogger.debug(LOG_TAG, "getInstance() called. Returning " + sInstance);
        return sInstance;
    }

    public Picasso getPicasso() {
        if (mPicasso != null) {
            return mPicasso;
        }

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                MarvelPediaLogger.error(LOG_TAG, exception);
            }
        });

        mPicasso = builder.build();
        return mPicasso;
    }

    public MarvelApiConfig getMarvelApiConfig() {
        if (mMarvelApiConfig != null) {
            return mMarvelApiConfig;
        }

        mMarvelApiConfig = new MarvelApiConfig.Builder(BuildConfig.MARVEL_API_PUBLIC_KEY, BuildConfig.MARVEL_API_PRIVATE_KEY).build();
        configureMarvelApiHttpClient();

        return mMarvelApiConfig;
    }


    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(BuildConfig.ANALYTICS_TRACKING_ID);
        }

        return mTracker;
    }

    protected void configureMarvelApiHttpClient() {
        OkHttpClient client = mMarvelApiConfig.getRetrofit().client();
        client.setConnectTimeout(BuildConfig.HTTP_CONNECTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        client.setReadTimeout(BuildConfig.HTTP_READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        Cache cache = new Cache(getCacheDir(), SIZE_10_MB);
        client.setCache(cache);
    }

}
