package com.gallopmark.banner.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.AnimatorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.gallopmark.banner.BannerConfig;
import com.gallopmark.banner.R;
import com.gallopmark.banner.Utils;

public class PagerIndicator extends LinearLayout {

    private Banner banner;
    private PagerAdapter pagerAdapter;
    private int mIndicatorMargin = -1;
    private int mIndicatorWidth = -1;
    private int mIndicatorHeight = -1;
    private int mAnimatorResId = BannerConfig.INDICATOR_ANIMATOR;
    private int mAnimatorReverseResId = 0;
    private int mIndicatorBackgroundResId = BannerConfig.INDICATOR_BACKGROUND;
    private int mIndicatorUnselectedBackgroundResId = BannerConfig.INDICATOR_UNSELECTED_BACKGROUND;
    private Animator mAnimatorOut;
    private Animator mAnimatorIn;
    private Animator mImmediateAnimatorOut;
    private Animator mImmediateAnimatorIn;

    private int mLastPosition = -1;

    public PagerIndicator(Context context) {
        super(context);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Create and configure Indicator in Java code.
     */
    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin) {
        configureIndicator(indicatorWidth, indicatorHeight, indicatorMargin,
                R.animator.default_scale_animator, 0, R.drawable.indicator_oval_white, R.drawable.indicator_oval_gray);
    }

    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin,
                                   @AnimatorRes int animatorId, @AnimatorRes int animatorReverseId,
                                   @DrawableRes int indicatorBackgroundId,
                                   @DrawableRes int indicatorUnselectedBackgroundId) {
        mIndicatorWidth = indicatorWidth;
        mIndicatorHeight = indicatorHeight;
        mIndicatorMargin = indicatorMargin;
        mAnimatorResId = animatorId;
        mAnimatorReverseResId = animatorReverseId;
        mIndicatorBackgroundResId = indicatorBackgroundId;
        mIndicatorUnselectedBackgroundResId = indicatorUnselectedBackgroundId;
        checkIndicatorConfig(getContext());
    }

    private void checkIndicatorConfig(Context context) {
        mIndicatorWidth = (mIndicatorWidth < 0) ? Utils.dip2px(context, BannerConfig.DEFAULT_INDICATOR_SIZE) : mIndicatorWidth;
        mIndicatorHeight = (mIndicatorHeight < 0) ? Utils.dip2px(context, BannerConfig.DEFAULT_INDICATOR_SIZE) : mIndicatorHeight;
        mIndicatorMargin = (mIndicatorMargin < 0) ? Utils.dip2px(context, BannerConfig.DEFAULT_INDICATOR_MARGIN) : mIndicatorMargin;
        mAnimatorResId = (mAnimatorResId == 0) ? R.animator.default_scale_animator : mAnimatorResId;
        mAnimatorOut = createAnimatorOut(context);
        mImmediateAnimatorOut = createAnimatorOut(context);
        mImmediateAnimatorOut.setDuration(0);
        mAnimatorIn = createAnimatorIn(context);
        mImmediateAnimatorIn = createAnimatorIn(context);
        mImmediateAnimatorIn.setDuration(0);
        mIndicatorBackgroundResId = (mIndicatorBackgroundResId == 0) ? R.drawable.indicator_oval_white : mIndicatorBackgroundResId;
        mIndicatorUnselectedBackgroundResId = (mIndicatorUnselectedBackgroundResId == 0) ? R.drawable.indicator_oval_gray : mIndicatorUnselectedBackgroundResId;
    }

    private Animator createAnimatorOut(Context context) {
        return AnimatorInflater.loadAnimator(context, mAnimatorResId);
    }

    private Animator createAnimatorIn(Context context) {
        Animator animatorIn;
        if (mAnimatorReverseResId == 0) {
            animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorResId);
            animatorIn.setInterpolator(new ReverseInterpolator());
        } else {
            animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorReverseResId);
        }
        return animatorIn;
    }

    public void setupBanner(Banner banner) {
        this.banner = banner;
        if (banner.getAdapter() != null) {
            mLastPosition = -1;
            pagerAdapter = banner.getAdapter();
            registerDataSetObserver(mInternalDataSetObserver);
            createIndicators();
            banner.removeOnPageChangeListener(mInternalPageChangeListener);
            banner.addOnPageChangeListener(mInternalPageChangeListener);
            mInternalPageChangeListener.onPageSelected(banner.getCurrentItem());
        }
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        pagerAdapter.registerDataSetObserver(observer);
    }

    private final ViewPager.OnPageChangeListener mInternalPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (pagerAdapter == null || pagerAdapter.getCount() <= 0) {
                return;
            }
            detachAnimator(mAnimatorIn);
            detachAnimator(mAnimatorOut);
            View currentIndicator;
            if (mLastPosition >= 0 && (currentIndicator = getChildAt(mLastPosition)) != null) {
                currentIndicator.setBackgroundResource(mIndicatorUnselectedBackgroundResId);
                mAnimatorIn.setTarget(currentIndicator);
                mAnimatorIn.start();
            }
            View selectedIndicator = getChildAt(position);
            if (selectedIndicator != null) {
                selectedIndicator.setBackgroundResource(mIndicatorBackgroundResId);
                mAnimatorOut.setTarget(selectedIndicator);
                mAnimatorOut.start();
            }
            mLastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void detachAnimator(Animator animator) {
        if (animator.isRunning()) {
            animator.end();
            animator.cancel();
        }
    }

    public DataSetObserver getDataSetObserver() {
        return mInternalDataSetObserver;
    }

    private DataSetObserver mInternalDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            if (pagerAdapter == null) {
                return;
            }
            int newCount = pagerAdapter.getCount();
            int currentCount = getChildCount();
            if (newCount == currentCount) {  // No change
                return;
            } else if (mLastPosition < newCount) {
                mLastPosition = banner.getCurrentItem();
            } else {
                mLastPosition = -1;
            }
            createIndicators();
        }
    };

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        if (banner == null) {
            return;
        }
        banner.removeOnPageChangeListener(onPageChangeListener);
        banner.addOnPageChangeListener(onPageChangeListener);
    }

    private void createIndicators() {
        removeAllViews();
        if (pagerAdapter == null || pagerAdapter.getCount() <= 0)
            return;
        int count = pagerAdapter.getCount();
        int currentItem = banner.getCurrentItem();
        for (int i = 0; i < count; i++) {
            if (currentItem == i) {
                addIndicator(mIndicatorBackgroundResId, mImmediateAnimatorOut);
            } else {
                addIndicator(mIndicatorUnselectedBackgroundResId, mImmediateAnimatorIn);
            }
        }
    }

    private void addIndicator(@DrawableRes int backgroundDrawableId, Animator animator) {
        detachAnimator(animator);
        View indicator = new View(getContext());
        indicator.setBackgroundResource(backgroundDrawableId);
        addView(indicator, mIndicatorWidth, mIndicatorHeight);
        LayoutParams lp = (LayoutParams) indicator.getLayoutParams();
        if (getOrientation() == HORIZONTAL) {
            lp.leftMargin = mIndicatorMargin;
            lp.rightMargin = mIndicatorMargin;
        } else {
            lp.topMargin = mIndicatorMargin;
            lp.bottomMargin = mIndicatorMargin;
        }
        indicator.setLayoutParams(lp);
        animator.setTarget(indicator);
        animator.start();
    }

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        unregisterDataSetObserver();
        super.onDetachedFromWindow();
    }

    public void unregisterDataSetObserver() {
        if (pagerAdapter != null) {
            pagerAdapter.unregisterDataSetObserver(mInternalDataSetObserver);
        }
    }
}
