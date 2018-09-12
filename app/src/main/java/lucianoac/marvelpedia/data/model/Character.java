package lucianoac.marvelpedia.data.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import lucianoac.marvelpedia.data.MPContract;

public class Character implements Parcelable {
    private long mId;
    private String mName;
    private String mDescription;
    private String mThumbnailUrl;
    private String mDetailsUrl;
    private String mModified;
    private boolean mIsBookmarked;
    private String mLastSeen;

    public Character() {
        super();
    }

    public static Character fromCursor(Cursor cursor) {
        Character character = new Character();
        character.setId(cursor.getLong(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_CHARACTER_ID)));
        character.setName(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_NAME)));
        character.setDescription(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_DESCRIPTION)));
        character.setThumbnailUrl(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_THUMBNAIL)));
        character.setDetailsUrl(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_DETAILS_URL)));
        character.setModified(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_MODIFIED)));
        character.setBookmarked(cursor.getInt(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_IS_BOOKMARK)) > 0);
        character.setLastSeen(cursor.getString(cursor.getColumnIndex(MPContract.CharacterEntry.COLUMN_LAST_SEEN)));

        return character;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mId);
        parcel.writeString(mName);
        parcel.writeString(mDescription);
        parcel.writeString(mThumbnailUrl);
        parcel.writeString(mDetailsUrl);
        parcel.writeString(mModified);
        parcel.writeByte((byte) (mIsBookmarked ? 1 : 0));
        parcel.writeString(mLastSeen);
    }

    protected Character(Parcel parcel) {
        mId = parcel.readLong();
        mName = parcel.readString();
        mDescription = parcel.readString();
        mThumbnailUrl = parcel.readString();
        mDetailsUrl = parcel.readString();
        mModified = parcel.readString();
        mIsBookmarked = (parcel.readByte() == 1);
        mLastSeen = parcel.readString();
    }

    public static final Creator<Character> CREATOR = new Creator<Character>() {
        @Override
        public Character createFromParcel(Parcel in) {
            return new Character(in);
        }

        @Override
        public Character[] newArray(int size) {
            return new Character[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    public long getId() {
        return mId;
    }

    public Character setId(long id) {
        this.mId = id;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Character setName(String name) {
        mName = name;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Character setDescription(String description) {
        mDescription = description;
        return this;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public Character setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
        return this;
    }

    public String getDetailsUrl() {
        return mDetailsUrl;
    }

    public Character setDetailsUrl(String detailsUrl) {
        mDetailsUrl = detailsUrl;
        return this;
    }

    public String getModified() {
        return mModified;
    }

    public Character setModified(String modified) {
        mModified = modified;
        return this;
    }

    public boolean isBookmarked() {
        return mIsBookmarked;
    }

    public Character setBookmarked(boolean bookmarked) {
        mIsBookmarked = bookmarked;
        return this;
    }

    public String getLastSeen() {
        return mLastSeen;
    }

    public Character setLastSeen(String lastSeen) {
        mLastSeen = lastSeen;
        return this;
    }
}
