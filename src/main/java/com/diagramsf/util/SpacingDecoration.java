package com.diagramsf.util;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * 设置RecyclerView的 item间距
 *
 * Created by Diagrams on 2016/7/27 18:15
 */
//http://stackoverflow.com/questions/30524599/items-are-not-the-same-width-when-using-recyclerview-gridlayoutmanager-to-make-c
public class SpacingDecoration extends RecyclerView.ItemDecoration {

  private int mHorizontalSpacing = 0;
  private int mVerticalSpacing = 0;
  private boolean mIncludeEdge = false;

  public SpacingDecoration(int hSpacing, int vSpacing, boolean includeEdge) {
    mHorizontalSpacing = hSpacing;
    mVerticalSpacing = vSpacing;
    mIncludeEdge = includeEdge;
  }

  public SpacingDecoration(int hSpacing, boolean includeEdge) {
    this(hSpacing, 0, includeEdge);
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    // Only handle the vertical situation
    int position = parent.getChildAdapterPosition(view);
    if (parent.getLayoutManager() instanceof GridLayoutManager) {
      GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
      GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
      int spanCount = layoutManager.getSpanCount();
      int positionSpanSize = spanSizeLookup.getSpanSize(position);
      int column = spanSizeLookup.getSpanIndex(position, spanCount);
      if (positionSpanSize == spanCount) {//只有一列的话，不进行设置
        return;
      }
      //int column = position % spanCount;//原始的计算column的方法
      getGridItemOffsets(outRect, position, column, spanCount);
    } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
      StaggeredGridLayoutManager layoutManager =
          (StaggeredGridLayoutManager) parent.getLayoutManager();
      int spanCount = layoutManager.getSpanCount();
      StaggeredGridLayoutManager.LayoutParams lp =
          (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
      int column = lp.getSpanIndex();
      getGridItemOffsets(outRect, position, column, spanCount);
    } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
      outRect.left = mHorizontalSpacing;
      outRect.right = mHorizontalSpacing;
      if (mIncludeEdge) {
        if (position == 0) {
          outRect.top = mVerticalSpacing;
        }
        outRect.bottom = mVerticalSpacing;
      } else {
        if (position > 0) {
          outRect.top = mVerticalSpacing;
        }
      }
    }
  }

  private void getGridItemOffsets(Rect outRect, int position, int column, int spanCount) {
    if (mIncludeEdge) {
      outRect.left = mHorizontalSpacing * (spanCount - column) / spanCount;
      outRect.right = mHorizontalSpacing * (column + 1) / spanCount;
      if (position < spanCount) {
        outRect.top = mVerticalSpacing;
      }
      outRect.bottom = mVerticalSpacing;
    } else {
      outRect.left = mHorizontalSpacing * column / spanCount;
      outRect.right = mHorizontalSpacing * (spanCount - 1 - column) / spanCount;
      if (position >= spanCount) {
        outRect.top = mVerticalSpacing;
      }
    }
  }
}