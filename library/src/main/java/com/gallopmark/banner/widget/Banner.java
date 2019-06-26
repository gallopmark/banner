package com.gallopmark.banner.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.gallopmark.banner.BannerConfig;
import com.gallopmark.banner.BannerIndicator;
import com.gallopmark.banner.R;
import com.gallopmark.banner.Utils;
import com.gallopmark.banner.loader.ImageLoader;
import com.gallopmark.banner.transformer.BannerPageTransformer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*
 *  1、可以自定义adapter
 *  2、可设置是否无限循环
 *  3、可设置指示器
 *  4、可设置图片 withImages，通过startup启动
 * */
public class Banner extends FrameLayout {

    /*适配器转换类，达到无限循环效果 getCount()方法 +2*/
    class WrapperPagerAdapter extends PagerAdapter {

        private PagerAdapter mAdapter;

        private SparseArray<PagerItem> pagerItems;

        private boolean mBoundaryCaching = true;
        private boolean mBoundaryLooping = false;

        void setBoundaryCaching(boolean flag) {
            mBoundaryCaching = flag;
        }

        void setBoundaryLooping(boolean flag) {
            mBoundaryLooping = flag;
        }


        WrapperPagerAdapter(PagerAdapter adapter) {
            this.mAdapter = adapter;
            pagerItems = new SparseArray<>();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return mAdapter.getItemPosition(object);
        }

        int toRealPosition(int position) {
            int realPosition = position;
            int realCount = getRealCount();
            if (realCount == 0) return 0;
            if (mBoundaryLooping) {
                realPosition = (position - 1) % realCount;
                if (realPosition < 0) realPosition += realCount;
            }
            return realPosition;
        }

        int toInnerPosition(int realPosition) {
            int position = (realPosition + 1);
            return mBoundaryLooping ? position : realPosition;
        }

        private int getRealFirstPosition() {
            return mBoundaryLooping ? 1 : 0;
        }

        private int getRealLastPosition() {
            return getRealFirstPosition() + getRealCount() - 1;
        }

        @Override
        public int getCount() {
            int count = getRealCount();
            if (count == 0) return 0;
            return mBoundaryLooping ? count + 2 : count;
        }

        int getRealCount() {
            return mAdapter.getCount();
        }

        PagerAdapter getRealAdapter() {
            return mAdapter;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int realPosition = toRealPosition(position);
            if (mBoundaryCaching) {
                PagerItem pagerItem = pagerItems.get(position);
                if (pagerItem != null) {
                    pagerItems.remove(position);
                    return pagerItem.object;
                }
            }
            return mAdapter.instantiateItem(container, realPosition);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            int realFirst = getRealFirstPosition();
            int realLast = getRealLastPosition();
            int realPosition = toRealPosition(position);
            if (mBoundaryCaching && (position == realFirst || position == realLast)) {
                pagerItems.put(position, new PagerItem(container, realPosition, object));
            } else {
                mAdapter.destroyItem(container, realPosition, object);
            }
        }

        /*
         * Delegate rest of methods directly to the inner adapter.
         */
        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            mAdapter.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return mAdapter.isViewFromObject(view, object);
        }

        @Override
        public void restoreState(Parcelable bundle, ClassLoader classLoader) {
            mAdapter.restoreState(bundle, classLoader);
        }

        @Override
        public Parcelable saveState() {
            return mAdapter.saveState();
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            mAdapter.startUpdate(container);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mAdapter.setPrimaryItem(container, position, object);
        }

        /*
         * End delegation
         */

        /**
         * Container class for caching the boundary views
         */
        class PagerItem {
            ViewGroup container;
            int position;
            Object object;

