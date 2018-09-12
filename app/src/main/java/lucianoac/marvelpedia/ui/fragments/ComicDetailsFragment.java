package lucianoac.marvelpedia.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lucianoac.marvelpedia.BuildConfig;
import lucianoac.marvelpedia.MarvelPediaApplication;
import lucianoac.marvelpedia.R;
import lucianoac.marvelpedia.adapter.ImagesAdapter;
import lucianoac.marvelpedia.data.model.Comic;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;
import lucianoac.marvelpedia.ui.activities.ImageViewerActivity;
import lucianoac.marvelpedia.ui.decorators.MarginItemDecoration;

import static butterknife.ButterKnife.findById;

public class ComicDetailsFragment extends Fragment implements ImagesAdapter.OnItemClickListener {
    private static final String LOG_TAG = ComicDetailsFragment.class.getSimpleName();

    private static final String ARG_COMIC = "lucianoac.marvelpedia.comic";
    private static final String ARG_SHOW_TITLE = "lucianoac.marvelpedia.show_title";
    private static final String ARG_SHOW_THUMBNAIL = "lucianoac.marvelpedia.show_thumbnail";
    private static final int LAYOUT_COLUMNS = 2;

    private ImagesAdapter mImagesAdapter;
    private RecyclerView mImagesRecyclerView;

    private Picasso mPicasso;

    private Comic mComic;

    private boolean mShowThumbnail;
    private boolean mShowTitle;

    @BindView(R.id.text_name)
    protected TextView mTextTitle;

    @BindView(R.id.text_description)
    protected TextView mTextDescription;

    @BindView(R.id.image_details_thumb)
    protected ImageView mImageThumbnail;

    private Unbinder mUnbinder;

    private String mSelectedImageUrl;
    private View mImageSelectedComic;

    public ComicDetailsFragment() {
        // Required empty public constructor
    }

    public static ComicDetailsFragment newInstance(Comic comic, boolean showName, boolean showThumbnail) {
        ComicDetailsFragment fragment = new ComicDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_COMIC, comic);
        args.putBoolean(ARG_SHOW_TITLE, showName);
        args.putBoolean(ARG_SHOW_THUMBNAIL, showThumbnail);

        fragment.setArguments(args);

        return fragment;
    }

    public static ComicDetailsFragment newInstance(Comic comic) {
        return newInstance(comic, false, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mComic = getArguments().getParcelable(ARG_COMIC);
            mShowThumbnail = getArguments().getBoolean(ARG_SHOW_THUMBNAIL);
            mShowTitle = getArguments().getBoolean(ARG_SHOW_TITLE);

            MarvelPediaLogger.debug(LOG_TAG, "comic: " + mComic);
        }

        mPicasso = MarvelPediaApplication.getInstance().getPicasso();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_comic_details, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mImageThumbnail.setVisibility(mShowThumbnail ? View.VISIBLE : View.GONE);
        mTextTitle.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);

        setupImagesRecyclerViewAndAdapter(rootView);

        bindValues();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
    }

    private void setupImagesRecyclerViewAndAdapter(View view) {
        mImagesAdapter = new ImagesAdapter(mComic.getImageUrls(), getString(R.string.shared_transition_comic_image));
        mImagesAdapter.setOnItemClickListener(this);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(LAYOUT_COLUMNS, StaggeredGridLayoutManager.VERTICAL);

        mImagesRecyclerView = findById(view, R.id.recycler_view_comics_images);
        mImagesRecyclerView.setLayoutManager(layoutManager);
        mImagesRecyclerView.setAdapter(mImagesAdapter);
        int margin = getResources().getDimensionPixelSize(R.dimen.default_spacing);
        mImagesRecyclerView.addItemDecoration(new MarginItemDecoration(margin, margin, margin, margin));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void bindValues() {
        if (mShowThumbnail) {
            mPicasso.load(mComic.getThumbnailUrl())
                    .placeholder(R.drawable.character_placeholder_landscape)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.character_placeholder_landscape)
                    .into(mImageThumbnail);

            mImageThumbnail.setContentDescription(getString(R.string.content_description_comic_image, mComic.getTitle()));
        }

        if (mShowTitle) {
            mTextTitle.setText(mComic.getTitle());
            mTextTitle.setContentDescription(getString(R.string.content_description_comic_title, mComic.getTitle()));
        }

        String description = mComic.getDescription();
        if (TextUtils.isEmpty(description)) {
            description = getString(R.string.not_available_description);
        }
        mTextDescription.setText(Html.fromHtml(description));
        mTextDescription.setContentDescription(getString(R.string.content_description_comic_description, mComic.getDescription()));
    }

    @Override
    public void onItemClick(int position, View view) {
        mSelectedImageUrl = mImagesAdapter.getItem(position);
        mImageSelectedComic = findById(view, R.id.image_thumbnail);

            showSelectedImage();
    }

    private void showSelectedImage() {
        if (mSelectedImageUrl == null) {
            return;
        }

        String id = String.valueOf(mSelectedImageUrl.hashCode());
        Intent intent = ImageViewerActivity.newIntent(getContext(), id, mSelectedImageUrl, getString(R.string.shared_transition_comic_image));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                mImageSelectedComic,
                getContext().getString(R.string.shared_transition_comic_image)
        );

        startActivity(intent, options.toBundle());
    }
}

