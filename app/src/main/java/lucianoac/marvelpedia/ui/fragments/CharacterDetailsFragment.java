package lucianoac.marvelpedia.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karumi.marvelapiclient.model.ComicDto;
import com.karumi.marvelapiclient.model.MarvelImage;
import com.karumi.marvelapiclient.model.SeriesDto;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lucianoac.marvelpedia.BuildConfig;
import lucianoac.marvelpedia.MarvelPediaApplication;
import lucianoac.marvelpedia.R;
import lucianoac.marvelpedia.adapter.AbstractCharacterRelatedItemsAdapter;
import lucianoac.marvelpedia.data.MPContract;
import lucianoac.marvelpedia.data.model.Character;
import lucianoac.marvelpedia.data.model.Comic;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;
import lucianoac.marvelpedia.managers.SeenCharactersManager;
import lucianoac.marvelpedia.ui.activities.ComicDetailsActivity;
import lucianoac.marvelpedia.ui.decorators.MarginItemDecoration;
import lucianoac.marvelpedia.ui.fragments.workers.ComicsLoaderWorkerFragment;
import lucianoac.marvelpedia.ui.fragments.workers.SeriesLoaderWorkerFragment;

import static butterknife.ButterKnife.findById;

public class CharacterDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ComicsLoaderWorkerFragment.TaskCallbacks, SeriesLoaderWorkerFragment.TaskCallbacks, AbstractCharacterRelatedItemsAdapter.OnItemClickListener {
    public static final String TAG = CharacterDetailsFragment.class.getSimpleName();
    private static final String LOG_TAG = TAG;

    private static final int CHARACTER_LOADER = 100;

    private static final String KEY_CHARACTER_ID = String.format("%s.character_id", BuildConfig.APPLICATION_ID);
    private static final String KEY_CHARACTER = String.format("%s.character", BuildConfig.APPLICATION_ID);
    private static final String KEY_SHOW_CHARACTER_NAME = String.format("%s.show_name", BuildConfig.APPLICATION_ID);
    private static final String KEY_SHOW_CHARACTER_THUMBNAIL = String.format("%s.show_thumbnail", BuildConfig.APPLICATION_ID);

    private static final String KEY_CHARACTER_REGISTERED_AS_SEEN = String.format("%s.character_seen", BuildConfig.APPLICATION_ID);

    private static final String KEY_COMICS_LIST_LAYOUT_STATE = String.format("%s.comics_layout_list_state", BuildConfig.APPLICATION_ID);

    private Picasso mPicasso;

    private AbstractCharacterRelatedItemsAdapter mAdapterCharacterComics;
    private AbstractCharacterRelatedItemsAdapter mAdapterCharacterSeries;

    @BindView(R.id.text_name)
    protected TextView mTextCharacterName;

    @BindView(R.id.text_description)
    protected TextView mTextCharacterDescription;

    @BindView(R.id.image_details_thumb)
    protected ImageView mImageCharacterThumbnail;

    @BindView(R.id.recycler_view_character_comics)
    protected RecyclerView mRecyclerViewCharacterComics;

    @BindView(R.id.recycler_view_character_series)
    protected RecyclerView mRecyclerViewCharacterSeries;

    @BindView(R.id.card_character_comics)
    protected CardView mCardCharacterComics;

    @BindView(R.id.card_toolbar)
    protected Toolbar mCardToolbar;

    private View mViewEmptyComics;
    private ProgressBar mProgressComics;

    private ComicsLoaderWorkerFragment mComicsLoaderWorkerFragment;
    private SeriesLoaderWorkerFragment mSeriesLoaderWorkerFragment;

    private long mCharacterId;
    private boolean mShowThumbnail;
    private boolean mShowCharacterName;

    private boolean mRegisteredAsSeen = false;

    private Character mCharacter;

    private Unbinder mUnbinder;
    private Button mButtonRetryLoadingComics;

    private Parcelable mRecyclerViewCharacterComicsState;

    public CharacterDetailsFragment() {
        // Required empty public constructor
    }


    public static CharacterDetailsFragment newInstance(long characterId, boolean showName, boolean showThumbnail) {
        CharacterDetailsFragment fragment = new CharacterDetailsFragment();

        Bundle args = new Bundle();
        args.putLong(KEY_CHARACTER_ID, characterId);
        args.putBoolean(KEY_SHOW_CHARACTER_NAME, showName);
        args.putBoolean(KEY_SHOW_CHARACTER_THUMBNAIL, showThumbnail);

        fragment.setArguments(args);

        return fragment;
    }

