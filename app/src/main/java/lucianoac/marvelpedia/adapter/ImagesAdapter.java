package lucianoac.marvelpedia.adapter;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.karumi.marvelapiclient.model.MarvelImage;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lucianoac.marvelpedia.MarvelPediaApplication;
import lucianoac.marvelpedia.R;
import lucianoac.marvelpedia.helpers.MarvelImgHelper;
import lucianoac.marvelpedia.infrastructure.MarvelPediaLogger;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private static final String LOG_TAG = ImagesAdapter.class.getSimpleName();

    private String mTransitionName;

    Picasso mPicasso;

    protected List<String> mItems;

    private OnItemClickListener mOnItemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image_thumbnail)
        public ImageView mImageViewThumbnail;

        private OnItemClickListener mOnItemClickListener;

        public ViewHolder(View view, String transitionName) {
            super(view);

            ButterKnife.bind(this, view);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mImageViewThumbnail.setTransitionName(transitionName);
            }

            view.setOnClickListener(this);
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getAdapterPosition(), view);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ImagesAdapter(List<String> items, String transitionName) {
        mPicasso = MarvelPediaApplication.getInstance().getPicasso();
        mTransitionName = transitionName;

        setItems(items);
    }

    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thumbnail, parent, false);

        ViewHolder vh = new ViewHolder(view, mTransitionName);
        vh.setOnItemClickListener(mOnItemClickListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setThumbnail(holder, mItems.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected void setThumbnail(ViewHolder holder, String imageUrl) {
        String thumbnailUrl = MarvelImgHelper.buildSizedImageUrl(imageUrl, MarvelImage.Size.PORTRAIT_XLARGE);
        mPicasso.load(thumbnailUrl)
                .placeholder(R.drawable.character_placeholder_portrait)
                .fit()
                .centerInside()
                .error(R.drawable.character_placeholder_portrait)
                .into(holder.mImageViewThumbnail);
    }

    public void setItems(List<String> items) {
        if (items == null) {
            mItems = new LinkedList<>();
        } else {
            mItems = items;
        }
    }

    public void addItems(List<String> items) {
        if (mItems == null) {
            mItems = new LinkedList<>();
        }

        mItems.addAll(items);
    }

    public void addItem(String item) {
        if (mItems == null) {
            mItems = new LinkedList<>();
        }

        mItems.add(item);
    }

    public String getItem(int position) {
        try {
            return mItems.get(position);
        } catch (Exception e) {
            MarvelPediaLogger.error(LOG_TAG, e);

            return null;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}