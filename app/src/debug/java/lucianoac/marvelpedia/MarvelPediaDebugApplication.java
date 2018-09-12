package lucianoac.marvelpedia;

import com.karumi.marvelapiclient.MarvelApiConfig;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;


public class MarvelPediaDebugApplication extends MarvelPediaApplication {
    private static final String LOG_TAG = MarvelPediaDebugApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        setupButterKnife();
    }

    public MarvelApiConfig getMarvelApiConfig() {
        if (mMarvelApiConfig != null) {
            return mMarvelApiConfig;
        }
        mMarvelApiConfig = new MarvelApiConfig.Builder(BuildConfig.MARVEL_API_PUBLIC_KEY, BuildConfig.MARVEL_API_PRIVATE_KEY).debug().build();
        configureMarvelApiHttpClient();
        return mMarvelApiConfig;
    }

    private void setupButterKnife() {
        ButterKnife.setDebug(true);
    }

    @Override
    public Picasso getPicasso() {
        Picasso picasso = super.getPicasso();
        picasso.setLoggingEnabled(true);
        return picasso;
    }
}
