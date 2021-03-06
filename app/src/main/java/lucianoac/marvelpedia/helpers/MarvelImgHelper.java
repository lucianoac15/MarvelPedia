package lucianoac.marvelpedia.helpers;

import com.karumi.marvelapiclient.model.MarvelImage;


public class MarvelImgHelper {
    private static final String MATCH_PATTERN = "\\/([^/]+)\\.([a-zA-Z]{3,4})$";
    private static final String REPLACE_PATTERN = "/$1/%s.$2";

    public static String buildSizedImageUrl(String imageUrl, MarvelImage.Size size) {
        if (size == null) {
            throw new IllegalArgumentException("Size can't be null.");
        }

        String replacement = String.format(REPLACE_PATTERN, size);

        return imageUrl.replaceAll(MATCH_PATTERN, replacement);
    }

    public static String getUrlFromImage(MarvelImage marvelImage) {
        if (marvelImage == null) {
            return null;
        }

        return String.format("%s.%s", marvelImage.getPath(), marvelImage.getExtension());
    }
}