package com.gallopmark.banner;


import androidx.annotation.NonNull;

/*banner indicator
 * builder and setting values
 * */
public class BannerIndicator {
    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mAnimatorResId;
    private int mAnimatorReverseResId;
    private int mIndicatorBackgroundResId;
    private int mIndicatorUnselectedBackgroundResId;
    private int mIndicatorLayoutMargin; //indicator margin
    private int mIndicatorLayoutMarginLeft; //indicator marginLeft
    private int mIndicatorLayoutMarginRight;    //indicator marginRight
    private int mIndicatorLayoutMarginTop;  //indicator marginTop
    private int mIndicatorLayoutMarginBottom;   //indicator marginBottom
    private int mIndicatorOrientation; //indicator orientation
    private int mIndicatorGravity; //indicator gravity

    public BannerIndicator(@NonNull Builder builder) {
        this.mIndicatorMargin = builder.mIndicatorMargin;
        this.mIndicatorWidth = builder.mIndicatorWidth;
        this.mIndicatorHeight = builder.mIndicatorHeight;
        this.mAnimatorResId = builder.mAnimatorResId;
        this.mAnimatorReverseResId = builder.mAnimatorReverseResId;
        this.mIndicatorBackgroundResId = builder.mIndicatorBackgroundResId;
        this.mIndicatorUnselectedBackgroundResId = builder.mIndicatorUnselectedBackgroundResId;
        this.mIndicatorLayoutMargin = builder.mIndicatorLayoutMargin;
        this.mIndicatorLayoutMarginLeft = builder.mIndicatorLayoutMarginLeft;
        this.mIndicatorLayoutMarginRight = builder.mIndicatorLayoutMarginRight;
        this.mIndicatorLayoutMarginTop = builder.mIndicatorLayoutMarginTop;
        this.mIndicatorLayoutMarginBottom = builder.mIndicatorLayoutMarginBottom;
        this.mIndicatorOrientation = builder.mIndicatorOrientation;
        this.mIndicatorGravity = builder.mIndicatorGravity;
    }

    public int getIndicatorMargin() {
        return mIndicatorMargin;
    }

    public int getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public int getIndicatorAnimatorResId() {
        return mAnimatorResId;
    }

    public int getIndicatorAnimatorReverseResId() {
        return mAnimatorReverseResId;
    }

    public int getIndicatorBackgroundResId() {
        return mIndicatorBackgroundResId;
    }

    public int getIndicatorUnselectedBackgroundResId() {
        return mIndicatorUnselectedBackgroundResId;
    }

    public int getIndicatorLayoutMargin() {
        return mIndicatorLayoutMargin;
    }

    public int getIndicatorLayoutMarginLeft() {
        return mIndicatorLayoutMarginLeft;
    }

    public int getIndicatorLayoutMarginRight() {
        return mIndicatorLayoutMarginRight;
    }

    public int getIndicatorLayoutMarginTop() {
        return mIndicatorLayoutMarginTop;
    }

    public int getIndicatorLayoutMarginBottom() {
        return mIndicatorLayoutMarginBottom;
    }

    public int getIndicatorOrientation() {
        return mIndicatorOrientation;
    }

    public int getIndicatorGravity() {
        return mIndicatorGravity;
    }

    public static class Builder {
        private int mIndicatorMargin = -1;  //指示器（圆点）之间间距
        private int mIndicatorWidth = -1;   //指示器宽度
        private int mIndicatorHeight = -1;  //指示器高度
        private int mAnimatorResId = BannerConfig.INDICATOR_ANIMATOR;
        private int mAnimatorReverseResId = 0;
        private int mIndicatorBackgroundResId = BannerConfig.INDICATOR_BACKGROUND;  //指示器选中背景
        private int mIndicatorUnselectedBackgroundResId = BannerConfig.INDICATOR_UNSELECTED_BACKGROUND; //指示器未选中背景
        private int mIndicatorLayoutMargin = 0; //indicator margin
        private int mIndicatorLayoutMarginLeft = 0; //indicator marginLeft
        private int mIndicatorLayoutMarginRight = 0;    //indicator marginRight
        private int mIndicatorLayoutMarginTop = 0;  //indicator marginTop
        private int mIndicatorLayoutMarginBottom;   //indicator marginBottom
        private int mIndicatorOrientation = -1; //indicator orientation
        private int mIndicatorGravity = -1; //indicator gravity

        public Builder indicatorMargin(int mIndicatorMargin) {
            this.mIndicatorMargin = mIndicatorMargin;
            return this;
        }

        public Builder indicatorWidth(int mIndicatorWidth) {
            this.mIndicatorWidth = mIndicatorWidth;
            return this;
        }

        public Builder indicatorHeight(int mIndicatorHeight) {
            this.mIndicatorHeight = mIndicatorHeight;
            return this;
        }

        public Builder indicatorAnimatorResId(int mAnimatorResId) {
            this.mAnimatorResId = mAnimatorResId;
            return this;
        }

        public Builder indicatorAnimatorReverseResId(int mAnimatorReverseResId) {
            this.mAnimatorReverseResId = mAnimatorReverseResId;
            return this;
        }

        public Builder indicatorBackgroundResId(int mIndicatorBackgroundResId) {
            this.mIndicatorBackgroundResId = mIndicatorBackgroundResId;
            return this;
        }

        public Builder indicatorUnselectedBackgroundResId(int mIndicatorUnselectedBackgroundResId) {
            this.mIndicatorUnselectedBackgroundResId = mIndicatorUnselectedBackgroundResId;
            return this;
        }

        public Builder indicatorLayoutMargin(int mIndicatorLayoutMargin) {
            this.mIndicatorLayoutMargin = mIndicatorLayoutMargin;
            return this;
        }

        public Builder indicatorLayoutMarginLeft(int mIndicatorLayoutMarginLeft) {
            this.mIndicatorLayoutMarginLeft = mIndicatorLayoutMarginLeft;
            return this;
        }

        public Builder indicatorLayoutMarginRight(int mIndicatorLayoutMarginRight) {
            this.mIndicatorLayoutMarginRight = mIndicatorLayoutMarginRight;
            return this;
        }

        public Builder indicatorLayoutMarginTop(int mIndicatorLayoutMarginTop) {
            this.mIndicatorLayoutMarginTop = mIndicatorLayoutMarginTop;
            return this;
        }

        public Builder indicatorLayoutMarginBottom(int mIndicatorLayoutMarginBottom) {
            this.mIndicatorLayoutMarginBottom = mIndicatorLayoutMarginBottom;
            return this;
        }

        public Builder indicatorOrientation(int mIndicatorOrientation) {
            this.mIndicatorOrientation = mIndicatorOrientation;
            return this;
        }

        public Builder indicatorGravity(int mIndicatorGravity) {
            this.mIndicatorGravity = mIndicatorGravity;
            return this;
        }

        public BannerIndicator build() {
            return new BannerIndicator(this);
        }
    }
}
