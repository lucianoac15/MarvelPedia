package lucianoac.marvelpedia.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.karumi.marvelapiclient.model.ComicDto;
import com.karumi.marvelapiclient.model.MarvelImage;
import com.karumi.marvelapiclient.model.MarvelUrl;

import java.util.LinkedList;
import java.util.List;

import lucianoac.marvelpedia.helpers.MarvelImgHelper;

public class Comic implements Parcelable {
    private static final String URL_TYPE_DETAIL = "detail";
    private int mId;
    private String mTitle;
    private String mDescription;
    private String mDetailsUrl;
    private String mThumbnailUrl;
    private List<String> mImageUrls;
    private String mModified;

    public Comic() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    private void setDescription(String description) {
        mDescription = description;
    }

    public String getDetailsUrl() {
        return mDetailsUrl;
    }

    private void setDetailsUrl(String detailsUrl) {
        mDetailsUrl = detailsUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    private void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public List<String> getImageUrls() {
        return mImageUrls;
    }

    private void setImageUrls(List<String> imageUrls) {
        mImageUrls = imageUrls;
    }

    public String getModified() {
        return mModified;
    }

    private void setModified(String modified) {
        mModified = modified;
    }

    public static Comic fromComicDto(ComicDto comicDto) {
        Comic comic = new Comic();
        comic.setId(Integer.valueOf(comicDto.getId()));
        comic.setTitle(comicDto.getTitle());
        comic.setDescription(comicDto.getDescription());
        comic.setDetailsUrl(getDetailsUrl(comicDto));
        comic.setThumbnailUrl(MarvelImgHelper.getUrlFromImage(comicDto.getThumbnail()));
        comic.setImageUrls(getImageUrls(comicDto));
        comic.setModified(comicDto.getModified());

        return comic;
    }

    private static String getDetailsUrl(ComicDto comicDto) {
        if (comicDto == null) {
            return null;
        }

        if (comicDto.getUrls() == null) {
            return null;
        }

        List<MarvelUrl> urls = comicDto.getUrls();
        for (MarvelUrl url : urls) {
            if (URL_TYPE_DETAIL.equals(url.getType())) {
                return url.getUrl();
            }
        }

        return null;
    }

    private static List<String> getImageUrls(ComicDto comicDto) {
        boolean hasImages = (comicDto != null &&
                comicDto.getImages() != null &&
                comicDto.getImages().size() > 0);

        if (!hasImages) {
            return new LinkedList<>();
        }

        List<String> imageUrls = new LinkedList<>();
        for (MarvelImage marvelImage : comicDto.getImages()) {
            String imageUrl = MarvelImgHelper.getUrlFromImage(marvelImage);
            if (!TextUtils.isEmpty(imageUrl)) {
                imageUrls.add(imageUrl);
            }
        }

        return imageUrls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeString(this.mDetailsUrl);
        dest.writeString(this.mThumbnailUrl);
        dest.writeStringList(this.mImageUrls);
        dest.writeString(this.mModified);
    }

    protected Comic(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mDetailsUrl = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mImageUrls = in.createStringArrayList();
        this.mModified = in.readString();
    }

    public static final Parcelable.Creator<Comic> CREATOR = new Parcelable.Creator<Comic>() {
        @Override
        public Comic createFromParcel(Parcel source) {
            return new Comic(source);
        }

        @Override
        public Comic[] newArray(int size) {
            return new Comic[size];
        }
    };
}
