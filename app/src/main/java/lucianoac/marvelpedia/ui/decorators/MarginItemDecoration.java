package lucianoac.marvelpedia.ui.decorators;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class MarginItemDecoration extends RecyclerView.ItemDecoration {

    private final int mMarginTop;
    private final int mMarginRight;
    private final int mMarginBottom;
    private final int mMarginLeft;

    public MarginItemDecoration(int marginTop, int marginRight, int marginBottom, int marginLeft) {
        this.mMarginTop = marginTop;
        this.mMarginRight = marginRight;
        this.mMarginBottom = marginBottom;
        this.mMarginLeft = marginLeft;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.top = mMarginTop;
        outRect.right = mMarginRight;
        outRect.bottom = mMarginBottom;
        outRect.left = mMarginLeft;
    }
}