            PagerItem(ViewGroup container, int position, Object object) {
                this.container = container;
                this.position = position;
                this.object = object;
            }
        }
    }

    static final String TAG = Banner.class.getName();
    private Context mContext;
    private ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;
    private WrapperPagerAdapter mWrapperAdapter;
    private DataSetObserver mAdapterDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            if (mWrapperAdapter != null) {
                mWrapperAdapter.notifyDataSetChanged();
            }
        }
    };

    private List<ViewPager.OnPageChangeListener> mOnPageChangeListeners;

    /*轮播时间间隔*/
    private int duration = BannerConfig.DEFAULT_DURATION;
    /*是否自动播放*/
    private boolean isAutoPlay = BannerConfig.AUTO_PLAY;
    /*单张图是否允许轮播*/
    private boolean isSingleEnabled = BannerConfig.SINGLE_ENABLED;
    /*是否无限循环*/
    private boolean isBoundaryLoop = BannerConfig.IS_LOOP;
    /*是否使用缓存*/
    private boolean isBoundaryCaching = BannerConfig.IS_CACHE;
    /*是否可以滚动*/
    private boolean isScrollable = BannerConfig.IS_SCROLLABLE;
    // 页面边距
    private int pageMargin = BannerConfig.DEFAULT_PAGE_MARGIN;
    // 页面显示屏幕占比
    private float pagePercent = BannerConfig.DEFAULT_PAGE_PERCENT;
    // 缩放和透明比例，需要自己修改想要的比例
    private float scaleMin = BannerConfig.DEFAULT_SCALE;
    private float alphaMin = BannerConfig.DEFAULT_ALPHA;
    /*滚动时间*/
    private int scrollerDuration = BannerConfig.DEFAULT_SCROLLER_DURATION;

    /*mImages*/
    private ImageView.ScaleType mScaleType;
    private List<String> mImages;    //图片数据源
    private ImageLoader mImageLoader;    //图片加载器
    private OnBannerClickListener mOnBannerClickListener;    //点击事件

    /*indicator*/
    private boolean isIndicatorEnabled = BannerConfig.INDICATOR_ENABLED;
    private boolean isIndicatorSingleEnabled = BannerConfig.INDICATOR_SINGLE_ENABLED;
    private PagerIndicator pagerIndicator;
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

    private AutoTask autoTask;

    private class AutoTask implements Runnable {
        @Override
        public void run() {
            /*单张图是否可以轮播*/
            if (mWrapperAdapter != null && (isSingleEnabled ? mWrapperAdapter.getRealCount() > 0 : mWrapperAdapter.getRealCount() > 1)) {
                int position = getCurrentItem() + 1;
                setCurrentItem(position);
            }
        }
    }

    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        obtain(attrs);
        setupView();
    }

    private void obtain(AttributeSet attrs) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pageMargin, metrics);
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.Banner);
        duration = ta.getInteger(R.styleable.Banner_duration, duration);
        isAutoPlay = ta.getBoolean(R.styleable.Banner_auto_play, isAutoPlay);
        isSingleEnabled = ta.getBoolean(R.styleable.Banner_single_enabled, isSingleEnabled);
        isBoundaryLoop = ta.getBoolean(R.styleable.Banner_boundary_loop, isBoundaryLoop);
        isBoundaryCaching = ta.getBoolean(R.styleable.Banner_boundary_cache, isBoundaryCaching);
        isScrollable = ta.getBoolean(R.styleable.Banner_scrollable, isScrollable);
        pageMargin = ta.getDimensionPixelSize(R.styleable.Banner_page_margin, pageMargin);
        pagePercent = ta.getFloat(R.styleable.Banner_page_percent, pagePercent);
        scaleMin = ta.getFloat(R.styleable.Banner_page_scale, scaleMin);
        alphaMin = ta.getFloat(R.styleable.Banner_page_alpha, alphaMin);
        scrollerDuration = ta.getInteger(R.styleable.Banner_scroller_duration, scrollerDuration);
        int imageScaleType = ta.getInt(R.styleable.Banner_image_scaleType, 1);
        setupScaleType(imageScaleType);

        /*indicator*/
        isIndicatorEnabled = ta.getBoolean(R.styleable.Banner_indicator_enabled, isIndicatorEnabled);
        isIndicatorSingleEnabled = ta.getBoolean(R.styleable.Banner_indicator_single_enabled, isIndicatorSingleEnabled);
        mIndicatorWidth = ta.getDimensionPixelSize(R.styleable.Banner_indicator_width, -1);
        mIndicatorHeight = ta.getDimensionPixelSize(R.styleable.Banner_indicator_height, -1);
        mIndicatorMargin = ta.getDimensionPixelSize(R.styleable.Banner_indicator_margin, -1);
        mAnimatorResId = ta.getResourceId(R.styleable.Banner_indicator_animator, mAnimatorResId);
        mAnimatorReverseResId = ta.getResourceId(R.styleable.Banner_indicator_animator_reverse, 0);
        mIndicatorBackgroundResId = ta.getResourceId(R.styleable.Banner_indicator_drawable_selected, mIndicatorBackgroundResId);
        mIndicatorUnselectedBackgroundResId = ta.getResourceId(R.styleable.Banner_indicator_drawable_unselected, mIndicatorUnselectedBackgroundResId);
        mIndicatorLayoutMargin = ta.getDimensionPixelSize(R.styleable.Banner_indicator_layout_margin, defaultMargin());
        mIndicatorLayoutMarginLeft = ta.getDimensionPixelSize(R.styleable.Banner_indicator_layout_margin_left, defaultMargin());
        mIndicatorLayoutMarginRight = ta.getDimensionPixelSize(R.styleable.Banner_indicator_layout_margin_right, defaultMargin());
        mIndicatorLayoutMarginTop = ta.getDimensionPixelSize(R.styleable.Banner_indicator_layout_margin_top, defaultMargin());
        mIndicatorLayoutMarginBottom = ta.getDimensionPixelSize(R.styleable.Banner_indicator_layout_margin_bottom, defaultMargin());
        mIndicatorOrientation = ta.getInt(R.styleable.Banner_indicator_orientation, 0);
        mIndicatorGravity = ta.getInt(R.styleable.Banner_indicator_gravity, -1);
        ta.recycle();
    }

    private void setupScaleType(int imageScaleType) {
        switch (imageScaleType) {
            case 0:
                mScaleType = ImageView.ScaleType.CENTER;
                break;
            case 1:
                mScaleType = ImageView.ScaleType.CENTER_CROP;
                break;
            case 2:
                mScaleType = ImageView.ScaleType.CENTER_INSIDE;
                break;
            case 3:
                mScaleType = ImageView.ScaleType.FIT_XY;
                break;
            case 4:
                mScaleType = ImageView.ScaleType.FIT_CENTER;
                break;
            case 5:
                mScaleType = ImageView.ScaleType.FIT_START;
                break;
            case 6:
                mScaleType = ImageView.ScaleType.FIT_END;
                break;
            case 7:
                mScaleType = ImageView.ScaleType.MATRIX;
                break;
        }
    }

    int defaultMargin() {
        return Utils.dip2px(mContext, BannerConfig.DEFAULT_INDICATOR_LAYOUT_MARGIN);
    }

    final int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private void setupView() {
        setClipChildren(false);
        mViewPager = new ViewPager(mContext);
        mViewPager.setClipChildren(false);
        int realWidth = LayoutParams.MATCH_PARENT;
        if (pagePercent != 1f) {
            realWidth = (int) (getScreenWidth() * pagePercent);
        }
        LayoutParams layoutParams = new LayoutParams(realWidth, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);
        mViewPager.setPageMargin(pageMargin);
        mViewPager.setPageTransformer(false, new BannerPageTransformer(scaleMin, alphaMin));
        setupScroller();
        mViewPager.removeOnPageChangeListener(onPageChangeListener);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        addView(mViewPager);
        autoTask = new AutoTask();
    }

    class BannerScroller extends Scroller {
        private static final int DEFAULT_TIME = 800;
        private int mDuration = DEFAULT_TIME;

        void setDuration(int time) {
            mDuration = time;
        }

        BannerScroller(Context context) {
            super(context);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    private void setupScroller() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            BannerScroller mScroller = new BannerScroller(mContext);
            mScroller.setDuration(scrollerDuration);
            mField.set(mViewPager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * helper function which may be used when implementing FragmentPagerAdapter
     *
     * @return (position - 1)%count
     */
    public int toRealPosition(int position, int count) {
        position = position - 1;
        if (position < 0) {
            position += count;
        } else {
            position = position % count;
        }
        return position;
    }

    /**
     * If set to true, the boundary views (i.e. first and last) will never be
     * destroyed This may help to prevent "blinking" of some views
     */
    public void setBoundaryCaching(boolean flag) {
        isBoundaryCaching = flag;
        if (mWrapperAdapter != null) {
            mWrapperAdapter.setBoundaryCaching(flag);
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setScaleType(@Nullable ImageView.ScaleType mScaleType) {
        this.mScaleType = mScaleType;
    }

    public void setSingleEnabled(boolean singleEnabled) {
        isSingleEnabled = singleEnabled;
    }

    public void setAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        startAutoPlay();
    }

    public void startAutoPlay() {
        if (isAutoPlay) {
            removeCallbacks(autoTask);
            postDelayed(autoTask, duration);
        }
    }

    public void stopAutoPlay() {
        if (isAutoPlay) {
            removeCallbacks(autoTask);
        }
    }

    /*是否无限循环*/
    public void setBoundaryLooping(boolean flag) {
        isBoundaryLoop = flag;
        if (mWrapperAdapter != null) {
            mWrapperAdapter.setBoundaryLooping(flag);
        }
    }

    public void setBannerIndicator(@NonNull BannerIndicator indicator) {
        setIndicatorMargin(indicator.getIndicatorMargin());
        setIndicatorWidth(indicator.getIndicatorWidth());
        setIndicatorHeight(indicator.getIndicatorHeight());
        setIndicatorAnimatorResId(indicator.getIndicatorAnimatorResId());
        setIndicatorAnimatorReverseResId(indicator.getIndicatorAnimatorReverseResId());
        setIndicatorBackgroundResId(indicator.getIndicatorBackgroundResId());
        setIndicatorUnselectedBackgroundResId(indicator.getIndicatorUnselectedBackgroundResId());
        setIndicatorLayoutMargin(indicator.getIndicatorLayoutMargin());
        setIndicatorLayoutMarginLeft(indicator.getIndicatorLayoutMarginLeft());
        setIndicatorLayoutMarginRight(indicator.getIndicatorLayoutMarginRight());
        setIndicatorLayoutMarginTop(indicator.getIndicatorLayoutMarginTop());
        setIndicatorLayoutMarginBottom(indicator.getIndicatorLayoutMarginBottom());
        setIndicatorOrientation(indicator.getIndicatorOrientation());
        setIndicatorGravity(indicator.getIndicatorGravity());
    }

    public void setIndicatorMargin(int mIndicatorMargin) {
        this.mIndicatorMargin = mIndicatorMargin;
    }

    public void setIndicatorWidth(int mIndicatorWidth) {
        this.mIndicatorWidth = mIndicatorWidth;
    }

    public void setIndicatorHeight(int mIndicatorHeight) {
        this.mIndicatorHeight = mIndicatorHeight;
    }

    public void setIndicatorAnimatorResId(int mAnimatorResId) {
        this.mAnimatorResId = mAnimatorResId;
    }

    public void setIndicatorAnimatorReverseResId(int mAnimatorReverseResId) {
        this.mAnimatorReverseResId = mAnimatorReverseResId;
    }

    public void setIndicatorBackgroundResId(int mIndicatorBackgroundResId) {
        this.mIndicatorBackgroundResId = mIndicatorBackgroundResId;
    }

    public void setIndicatorUnselectedBackgroundResId(int mIndicatorUnselectedBackgroundResId) {
        this.mIndicatorUnselectedBackgroundResId = mIndicatorUnselectedBackgroundResId;
    }

    public void setIndicatorLayoutMargin(int mIndicatorLayoutMargin) {
        this.mIndicatorLayoutMargin = mIndicatorLayoutMargin;
    }

    public void setIndicatorLayoutMarginLeft(int mIndicatorLayoutMarginLeft) {
        this.mIndicatorLayoutMarginLeft = mIndicatorLayoutMarginLeft;
    }

    public void setIndicatorLayoutMarginRight(int mIndicatorLayoutMarginRight) {
        this.mIndicatorLayoutMarginRight = mIndicatorLayoutMarginRight;
    }

    public void setIndicatorLayoutMarginTop(int mIndicatorLayoutMarginTop) {
        this.mIndicatorLayoutMarginTop = mIndicatorLayoutMarginTop;
    }

    public void setIndicatorLayoutMarginBottom(int mIndicatorLayoutMarginBottom) {
        this.mIndicatorLayoutMarginBottom = mIndicatorLayoutMarginBottom;
    }

    public void setIndicatorOrientation(int mIndicatorOrientation) {
        this.mIndicatorOrientation = mIndicatorOrientation;
    }

    public void setIndicatorGravity(int mIndicatorGravity) {
        this.mIndicatorGravity = mIndicatorGravity;
    }

    public class BannerInitializer {

        BannerInitializer(@NonNull List<String> images) {
            if (mImages == null) {
                mImages = new ArrayList<>();
            } else {
                mImages.clear();
            }
            mImages.addAll(images);
        }

        /*设置图片加载器*/
        public BannerInitializer withImageLoader(ImageLoader imageLoader) {
            mImageLoader = imageLoader;
            return this;
        }

        public BannerInitializer withImageScaleType(ImageView.ScaleType imageScaleType) {
            mScaleType = imageScaleType;
            return this;
        }

        /*设置banner点击事件*/
        public BannerInitializer withBannerClickListener(OnBannerClickListener onBannerClickListener) {
            mOnBannerClickListener = onBannerClickListener;
            return this;
        }

        public BannerInitializer autoPlay(boolean autoPlay) {
            isAutoPlay = autoPlay;
            return this;
        }

        public BannerInitializer loop(boolean loop) {
            isBoundaryLoop = loop;
            return this;
        }

        /*此方法要在setAdapter或startup方法之前调用，否则无效*/
        public BannerInitializer indicatorEnabled(boolean indicatorEnabled) {
            isIndicatorEnabled = indicatorEnabled;
            if (getAdapter() != null) {
                Log.e(TAG, "please use setIndicatorEnabled before startup");
            }
            return this;
        }

        public BannerInitializer indicatorSingleEnabled(boolean flag) {
            isIndicatorSingleEnabled = flag;
            return this;
        }

        public BannerInitializer indicator(BannerIndicator indicator) {
            setBannerIndicator(indicator);
            return this;
        }

        public BannerInitializer indicatorMargin(int indicatorMargin) {
            setIndicatorMargin(indicatorMargin);
            return this;
        }

        public BannerInitializer indicatorWidth(int indicatorWidth) {
            setIndicatorWidth(indicatorWidth);
            return this;
        }

        public BannerInitializer indicatorHeight(int indicatorHeight) {
            setIndicatorHeight(indicatorHeight);
            return this;
        }

        public BannerInitializer indicatorAnimatorResId(int animatorResId) {
            setIndicatorAnimatorResId(animatorResId);
            return this;
        }

        public BannerInitializer indicatorAnimatorReverseResId(int animatorReverseResId) {
            setIndicatorAnimatorReverseResId(animatorReverseResId);
            return this;
        }

        public BannerInitializer indicatorBackgroundResId(int indicatorBackgroundResId) {
            setIndicatorBackgroundResId(indicatorBackgroundResId);
            return this;
        }

        public BannerInitializer indicatorUnselectedBackgroundResId(int indicatorUnselectedBackgroundResId) {
            setIndicatorUnselectedBackgroundResId(indicatorUnselectedBackgroundResId);
            return this;
        }

        public BannerInitializer indicatorLayoutMargin(int indicatorLayoutMargin) {
            setIndicatorLayoutMargin(indicatorLayoutMargin);
            return this;
        }

        public BannerInitializer indicatorLayoutMarginLeft(int indicatorLayoutMarginLeft) {
            setIndicatorLayoutMarginLeft(indicatorLayoutMarginLeft);
            return this;
        }

        public BannerInitializer indicatorLayoutMarginRight(int indicatorLayoutMarginRight) {
            setIndicatorLayoutMarginRight(indicatorLayoutMarginRight);
            return this;
        }

        public BannerInitializer indicatorLayoutMarginTop(int indicatorLayoutMarginTop) {
            setIndicatorLayoutMarginTop(indicatorLayoutMarginTop);
            return this;
        }

        public BannerInitializer indicatorLayoutMarginBottom(int indicatorLayoutMarginBottom) {
            setIndicatorLayoutMarginBottom(indicatorLayoutMarginBottom);
            return this;
        }

        public BannerInitializer indicatorOrientation(int indicatorOrientation) {
            setIndicatorOrientation(indicatorOrientation);
            return this;
        }

        public BannerInitializer indicatorGravity(int indicatorGravity) {
            setIndicatorGravity(indicatorGravity);
            return this;
        }

        /*一定要调用此方法 否则banner无效*/
        public void startup() {
            if (mImageLoader == null) {
                Log.e(TAG, "please set your mImageLoader");
                return;
            }
            setAdapter(new ImageBannerAdapter(mImageLoader));
        }
    }

    public BannerInitializer initializer(@NonNull List<String> images) {
        return new BannerInitializer(images);
    }

    /*设置指示器*/
    private void setupIndicator() {
        if (pagerIndicator == null) {
            pagerIndicator = new PagerIndicator(mContext);
            pagerIndicator.setOrientation(mIndicatorOrientation == 0 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = mIndicatorGravity >= 0 ? mIndicatorGravity : Gravity.END | Gravity.BOTTOM;
            if (mIndicatorLayoutMargin != 0) {
                params.setMargins(mIndicatorLayoutMargin, mIndicatorLayoutMargin, mIndicatorLayoutMargin, mIndicatorLayoutMargin);
            } else {
                params.setMargins(mIndicatorLayoutMarginLeft, mIndicatorLayoutMarginTop, mIndicatorLayoutMarginRight, mIndicatorLayoutMarginBottom);
            }
            pagerIndicator.setLayoutParams(params);
            pagerIndicator.configureIndicator(mIndicatorWidth, mIndicatorHeight, mIndicatorMargin, mAnimatorResId, mAnimatorReverseResId,
                    mIndicatorBackgroundResId, mIndicatorUnselectedBackgroundResId);
            pagerIndicator.setSingleEnabled(isIndicatorSingleEnabled).setupBanner(this);
            addView(pagerIndicator);
        }
    }

    class ImageBannerAdapter extends PagerAdapter {
        private ImageLoader imageLoader;

        ImageBannerAdapter(@NonNull ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final ImageView imageView = imageLoader.createImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (mScaleType != null) {
                imageView.setScaleType(mScaleType);
            }
            imageLoader.displayImage(mContext, mImages.get(position), imageView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnBannerClickListener != null) {
                        mOnBannerClickListener.onBannerClick(imageView, position);
                    }
                }
            });
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public interface OnBannerClickListener {
        void onBannerClick(@NonNull View view, int position);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setPageTransformer(boolean reverseDrawingOrder, @Nullable ViewPager.PageTransformer transformer) {
        mViewPager.setPageTransformer(reverseDrawingOrder, transformer);
    }

    public void setPageTransformer(boolean reverseDrawingOrder, @Nullable ViewPager.PageTransformer transformer, int pageLayerType) {
        mViewPager.setPageTransformer(reverseDrawingOrder, transformer, pageLayerType);
    }

    public void setOffscreenPageLimit(int limit) {
        mViewPager.setOffscreenPageLimit(limit);
    }

    public void setPageMarginDrawable(@DrawableRes int resId) {
        mViewPager.setPageMarginDrawable(resId);
    }

    public void setPageMarginDrawable(@Nullable Drawable drawable) {
        mViewPager.setPageMarginDrawable(drawable);
    }

    public void setAdapter(@NonNull PagerAdapter adapter) {
        // 为了防止多次设置Adapter
        if (mPagerAdapter != null) {
            mPagerAdapter.unregisterDataSetObserver(mAdapterDataObserver);
            mPagerAdapter = null;
        }
        mPagerAdapter = adapter;
        mWrapperAdapter = new WrapperPagerAdapter(mPagerAdapter);
        mWrapperAdapter.setBoundaryCaching(isBoundaryCaching);
        mWrapperAdapter.setBoundaryLooping(isBoundaryLoop);
        mViewPager.setAdapter(mWrapperAdapter);
        mPagerAdapter.registerDataSetObserver(mAdapterDataObserver);
        setCurrentItem(0, false);
        if (isIndicatorEnabled) {
            setupIndicator();
        }
        startAutoPlay();
    }

    public PagerAdapter getAdapter() {
        return mWrapperAdapter != null ? mWrapperAdapter.getRealAdapter() : null;
    }

    public int getCurrentItem() {
        return mWrapperAdapter != null ? mWrapperAdapter.toRealPosition(mViewPager.getCurrentItem()) : 0;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (mWrapperAdapter == null) return;
        int realItem = mWrapperAdapter.toInnerPosition(item);
        mViewPager.setCurrentItem(realItem, smoothScroll);
    }

    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }

    public void setOnPageChangeListener(@NonNull ViewPager.OnPageChangeListener listener) {
        addOnPageChangeListener(listener);
    }

    public void addOnPageChangeListener(@NonNull ViewPager.OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    public void removeOnPageChangeListener(@NonNull ViewPager.OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    public void clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        private float mPreviousOffset = -1;
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {
            if (mWrapperAdapter != null) {
                int realPosition = mWrapperAdapter.toRealPosition(position);
                if (mPreviousPosition != realPosition) {
                    mPreviousPosition = realPosition;
                    if (mOnPageChangeListeners != null) {
                        for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                            ViewPager.OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                            if (listener != null) {
                                listener.onPageSelected(realPosition);
                            }
                        }
                    }
                }
            }
            startAutoPlay();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realPosition = position;
            if (mWrapperAdapter != null) {
                realPosition = mWrapperAdapter.toRealPosition(position);
                if (positionOffset == 0 && mPreviousOffset == 0 && (position == 0 || position == mWrapperAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            mPreviousOffset = positionOffset;
            if (mWrapperAdapter != null && mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    ViewPager.OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        if (realPosition != mWrapperAdapter.getRealCount() - 1) {
                            listener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
                        } else {
                            if (positionOffset > .5) {
                                listener.onPageScrolled(0, 0, 0);
                            } else {
                                listener.onPageScrolled(realPosition, 0, 0);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mWrapperAdapter != null) {
                int position = mViewPager.getCurrentItem();
                int realPosition = mWrapperAdapter.toRealPosition(position);
                if (state == ViewPager.SCROLL_STATE_IDLE && (position == 0 || position == mWrapperAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    ViewPager.OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrollStateChanged(state);
                    }
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                break;
            default:
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDispatchListener != null) {
            mDispatchListener.onDispatchKeyEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAutoPlay();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                startAutoPlay();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private OnDispatchTouchEventListener mDispatchListener;

    public void setOnDispatchTouchEventListener(OnDispatchTouchEventListener listener) {
        mDispatchListener = listener;
    }

    public interface OnDispatchTouchEventListener {
        void onDispatchKeyEvent(MotionEvent event);
    }

    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isScrollable) {
            return false;
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();
    }

    /*释放资源*/
    protected void release() {
        stopAutoPlay();
        if (mPagerAdapter != null) {
            mPagerAdapter.unregisterDataSetObserver(mAdapterDataObserver);
        }
    }
}
