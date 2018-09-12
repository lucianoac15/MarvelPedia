package lucianoac.marvelpedia;

import com.karumi.marvelapiclient.model.MarvelImage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import lucianoac.marvelpedia.helpers.MarvelImgHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarvelImgHelperUnitTest {
    private static final String IMAGE_PATH = "http://i.annihil.us/u/prod/marvel/i/mg/3/10/5130f81a682b5";
    private static final String IMAGE_EXTENSION = "jpg";
    private static final String IMAGE_URL = String.format("%s.%s", IMAGE_PATH, IMAGE_EXTENSION);

    @Mock
    MarvelImage image;

    @Test
    public void shouldReturnUrlFromMarvelImageObject() {
        when(image.getPath()).thenReturn(IMAGE_PATH);
        when(image.getExtension()).thenReturn(IMAGE_EXTENSION);

        String imageUrl = MarvelImgHelper.getUrlFromImage(image);
        assertEquals(IMAGE_URL, imageUrl);
    }

    @Test
    public void shouldCreateCorrectImageUrl() throws Exception {
        String resizedUrl = MarvelImgHelper.buildSizedImageUrl(IMAGE_URL, MarvelImage.Size.LANDSCAPE_SMALL);

        String expectedUrl = "http://i.annihil.us/u/prod/marvel/i/mg/3/10/5130f81a682b5/landscape_small.jpg";
        assertEquals(expectedUrl, resizedUrl);
    }

    @Test
    public void shouldReturnNullUrlIfMarvelImageObjectIsNull() {
        assertNull(MarvelImgHelper.getUrlFromImage(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotChangeUrlIfDoesntMatchPattern() {
        MarvelImgHelper.buildSizedImageUrl(IMAGE_URL, null);
    }
}