    public static CharacterDetailsFragment newInstance(long characterId) {
        return newInstance(characterId, false, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MarvelPediaLogger.debug(LOG_TAG, MarvelPediaLogger.SEPARATOR);
        MarvelPediaLogger.debug(LOG_TAG, "onCreate()");
        MarvelPediaLogger.debug(LOG_TAG, MarvelPediaLogger.SEPARATOR);

        if (savedInstanceState == null && getArguments() != null) {
            mCharacterId = getArguments().getLong(KEY_CHARACTER_ID);
            mShowThumbnail = getArguments().getBoolean(KEY_SHOW_CHARACTER_THUMBNAIL);
            mShowCharacterName = getArguments().getBoolean(KEY_SHOW_CHARACTER_NAME);
        }

        logState();

        registerSeenCharacter();

        setupComicsLoaderFragment();
        setupSeriesLoaderFragment();

        mPicasso = MarvelPediaApplication.getInstance().getPicasso();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreInstanceState(savedInstanceState);
    }

    private void logState() {
        MarvelPediaLogger.debug(LOG_TAG, "mCharacterId: " + mCharacterId);
        MarvelPediaLogger.debug(LOG_TAG, "mShowThumbnail: " + mShowThumbnail);
        MarvelPediaLogger.debug(LOG_TAG, "mShowCharacterName: " + mShowCharacterName);
        MarvelPediaLogger.debug(LOG_TAG, "mRegisteredAsSeen: " + mRegisteredAsSeen);
        MarvelPediaLogger.debug(LOG_TAG, "mRecyclerViewCharacterComicsState: " + mRecyclerViewCharacterComicsState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(KEY_CHARACTER_ID, mCharacterId);
        outState.putParcelable(KEY_CHARACTER, mCharacter);
        outState.putBoolean(KEY_SHOW_CHARACTER_THUMBNAIL, mShowThumbnail);
        outState.putBoolean(KEY_SHOW_CHARACTER_NAME, mShowCharacterName);

        outState.putParcelable(KEY_COMICS_LIST_LAYOUT_STATE, mRecyclerViewCharacterComics.getLayoutManager().onSaveInstanceState());

        outState.putBoolean(KEY_CHARACTER_REGISTERED_AS_SEEN, mRegisteredAsSeen);
    }

    private void registerSeenCharacter() {
        if (mRegisteredAsSeen) {
            MarvelPediaLogger.debug(LOG_TAG, String.format("Character %s has already been registered as seen. Skipping it.", mCharacterId));
            return;
        }

        MarvelPediaLogger.debug(LOG_TAG, String.format("Registering character %s as seen.", mCharacterId));

        new SeenCharactersManager(getContext()).registrySeenCharacter(mCharacterId, new SeenCharactersManager.OnRegisterCharacterAsSeen() {
            @Override
            public void onRegisterAsSeen(boolean wasRegistered) {
                CharacterDetailsFragment.this.mRegisteredAsSeen = wasRegistered;

                MarvelPediaLogger.debug(LOG_TAG, "onRegisterAsSeen - Registered: " + wasRegistered);
            }
        });
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        mCharacterId = savedInstanceState.getLong(KEY_CHARACTER_ID);
        mCharacter = savedInstanceState.getParcelable(KEY_CHARACTER);
        mShowThumbnail = savedInstanceState.getBoolean(KEY_SHOW_CHARACTER_THUMBNAIL);
        mShowCharacterName = savedInstanceState.getBoolean(KEY_SHOW_CHARACTER_NAME);

        mRegisteredAsSeen = savedInstanceState.getBoolean(KEY_CHARACTER_REGISTERED_AS_SEEN, false);

        mRecyclerViewCharacterComicsState = savedInstanceState.getParcelable(KEY_COMICS_LIST_LAYOUT_STATE);
    }

    private void setupComicsLoaderFragment() {
        FragmentManager fm = getFragmentManager();

        mComicsLoaderWorkerFragment = (ComicsLoaderWorkerFragment) fm.findFragmentByTag(ComicsLoaderWorkerFragment.TAG);
        if (mComicsLoaderWorkerFragment == null) {
            mComicsLoaderWorkerFragment = ComicsLoaderWorkerFragment.newInstance(mCharacterId);
            fm.beginTransaction().add(mComicsLoaderWorkerFragment, ComicsLoaderWorkerFragment.TAG).commit();
        }
    }

    private void setupSeriesLoaderFragment() {
        FragmentManager fm = getFragmentManager();
        mSeriesLoaderWorkerFragment = (SeriesLoaderWorkerFragment) fm.findFragmentByTag(SeriesLoaderWorkerFragment.TAG);
        if (mSeriesLoaderWorkerFragment == null) {
            mSeriesLoaderWorkerFragment = SeriesLoaderWorkerFragment.newInstance(mCharacterId);
            fm.beginTransaction().add(mSeriesLoaderWorkerFragment, SeriesLoaderWorkerFragment.TAG).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_character_details, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mImageCharacterThumbnail.setVisibility(mShowThumbnail ? View.VISIBLE : View.GONE);
        mTextCharacterName.setVisibility(mShowCharacterName ? View.VISIBLE : View.GONE);

        mProgressComics = findById(mCardCharacterComics, R.id.progress_bar);
        mViewEmptyComics = findById(mCardCharacterComics, R.id.view_blank_state);
        setupRetryButton();

        setupComicsAdapterAndRecyclerView(rootView);
        setupSeriesAdapterAndRecyclerView(rootView);

        mCardToolbar.setVisibility(View.GONE);

        if (mCharacter == null) {
            getActivity().getSupportLoaderManager().initLoader(CHARACTER_LOADER, null, this);
        } else {
            bindValues();
        }

        return rootView;
    }

    private void setupRetryButton() {
        mButtonRetryLoadingComics = findById(mViewEmptyComics, R.id.button_blank_state_action);
        mButtonRetryLoadingComics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mComicsLoaderWorkerFragment.isLoading()) {
                    mComicsLoaderWorkerFragment.load();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
    }

    private void setupComicsAdapterAndRecyclerView(View view) {
        List<ComicDto> comics = new LinkedList<>();
        if (!mComicsLoaderWorkerFragment.isLoading() && mComicsLoaderWorkerFragment.hasCachedResults()) {
            comics = mComicsLoaderWorkerFragment.getCachedResults();
        }

        mAdapterCharacterComics = new AbstractCharacterRelatedItemsAdapter<ComicDto>(ComicDto.class, comics) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                ComicDto comic = mItems.get(position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mImageViewThumbnail.setTransitionName(getString(R.string.shared_transition_comic_thumb));
                }

                if (!comic.getImages().isEmpty()) {
                    MarvelImage image = comic.getImages().get(0);
                    setThumbnail(holder, image);
                }
                holder.mImageViewThumbnail.setContentDescription(getString(R.string.content_description_comic_image, comic.getTitle()));

                setTitle(holder, comic.getTitle());
                holder.mTxtViewTitle.setContentDescription(getString(R.string.content_description_comic_title, comic.getTitle()));
            }
        };
        mAdapterCharacterComics.setOnItemClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mRecyclerViewCharacterComics.setLayoutManager(layoutManager);
        mRecyclerViewCharacterComics.setAdapter(mAdapterCharacterComics);
        int marginRight = getResources().getDimensionPixelSize(R.dimen.default_spacing);
        mRecyclerViewCharacterComics.addItemDecoration(new MarginItemDecoration(0, marginRight, 0, 0));

        if (mRecyclerViewCharacterComicsState != null) {
            mRecyclerViewCharacterComics.getLayoutManager().onRestoreInstanceState(mRecyclerViewCharacterComicsState);
        }

        showOrHideComicsList(comics);
    }

    private void setupSeriesAdapterAndRecyclerView(View view) {
        mAdapterCharacterSeries = new AbstractCharacterRelatedItemsAdapter<SeriesDto>(SeriesDto.class, null) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                SeriesDto series = mItems.get(position);

                if (series.getThumbnail() != null) {
                    setThumbnail(holder, series.getThumbnail());
                }
                holder.mImageViewThumbnail.setContentDescription(getString(R.string.content_description_series_image, series.getTitle()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mImageViewThumbnail.setTransitionName(getString(R.string.shared_transition_series_image));
                }

                setTitle(holder, series.getTitle());
                holder.mTxtViewTitle.setContentDescription(getString(R.string.content_description_series_title, series.getTitle()));
            }
        };
        mAdapterCharacterSeries.setOnItemClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mRecyclerViewCharacterSeries.setLayoutManager(layoutManager);
        mRecyclerViewCharacterSeries.setAdapter(mAdapterCharacterSeries);
        int marginRight = getResources().getDimensionPixelSize(R.dimen.default_spacing);
        mRecyclerViewCharacterSeries.addItemDecoration(new MarginItemDecoration(0, marginRight, 0, 0));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CHARACTER_LOADER:
                Uri characterUri = MPContract.CharacterEntry.buildCharacterUri(mCharacterId);
                return new CursorLoader(getContext(), characterUri, null, null, null, null);
            default:
                String message = String.format("Invalid loader id: %s", id);
                throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        switch (loader.getId()) {
            case CHARACTER_LOADER:
                cursor.moveToFirst();
                mCharacter = Character.fromCursor(cursor);
                bindValues();

                return;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void bindValues() {
        if (mShowThumbnail) {
            mPicasso.load(mCharacter.getThumbnailUrl())
                    .placeholder(R.drawable.character_placeholder_landscape)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.character_placeholder_landscape)
                    .into(mImageCharacterThumbnail);

            mImageCharacterThumbnail.setContentDescription(getString(R.string.content_description_character_image, mCharacter.getName()));
        }

        if (mShowCharacterName) {
            String name = mCharacter.getName();
            mTextCharacterName.setText(name);
            mTextCharacterName.setContentDescription(getString(R.string.content_description_character_name, mCharacter.getName()));
        }

        String description = mCharacter.getDescription();
        if (TextUtils.isEmpty(description)) {
            description = getString(R.string.not_available_description);
        }
        mTextCharacterDescription.setText(Html.fromHtml(description));
        mTextCharacterDescription.setContentDescription(getString(R.string.content_description_character_description, description));
    }

    @Override
    public void onComicsLoadingPreExecute() {
        MarvelPediaLogger.debug(LOG_TAG, "onComicsLoadingPreExecute");

        mViewEmptyComics.setVisibility(View.INVISIBLE);
        mRecyclerViewCharacterComics.setVisibility(View.GONE);
        mProgressComics.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComicsLoadingCancelled() {
        MarvelPediaLogger.debug(LOG_TAG, "onComicsLoadingCancelled");

        mViewEmptyComics.setVisibility(View.VISIBLE);
        mRecyclerViewCharacterComics.setVisibility(View.GONE);
        mProgressComics.setVisibility(View.GONE);
    }

    @Override
    public void onComicsLoaded(List<ComicDto> comics) {
        MarvelPediaLogger.debug(LOG_TAG, "onComicsLoaded - comics: " + comics);

        mAdapterCharacterComics.setItems(comics);
        mAdapterCharacterComics.notifyDataSetChanged();

        showOrHideComicsList(comics);
    }

    private void showOrHideComicsList(List<ComicDto> comics) {
        mViewEmptyComics.setVisibility(comics.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        mRecyclerViewCharacterComics.setVisibility(comics.isEmpty() ? View.GONE : View.VISIBLE);
        mProgressComics.setVisibility(View.GONE);
    }

    @Override
    public void onSeriesLoadingPreExecute() {
        MarvelPediaLogger.debug(LOG_TAG, "onSeriesLoadingPreExecute");
    }

    @Override
    public void onSeriesLoadingCancelled() {
        MarvelPediaLogger.debug(LOG_TAG, "onSeriesLoadingCancelled");
    }

    @Override
    public void onSeriesLoaded(List<SeriesDto> series) {
        MarvelPediaLogger.debug(LOG_TAG, "onSeriesLoaded - comics: " + series);
        mAdapterCharacterSeries.setItems(series);
        mAdapterCharacterSeries.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Class type, int position, View view) {
        MarvelPediaLogger.debug(LOG_TAG, "type: " + type + " | position: " + position);

        if (type == ComicDto.class) {
            ComicDto comic = (ComicDto) mAdapterCharacterComics.getItem(position);
            showComicDetails(comic, view);
        } else if (type == SeriesDto.class) {
            SeriesDto series = (SeriesDto) mAdapterCharacterSeries.getItem(position);
            showSeriesDetails(series, view);
        } else {
            MarvelPediaLogger.debug(LOG_TAG, "Unknown item type: " + type);
        }
    }

    private void showComicDetails(ComicDto comic, View view) {
        MarvelPediaLogger.debug(LOG_TAG, "Comic: " + comic);

        Comic marvelComic = Comic.fromComicDto(comic);
        Intent intent = ComicDetailsActivity.newIntent(getContext(), marvelComic);

        String sharedTransitionName = getContext().getString(R.string.shared_transition_comic_thumb);
        startDetailsActivity(view, intent, sharedTransitionName);
    }

    private void startDetailsActivity(View view, Intent intent, String sharedTransitionName) {
        ImageView imageView = findById(view, R.id.image_item_thumb);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                imageView,
                sharedTransitionName
        );

        startActivity(intent, options.toBundle());
    }

    private void showSeriesDetails(SeriesDto series, View view) {
        MarvelPediaLogger.debug(LOG_TAG, "Series: " + series);
    }
}

