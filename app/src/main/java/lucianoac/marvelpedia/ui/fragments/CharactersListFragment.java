package lucianoac.marvelpedia.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lucianoac.marvelpedia.BuildConfig;
import lucianoac.marvelpedia.R;
import lucianoac.marvelpedia.adapter.CharactersCursorAdapter;
import lucianoac.marvelpedia.data.MPContract;
import lucianoac.marvelpedia.data.model.Character;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;
import lucianoac.marvelpedia.managers.FavoritesManager;
import lucianoac.marvelpedia.ui.activities.CharacterDetailsActivity;
import lucianoac.marvelpedia.ui.decorators.MarginItemDecoration;

import static butterknife.ButterKnife.findById;

public class CharactersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static int LIST_TYPE_ALL = 0;
    public static int LIST_TYPE_BOOKMARKS = 1;
    public static int LIST_TYPE_SEEN = 2;

    private static final String LOG_TAG = CharactersListFragment.class.getSimpleName();
    private static final String EXTRA_LIST_TYPE = String.format("%s.list_type", BuildConfig.APPLICATION_ID);

    private int mListType;

    @BindView(R.id.recycler_view_characters_list)
    protected RecyclerView mCharactersRecyclerView;

    private CharactersCursorAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Unbinder mUnbinder;

    private FavoritesManager mFavoritesManager;
    private String[] mSelectionArgs;
    private String mSelection;

    public CharactersListFragment() {
        // Required empty public constructor
    }

    public static CharactersListFragment newInstance(int listType) {
        CharactersListFragment fragment = new CharactersListFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mListType = getArguments().getInt(EXTRA_LIST_TYPE);

            MarvelPediaLogger.debug(LOG_TAG, "listType: " + mListType);
        }

        setupBookmarksManager();
    }

    private void setupBookmarksManager() {
        mFavoritesManager = new FavoritesManager(getContext(), new FavoritesManager.BookmarkCallbacks() {
            @Override
            public void onBookmarkToogled(boolean isBookmarked) {
                MarvelPediaLogger.debug(LOG_TAG, String.format("onBookmarkToogled: ", isBookmarked));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_characters_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        setupAdapter();

        mLayoutManager = new LinearLayoutManager(getContext());
        mCharactersRecyclerView.setLayoutManager(mLayoutManager);
        mCharactersRecyclerView.setAdapter(mAdapter);

        int marginBottom = getResources().getDimensionPixelSize(R.dimen.default_spacing);
        mCharactersRecyclerView.addItemDecoration(new MarginItemDecoration(0, 0, marginBottom, 0));

        getActivity().getSupportLoaderManager().initLoader(mListType, null, this);

        return rootView;
    }

    private void setupAdapter() {
        mAdapter = new CharactersCursorAdapter(getContext(), null);
        mAdapter.setOnItemClickListener(new CharactersCursorAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, View view) {
                mAdapter.getCursor().moveToPosition(position);

                Character character = Character.fromCursor(mAdapter.getCursor());
                Intent intent = CharacterDetailsActivity.newIntent(getContext(), character);

                ImageView characterImage = findById(view, R.id.image_details_thumb);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        characterImage,
                        getContext().getString(R.string.shared_transition_character_image)
                );

                startActivity(intent, options.toBundle());
            }
        });

        mAdapter.setOnToogleBookmarkListener(new CharactersCursorAdapter.OnToogleBookmarkListener() {

            @Override
            public void onToogleBookmark(long characterId) {
                mFavoritesManager.toggleBookmark(characterId);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
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
        Pair<String, String[]> selectionArgs = getSelectionArgs();
        return new CursorLoader(getContext(), MPContract.CharacterEntry.CONTENT_URI, null, selectionArgs.first, selectionArgs.second, getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (!isKnownLoader(loader.getId())) {
            return;
        }

        cursor.moveToFirst();

        mAdapter.swapCursor(cursor);
        mAdapter.notifyDataSetChanged();
    }

    private boolean isKnownLoader(int loaderId) {
        return Arrays.asList(LIST_TYPE_ALL, LIST_TYPE_BOOKMARKS, LIST_TYPE_SEEN).contains(loaderId);
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    public String getSortOrder() {
        String sortOrder = null;

        if (LIST_TYPE_ALL == mListType) {
            sortOrder = String.format("%s ASC", MPContract.CharacterEntry.COLUMN_NAME);
        } else if (LIST_TYPE_BOOKMARKS == mListType) {
            sortOrder = String.format("%s DESC", MPContract.CharacterEntry.COLUMN_NAME);
        } else if (LIST_TYPE_SEEN == mListType) {
            sortOrder = String.format("%s DESC", MPContract.CharacterEntry.COLUMN_LAST_SEEN);
        }

        return sortOrder;
    }

    public Pair<String, String[]> getSelectionArgs() {
        String selection = null;
        String[] selectionArgs = null;

        if (LIST_TYPE_ALL == mListType) {
            selection = null;
            selectionArgs = null;
        } else if (LIST_TYPE_BOOKMARKS == mListType) {
            selection = String.format("%s = ?", MPContract.CharacterEntry.COLUMN_IS_BOOKMARK);
            selectionArgs = new String[]{"1"};
        } else if (LIST_TYPE_SEEN == mListType) {
            selection = String.format("%s IS NOT NULL", MPContract.CharacterEntry.COLUMN_LAST_SEEN);
            selectionArgs = null;
        }

        return new Pair<>(selection, selectionArgs);
    }
}
