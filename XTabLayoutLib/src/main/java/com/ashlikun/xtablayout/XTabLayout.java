package com.ashlikun.xtablayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pools;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import static android.R.attr.maxWidth;
import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * @author　　: 李坤
 * 创建时间: 2018/11/8 9:29
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：自定义和官方一样的TabLayout
 */

public class XTabLayout extends HorizontalScrollView {

    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;
    private static final int DEFAULT_GAP_TEXT_ICON = 5;
    private static final int INVALID_WIDTH = -1;
    private static final int DEFAULT_HEIGHT = 48;
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private static final int FIXED_WRAP_GUTTER_MIN = 16;
    private static final int MOTION_NON_ADJACENT_OFFSET = 24;

    private static final int ANIMATION_DURATION = 300;

    private static final Pools.Pool<Tab> sTabPool = new Pools.SynchronizedPool<>(16);
    /**
     * 文本字母是否小写转大写
     */
    private boolean xTabTextAllCaps = false;


    /**
     * 可滚动选项卡在任何给定时刻显示选项卡的子集，并且可以包含较长的选项卡
     * 标签和更多的标签。它们最好用于触摸时浏览上下文
     * 当用户不需要直接比较标签标签时的界面。
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_SCROLLABLE = 0;

    /**
     * 固定的选项卡同时显示所有选项卡，最好与受益的内容一起使用
     * 在选项卡之间快速旋转。选项卡的最大数量受视图宽度的限制。
     * 固定标签有相等的宽度，基于最宽的标签标签。
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_FIXED = 1;
    /**
     * 自动计算宽度，当宽度大于Tablayout宽度就可以滑动
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_AUTO = 2;

    /**
     * @hide
     */
    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    /**
     * Gravity用于填充{@link XTabLayout}尽可能多。此选项仅生效
     * 当与{@link #MODE_FIXED}一起使用时。
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_FILL = 0;

    /**
     * 重力用于在{@link XTabLayout}的中心布局选项卡。
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_CENTER = 1;

    /**
     * @hide
     */
    @IntDef(flag = true, value = {GRAVITY_FILL, GRAVITY_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    /**
     * 当选项卡的选择状态改变时调用回调接口。
     */
    public interface OnTabSelectedListener {

        /**
         * 当选项卡进入所选状态时调用。
         *
         * @param tab 选中的选项卡
         */
        void onTabSelected(Tab tab);

        /**
         * 当选项卡退出所选状态时调用。
         *
         * @param tab 未选中的选项卡
         */
        void onTabUnselected(Tab tab);

        /**
         * 当用户再次选择已选中的选项卡时调用。一些应用程序
         * 可使用此操作返回到类别的最高级别。
         *
         * @param tab 重新选择的选项卡。
         */
        void onTabReselected(Tab tab);
    }

    private final ArrayList<Tab> mTabs = new ArrayList<>();
    private Tab mSelectedTab;

    private final SlidingTabStrip mTabStrip;

    private int mTabPaddingStart;
    private int mTabPaddingTop;
    private int mTabPaddingEnd;
    private int mTabPaddingBottom;

    private int mTabTextAppearance;
    private ColorStateList mTabTextColors;
    private float mTabTextSize = 0;
    private boolean xTabTextBold;
    private float mTabSelectedTextSize = 0;
    private boolean xTabTextSelectedBold;
    private float mTabTextMultiLineSize;

    private Drawable xTabItemBackground = null;
    private Drawable xTabItemSelectedBackground = null;

    private int mTabMaxWidth = Integer.MAX_VALUE;
    private final int mRequestedTabMinWidth;
    private final int mRequestedTabMaxWidth;
    private int xTabDisplayNum;
    private final int mScrollableTabMinWidth;

    private int mContentInsetStart;

    private int mTabGravity;
    private int mMode;

    private int dividerWidth;
    private int dividerHeight;
    private int dividerColor;
    private int dividerGravity;
    /**
     * 图标与文字距离
     */
    private int iconAndTextSpace;


    private OnTabSelectedListener mSelectedListener;
    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private OnTabSelectedListener mCurrentVpSelectedListener;

    private ValueAnimator mScrollAnimator;

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;
    private TabLayoutOnPageChangeListener mPageChangeListener;
    private AdapterChangeListener mAdapterChangeListener;
    //是否布局完成
    private boolean onLayoutOk = false;

    /**
     * 我们使用池作为一个简单的回收桶
     */
    private final Pools.Pool<TabView> mTabViewPool = new Pools.SimplePool<>(12);

    public XTabLayout(Context context) {
        this(context, null);
    }

    public XTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ThemeUtils.checkAppCompatTheme(context);

        //禁用滚动条
        setHorizontalScrollBarEnabled(false);

        // 添加TabStrip
        mTabStrip = new SlidingTabStrip(context);
        super.addView(mTabStrip, 0, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XTabLayout,
                defStyleAttr, R.style.Widget_Design_TabLayout);

        mTabStrip.setSelectedIndicatorHeight(
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIndicatorHeight, dpToPx(2)));
        mTabStrip.setSelectedIndicatorWidth(
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIndicatorWidth, 0));
        mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.XTabLayout_xTabIndicatorColor, 0));
        mTabStrip.setSelectedIndicatorDrawable(a.getDrawable(R.styleable.XTabLayout_xTabIndicatorDrawable));
        mTabStrip.setWidthText(a.getBoolean(R.styleable.XTabLayout_xTabIndicatorWidthWidthText, false));

        mTabPaddingStart = mTabPaddingTop = mTabPaddingEnd = mTabPaddingBottom = a
                .getDimensionPixelSize(R.styleable.XTabLayout_xTabPadding, 0);
        mTabPaddingStart = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingStart,
                mTabPaddingStart);
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingTop,
                mTabPaddingTop);
        mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingEnd,
                mTabPaddingEnd);
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingBottom,
                mTabPaddingBottom);


        xTabTextAllCaps = a.getBoolean(R.styleable.XTabLayout_xTabTextAllCaps, false);

        mTabTextAppearance = a.getResourceId(R.styleable.XTabLayout_xTabTextAppearance,
                R.style.TextAppearance_Design_Tab);
        mTabTextSize = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabTextSize, 0);
        xTabTextBold = a.getBoolean(R.styleable.XTabLayout_xTabTextBold, false);

        xTabTextSelectedBold = a.getBoolean(R.styleable.XTabLayout_xTabTextSelectedBold, false);

        // Text colors/sizes come from the text appearance first
        final TypedArray ta = context.obtainStyledAttributes(mTabTextAppearance,
                R.styleable.TextAppearance);
        try {
            if (mTabTextSize == 0) {
                mTabTextSize = ta.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
            }
            mTabTextColors = ta.getColorStateList(R.styleable.TextAppearance_android_textColor);
        } finally {
            ta.recycle();
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabSelectedTextSize)) {
            mTabSelectedTextSize = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabSelectedTextSize, 0);
        } else {
            mTabSelectedTextSize = mTabTextSize;
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabTextColor)) {
            // If we have an explicit text color set, use it instead
            mTabTextColors = a.getColorStateList(R.styleable.XTabLayout_xTabTextColor);
        }

        if (a.hasValue(R.styleable.XTabLayout_xTabSelectedTextColor)) {
            // We have an explicit selected text color set, so we need to make merge it with the
            // current colors. This is exposed so that developers can use theme attributes to set
            // this (theme attrs in ColorStateLists are Lollipop+)
            final int selected = a.getColor(R.styleable.XTabLayout_xTabSelectedTextColor, 0);
            mTabTextColors = createColorStateList(mTabTextColors.getDefaultColor(), selected);
        }

        xTabDisplayNum = a.getInt(R.styleable.XTabLayout_xTabDisplayNum, 0);
        mRequestedTabMinWidth = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabMinWidth,
                INVALID_WIDTH);
        mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabMaxWidth,
                INVALID_WIDTH);
        if (a.hasValue(R.styleable.XTabLayout_xTabItemBackground)) {
            xTabItemBackground = a.getDrawable(R.styleable.XTabLayout_xTabItemBackground);
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabItemSelectedBackground)) {
            xTabItemSelectedBackground = a.getDrawable(R.styleable.XTabLayout_xTabItemSelectedBackground);
        }

        mContentInsetStart = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabContentStart, 0);
        mMode = a.getInt(R.styleable.XTabLayout_xTabMode, MODE_AUTO);
        mTabGravity = a.getInt(R.styleable.XTabLayout_xTabGravity, GRAVITY_FILL);

        dividerWidth = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabDividerWidth, 0);
        dividerHeight = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabDividerHeight, 0);

        dividerColor = a.getColor(R.styleable.XTabLayout_xTabDividerColor, Color.BLACK);
        dividerGravity = a.getInteger(R.styleable.XTabLayout_xTabDividerGravity, DividerDrawable.CENTER);

        iconAndTextSpace = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIconAndTextSpace, dpToPx(DEFAULT_GAP_TEXT_ICON));
        a.recycle();

        final Resources res = getResources();
        mTabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
        mScrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);

        // Now apply the tab mode and gravity
        applyModeAndGravity(true);

        //添加分割线
        addDivider();
    }

    /**
     * 添加分割线
     */
    private void addDivider() {
        post(new Runnable() {
            @Override
            public void run() {
                if (dividerWidth > 0) {
                    mTabStrip.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                    DividerDrawable dividerDrawable = new DividerDrawable(getContext());
                    dividerDrawable.setDividerSize(dividerWidth, dividerHeight);
                    dividerDrawable.setColor(dividerColor);
                    dividerDrawable.setGravity(dividerGravity);
                    mTabStrip.setDividerDrawable(dividerDrawable);
                }
            }
        });
    }

    /**
     * 设置分割线长宽
     *
     * @param width
     * @param height 当height =0 时，则分割线长度占满
     */
    public void setDividerSize(int width, int height) {
        dividerWidth = width;
        dividerHeight = height;
        addDivider();
    }

    /**
     * 设置分割线颜色
     *
     * @param color
     */
    public void setDividerColor(int color) {
        dividerColor = color;
        addDivider();
    }

    /**
     * 设置分割线位置
     */
    public void setDividerGravity(int gravity) {
        dividerGravity = gravity;
        addDivider();

    }

    /**
     * 设置当前的位置
     *
     * @param position
     */
    public void setCurrentTabPosition(final int position, final boolean isNotifica) {
        if (position >= 0 && position < getTabCount()) {
            if (onLayoutOk) {
                selectTab(getTabAt(position), true, isNotifica);
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setCurrentTabPosition(position, isNotifica);
                    }
                }, 50);
            }

        }
    }

    /**
     * 设置当前的位置
     *
     * @param position
     */
    public void setCurrentTabPosition(final int position) {
        setCurrentTabPosition(position, true);
    }

    /**
     * 设置字母是否自动小写转大写
     */
    public void setAllCaps(boolean allCaps) {
        xTabTextAllCaps = allCaps;
//        invalidate();
    }

    /**
     * Sets the tab indicator's color for the currently selected tab.
     *
     * @param color color to use for the indicator
     * @attr ref R.styleable#TabLayout_tabIndicatorColor
     */
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        mTabStrip.setSelectedIndicatorColor(color);
    }

    /**
     * 设置指示器的Drawable
     *
     * @attr ref R.styleable#xTabIndicatorDrawable
     */
    public void setSelectedTabIndicatorDrawable(Drawable drawable) {
        mTabStrip.setSelectedIndicatorDrawable(drawable);
    }

    public void setSelectedTabIndicatorDrawable(@DrawableRes int drawable) {
        mTabStrip.setSelectedIndicatorDrawable(getResources().getDrawable(drawable));
    }

    /**
     * Sets the tab indicator's height for the currently selected tab.
     *
     * @param height height to use for the indicator in pixels
     * @attr ref R.styleable#TabLayout_tabIndicatorHeight
     */
    public void setSelectedTabIndicatorHeight(int height) {
        mTabStrip.setSelectedIndicatorHeight(height);
    }

    public void setxTabDisplayNum(int xTabDisplayNum) {
        this.xTabDisplayNum = xTabDisplayNum;
    }

    /**
     * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
     * part of a scrolling container such as {@link ViewPager}.
     * <p>
     * Calling this method does not update the selected tab, it is only used for drawing purposes.
     *
     * @param position           current scroll position
     * @param positionOffset     Value from [0, 1) indicating the offset from {@code position}.
     * @param updateSelectedText Whether to update the text's selected state.
     */
    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText,
                                  boolean updateIndicatorPosition) {
        final int roundedPosition = Math.round(position + positionOffset);
        if (roundedPosition < 0 || roundedPosition >= mTabStrip.getChildCount()) {
            return;
        }

        // Set the indicator position, if enabled
        if (updateIndicatorPosition) {
            mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
        }

        // Now update the scroll position, canceling any running animation
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.cancel();
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0);

        // Update the 'selected state' view as we scroll, if enabled
        if (updateSelectedText) {
            setSelectedTabView(roundedPosition);
        }
    }


    private float getScrollPosition() {
        return mTabStrip.getIndicatorPosition();
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     * If this is the first tab to be added it will become the selected tab.
     *
     * @param tab Tab to add
     */
    public void addTab(@NonNull Tab tab) {
        addTab(tab, mTabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>.
     * If this is the first tab to be added it will become the selected tab.
     *
     * @param tab      The tab to add
     * @param position The new position of the tab
     */
    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, mTabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     *
     * @param tab         Tab to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }
        if (mTabs.isEmpty()) {
            //没有tab就设置tabposition为0
            tab.setPosition(0);
        }
        addTabView(tab, setSelected);
        configureTab(tab, mTabs.size());
        if (setSelected) {
            tab.select();
        }
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>.
     *
     * @param tab         The tab to add
     * @param position    The new position of the tab
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }

        addTabView(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            tab.select();
        }
    }

    private void addTabFromItemView(@NonNull TabItem item) {
        final Tab tab = newTab();
        if (item.mText != null) {
            tab.setText(item.mText);
        }
        if (item.mIcon != null) {
            tab.setIcon(item.mIcon);
        }
        if (item.mCustomLayout != 0) {
            tab.setCustomView(item.mCustomLayout);
        }
        addTab(tab);
    }

    public void setTabItemBackground(Drawable tabItemBackground) {
        if (this.xTabItemBackground == tabItemBackground) {
            return;
        }
        this.xTabItemBackground = tabItemBackground;
        if (mSelectedTab != null) {
            selectTab(mSelectedTab);
        }
    }

    /**
     * 获取指示器
     *
     * @return
     */
    public SlidingTabStrip getIndicator() {
        return mTabStrip;
    }

    public void setTabItemSelectedBackground(Drawable tabItemSelectedBackground) {
        if (this.xTabItemSelectedBackground == tabItemSelectedBackground) {
            return;
        }
        this.xTabItemSelectedBackground = tabItemSelectedBackground;
        if (mSelectedTab != null) {
            selectTab(mSelectedTab);
        }
    }

    /**
     * @deprecated Use {@link #addOnTabSelectedListener(XTabLayout.OnTabSelectedListener)} and
     * {@link #removeOnTabSelectedListener(XTabLayout.OnTabSelectedListener)}.
     */
    @Deprecated
    public void setOnTabSelectedListener(@Nullable XTabLayout.OnTabSelectedListener listener) {
        // The logic in this method emulates what we had before support for multiple
        // registered listeners.
        if (mSelectedListener != null) {
            removeOnTabSelectedListener(mSelectedListener);
        }
        // Update the deprecated field so that we can remove the passed listener the next
        // time we're called
        mSelectedListener = listener;
        if (listener != null) {
            addOnTabSelectedListener(listener);
        }
    }

    /**
     * Add a {@link XTabLayout.OnTabSelectedListener} that will be invoked when tab selection
     * changes.
     *
     * <p>Components that add a listener should take care to remove it when finished via
     * {@link #removeOnTabSelectedListener(XTabLayout.OnTabSelectedListener)}.</p>
     *
     * @param listener listener to add
     */
    public void addOnTabSelectedListener(@NonNull XTabLayout.OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    /**
     * Remove the given {@link XTabLayout.OnTabSelectedListener} that was previously added via
     * {@link #addOnTabSelectedListener(XTabLayout.OnTabSelectedListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnTabSelectedListener(@NonNull XTabLayout.OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    /**
     * Remove all previously added {@link XTabLayout.OnTabSelectedListener}s.
     */
    public void clearOnTabSelectedListeners() {
        mSelectedListeners.clear();
    }

    @NonNull
    public Tab newTab() {
        Tab tab = sTabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }
        tab.mParent = this;
        tab.mView = createTabView(tab);
        return tab;
    }

    /**
     * Returns the number of tabs currently registered with the action bar.
     *
     * @return Tab count
     */
    public int getTabCount() {
        return mTabs.size();
    }

    /**
     * Returns the tab at the specified index.
     */
    @Nullable
    public Tab getTabAt(int index) {
        return mTabs.get(index);
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or {@code -1} if there isn't a selected tab.
     */
    public int getSelectedTabPosition() {
        return mSelectedTab != null ? mSelectedTab.getPosition() : -1;
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param tab The tab to remove
     */
    public void removeTab(Tab tab) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
        }

        removeTabAt(tab.getPosition());
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param position Position of the tab to remove
     */
    public void removeTabAt(int position) {
        final int selectedTabPosition = mSelectedTab != null ? mSelectedTab.getPosition() : 0;
        removeTabViewAt(position);

        final Tab removedTab = mTabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            sTabPool.release(removedTab);
        }

        final int newTabCount = mTabs.size();
        for (int i = position; i < newTabCount; i++) {
            mTabs.get(i).setPosition(i);
        }

        if (selectedTabPosition == position) {
            selectTab(mTabs.isEmpty() ? null : mTabs.get(Math.max(0, position - 1)));
        }
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    public void removeAllTabs() {
        // Remove all the views
        for (int i = mTabStrip.getChildCount() - 1; i >= 0; i--) {
            removeTabViewAt(i);
        }

        for (final Iterator<Tab> i = mTabs.iterator(); i.hasNext(); ) {
            final Tab tab = i.next();
            i.remove();
            tab.reset();
            sTabPool.release(tab);
        }

        mSelectedTab = null;
    }

    /**
     * Set the behavior mode for the Tabs in this layout. The valid input options are:
     * <ul>
     * <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used
     * with content that benefits from quick pivots between tabs.</li>
     * <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
     * and can contain longer tab labels and a larger number of tabs. They are best used for
     * browsing contexts in touch interfaces when users don’t need to directly compare the tab
     * labels. This mode is commonly used with a {@link ViewPager}.</li>
     * </ul>
     *
     * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}or {@link #MODE_AUTO}.
     * @attr ref R.styleable#TabLayout_tabMode
     */
    public void setTabMode(@Mode int mode) {
        if (mode != mMode) {
            mMode = mode;
            applyModeAndGravity(true);
        }
    }

    /**
     * Returns the current mode used by this {@link XTabLayout}.
     *
     * @see #setTabMode(int)
     */
    @XTabLayout.Mode
    public int getTabMode() {
        return mMode;
    }

    /**
     * Set the gravity to use when laying out the tabs.
     *
     * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     * @attr ref R.styleable#TabLayout_tabGravity
     */
    public void setTabGravity(@TabGravity int gravity) {
        if (mTabGravity != gravity) {
            mTabGravity = gravity;
            applyModeAndGravity(true);
        }
    }

    /**
     * The current gravity used for laying out tabs.
     *
     * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     */
    @TabGravity
    public int getTabGravity() {
        return mTabGravity;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @see #getTabTextColors()
     */
    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (mTabTextColors != textColor) {
            mTabTextColors = textColor;
            updateAllTabs();
        }
    }

    /**
     * Gets the text colors for the different states (normal, selected) used for the tabs.
     */
    @Nullable
    public ColorStateList getTabTextColors() {
        return mTabTextColors;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @attr ref R.styleable#TabLayout_tabTextColor
     * @attr ref R.styleable#TabLayout_tabSelectedTextColor
     */
    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    /**
     * The one-stop shop for setting up this {@link XTabLayout} with a {@link ViewPager}.
     * <p>
     * <p>This method will link the given ViewPager and this TabLayout together so that any
     * changes in one are automatically reflected in the other. This includes adapter changes,
     * scroll state changes, and clicks. The tabs displayed in this layout will be populated
     * from the ViewPager adapter's page titles.</p>
     * <p>
     * <p>After this method is called, you will not need this method again unless you want
     * to change the linked ViewPager.</p>
     * <p>
     * <p>If the given ViewPager is non-null, it needs to already have a
     * {@link PagerAdapter} set.</p>
     *
     * @param viewPager The ViewPager to link, or {@code null} to clear any previous link.
     */
    public void setupWithViewPager(@Nullable final ViewPager viewPager) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mPageChangeListener);
            }
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }
        if (mCurrentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(mCurrentVpSelectedListener);
            mCurrentVpSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;
            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            mPageChangeListener.reset();
            viewPager.addOnPageChangeListener(mPageChangeListener);
            // Now we'll add a tab selected listener to set ViewPager's current item
            mCurrentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mCurrentVpSelectedListener);
            // Now we'll add a tab selected listener to set ViewPager's current item
            addOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager));

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, true);
            }
            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            mAdapterChangeListener.setAutoRefresh(true);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            setScrollPosition(viewPager.getCurrentItem(), 0f, true);

        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null;
            setPagerAdapter(null, false);
        }
    }

    /**
     * @deprecated Use {@link #setupWithViewPager(ViewPager)} to link a TabLayout with a ViewPager
     * together. When that method is used, the TabLayout will be automatically updated
     * when the {@link PagerAdapter} is changed.
     */
    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
        setPagerAdapter(adapter, false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        // Only delay the pressed state if the tabs can scroll
        return getTabScrollRange() > 0;
    }

    private int getTabScrollRange() {
        return Math.max(0, mTabStrip.getWidth() - getWidth() - getPaddingLeft()
                - getPaddingRight());
    }

    private void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }

        mPagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    private void populateFromPagerAdapter() {
        removeAllTabs();

        if (mPagerAdapter != null) {
            final int adapterCount = mPagerAdapter.getCount();

            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(mPagerAdapter.getPageTitle(i)), false);
            }

            // Make sure we reflect the currently set ViewPager item
            if (mViewPager != null && adapterCount > 0) {
                final int curItem = mViewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem));
                }
            }
        } else {
            removeAllTabs();
        }
    }

    private void updateAllTabs() {
        for (int i = 0, z = mTabs.size(); i < z; i++) {
            mTabs.get(i).updateView();
        }
    }

    private TabView createTabView(@NonNull final Tab tab) {
        TabView tabView = mTabViewPool != null ? mTabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        return tabView;
    }

    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        mTabs.add(position, tab);

        final int count = mTabs.size();
        for (int i = position + 1; i < count; i++) {
            mTabs.get(i).setPosition(i);
        }
    }

    private void addTabView(Tab tab, boolean setSelected) {
        final TabView tabView = tab.mView;

        mTabStrip.addView(tabView, createLayoutParamsForTabs());
        tabView.setSelected(setSelected);
    }

    private void addTabView(Tab tab, int position, boolean setSelected) {
        final TabView tabView = tab.mView;
        mTabStrip.addView(tabView, position, createLayoutParamsForTabs());
        tabView.setSelected(setSelected);
    }

    @Override
    public void addView(View child) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    private void addViewInternal(final View child) {
        if (child instanceof TabItem) {
            addTabFromItemView((TabItem) child);
        } else {
            throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
        }
    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
        if (mMode == MODE_FIXED && mTabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else if (mMode == MODE_AUTO && mTabGravity == GRAVITY_FILL) {
            if (mTabStrip.getMeasuredWidth() < getMeasuredWidth()) {
                lp.width = 0;
                lp.weight = 1;
            } else {
                lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                lp.weight = 0;
            }
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        }
    }

    private int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        onLayoutOk = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If we have a MeasureSpec which allows us to decide our height, try and use the default
        // height
        final int idealHeight = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)),
                        MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, MeasureSpec.EXACTLY);
                break;
        }

        final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            // If we don't have an unspecified width spec, use the given size to calculate
            // the max tab width
            if (mPagerAdapter != null && xTabDisplayNum != 0) {
                if (mPagerAdapter.getCount() == 1 || xTabDisplayNum == 1) {
                    WindowManager wm = (WindowManager) getContext()
                            .getSystemService(Context.WINDOW_SERVICE);
                    mTabMaxWidth = wm.getDefaultDisplay().getWidth();
                } else {
                    mTabMaxWidth = mRequestedTabMaxWidth > 0
                            ? mRequestedTabMaxWidth
                            : specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN);
                }
            } else {
                mTabMaxWidth = mRequestedTabMaxWidth > 0
                        ? mRequestedTabMaxWidth
                        : specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN);
            }
        }

        // Now super measure itself using the (possibly) modified height spec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 1) {
            // If we're in fixed mode then we need to make the tab strip is the same width as us
            // so we don't scroll
            final View child = getChildAt(0);
            boolean remeasure = false;

            switch (mMode) {
                case MODE_SCROLLABLE:
                    // We only need to resize the child if it's smaller than us. This is similar
                    // to fillViewport
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
                case MODE_FIXED:
                    // Resize the child so that it doesn't scroll
                    remeasure = child.getMeasuredWidth() != getMeasuredWidth();
                    break;
                case MODE_AUTO:
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
            }

            if (remeasure) {
                // Re-measure the child with a widthSpec set to be exactly our measure width
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop()
                        + getPaddingBottom(), child.getLayoutParams().height);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                        getMeasuredWidth(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    private void removeTabViewAt(int position) {
        final TabView view = (TabView) mTabStrip.getChildAt(position);
        mTabStrip.removeViewAt(position);
        if (view != null) {
            view.reset();
            mTabViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition == Tab.INVALID_POSITION) {
            return;
        }

        if (getWindowToken() == null || !ViewCompat.isLaidOut(this)
                || mTabStrip.childrenNeedLayout()) {
            // If we don't have a window token, or we haven't been laid out yet just draw the new
            // position now
            setScrollPosition(newPosition, 0f, true);
            return;
        }

        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(newPosition, 0);
        if (startScrollX != targetScrollX) {
            if (mScrollAnimator == null) {
                mScrollAnimator = new ValueAnimator();
                mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                mScrollAnimator.setDuration(ANIMATION_DURATION);
                mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        scrollTo((int) animator.getAnimatedValue(), 0);
                    }
                });
            }

            mScrollAnimator.setIntValues(startScrollX, targetScrollX);
            mScrollAnimator.start();
        }

        // Now animate the indicator
        mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
    }

    private void setSelectedTabView(int position) {
        final int tabCount = mTabStrip.getChildCount();
        if (position < tabCount && !mTabStrip.getChildAt(position).isSelected()) {
            for (int i = 0; i < tabCount; i++) {
                final View child = mTabStrip.getChildAt(i);
                child.setSelected(i == position);
            }
        }
    }

    void selectTab(Tab tab) {
        selectTab(tab, true, true);
    }

    void selectTab(Tab tab, boolean updateIndicator, boolean isNotifica) {
        if (mSelectedTab == tab) {
            if (mSelectedTab != null) {
                if (isNotifica) {
                    if (mSelectedListener != null) {
                        mSelectedListener.onTabReselected(mSelectedTab);
                    }
                    for (OnTabSelectedListener onTabSelectedListener : mSelectedListeners) {
                        onTabSelectedListener.onTabReselected(mSelectedTab);
                    }
                }
                animateToTab(tab.getPosition());
            }
        } else {
            if (updateIndicator) {
                final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition);
                }
                if (!onLayoutOk || (mSelectedTab == null || mSelectedTab.getPosition() == Tab.INVALID_POSITION)
                        && newPosition != Tab.INVALID_POSITION) {
                    // If we don't currently have a tab, just draw the indicator
                    setScrollPosition(newPosition, 0f, true);
                } else {
                    animateToTab(newPosition);
                }
            }
            if (isNotifica) {
                if (mSelectedTab != null && mSelectedTab.getPosition() != Tab.INVALID_POSITION) {
                    if (mSelectedListener != null) {
                        mSelectedListener.onTabUnselected(mSelectedTab);
                    }
                    for (OnTabSelectedListener onTabSelectedListener : mSelectedListeners) {
                        onTabSelectedListener.onTabUnselected(mSelectedTab);
                    }
                }
            }
            mSelectedTab = tab;
            if (isNotifica) {
                if (mSelectedTab != null && mSelectedTab.getPosition() != Tab.INVALID_POSITION) {
                    if (mSelectedListener != null) {
                        mSelectedListener.onTabSelected(mSelectedTab);
                    }
                    for (OnTabSelectedListener onTabSelectedListener : mSelectedListeners) {
                        onTabSelectedListener.onTabSelected(mSelectedTab);
                    }
                }
            }
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mMode == MODE_SCROLLABLE || mMode == MODE_AUTO) {
            final View selectedChild = mTabStrip.getChildAt(position);
            final View nextChild = position + 1 < mTabStrip.getChildCount()
                    ? mTabStrip.getChildAt(position + 1)
                    : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            return selectedChild.getLeft()
                    + ((int) ((selectedWidth + nextWidth) * positionOffset * 0.5f))
                    + (selectedChild.getWidth() / 2)
                    - (getWidth() / 2);
        }
        return 0;
    }

    private void applyModeAndGravity(boolean requestLayout) {
        int paddingStart = 0;
        if (mMode == MODE_SCROLLABLE || mMode == MODE_AUTO) {
            // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, mContentInsetStart - mTabPaddingStart);
        }
        mTabStrip.setPadding(paddingStart, 0, 0, 0);
        switch (mMode) {
            case MODE_FIXED:
                mTabStrip.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case MODE_SCROLLABLE:
                mTabStrip.setGravity(GravityCompat.START);
                break;
            case MODE_AUTO:
                mTabStrip.setGravity(GravityCompat.START);
                break;
        }
        updateTabViews(requestLayout);
    }

    private void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View child = mTabStrip.getChildAt(i);
            child.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            if (requestLayout) {
                child.requestLayout();
            }
        }
    }

    /**
     * A tab in this layout. Instances can be created via {@link #newTab()}.
     */
    public static final class Tab {

        /**
         * An invalid position for a tab.
         *
         * @see #getPosition()
         */
        public static final int INVALID_POSITION = -1;

        private Object mTag;
        private Drawable mIcon;
        private CharSequence mText;
        private CharSequence mContentDesc;
        private int mPosition = INVALID_POSITION;
        private View mCustomView;

        private XTabLayout mParent;
        private TabView mView;

        private Tab() {
            // Private constructor
        }


        /**
         * @return This Tab's tag object.
         */
        @Nullable
        public Object getTag() {
            return mTag;
        }

        public int getTextWidth() {
            return mView.getTextWidth();
        }

        /**
         * Give this Tab an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setTag(@Nullable Object tag) {
            mTag = tag;
            return this;
        }


        /**
         * Returns the custom view used for this tab.
         *
         * @see #setCustomView(View)
         * @see #setCustomView(int)
         */
        @Nullable
        public View getCustomView() {
            return mCustomView;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the provided view contains a {@link TextView} with an ID of
         * {@link android.R.id#text1} then that will be updated with the value given
         * to {@link #setText}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param view Custom view to be used as a tab.
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@Nullable View view) {
            mCustomView = view;
            updateView();
            return this;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the inflated layout contains a {@link TextView} with an ID of
         * {@link android.R.id#text1} then that will be updated with the value given
         * to {@link #setText}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param resId A layout resource to inflate and use as a custom tab view
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@LayoutRes int resId) {
            final LayoutInflater inflater = LayoutInflater.from(mView.getContext());
            return setCustomView(inflater.inflate(resId, mView, false));
        }

        /**
         * Return the icon associated with this tab.
         *
         * @return The tab's icon
         */
        @Nullable
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * Return the current position of this tab in the action bar.
         *
         * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in
         * the action bar.
         */
        public int getPosition() {
            return mPosition;
        }

        void setPosition(int position) {
            mPosition = position;
        }

        /**
         * Return the text of this tab.
         *
         * @return The tab's text
         */
        @Nullable
        public CharSequence getText() {
            return mText;
        }


        /**
         * Set the icon displayed on this tab.
         *
         * @param icon The drawable to use as an icon
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            mIcon = icon;
            updateView();
            return this;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param resId A resource ID referring to the icon that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@DrawableRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setIcon(AppCompatResources.getDrawable(mParent.getContext(), resId));
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param text The text to display
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            mText = text;
            updateView();
            return this;
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param resId A resource ID referring to the text that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setText(mParent.getResources().getText(resId));
        }

        /**
         * Select this tab. Only valid if the tab has been added to the action bar.
         */
        public void select() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            mParent.selectTab(this);
        }

        /**
         * Returns true if this tab is currently selected.
         */
        public boolean isSelected() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return mParent.getSelectedTabPosition() == mPosition;
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param resId A resource ID referring to the description text
         * @return The current instance for call chaining
         * @see #setContentDescription
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setContentDescription(mParent.getResources().getText(resId));
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param contentDesc Description of this tab's content
         * @return The current instance for call chaining
         * @see #setContentDescription(int)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@Nullable CharSequence contentDesc) {
            mContentDesc = contentDesc;
            updateView();
            return this;
        }

        /**
         * Gets a brief description of this tab's content for use in accessibility support.
         *
         * @return Description of this tab's content
         * @see #setContentDescription
         * @see #setContentDescription(int)
         */
        @Nullable
        public CharSequence getContentDescription() {
            return mContentDesc;
        }

        private void updateView() {
            if (mView != null) {
                mView.update();
            }
        }

        private void reset() {
            mParent = null;
            mView = null;
            mTag = null;
            mIcon = null;
            mText = null;
            mContentDesc = null;
            mPosition = INVALID_POSITION;
            mCustomView = null;
        }
    }

    class TabView extends LinearLayout implements OnLongClickListener {
        private Tab mTab;
        private TextView mTextView;
        private ImageView mIconView;

        private View mCustomView;
        private TextView mCustomTextView;
        private ImageView mCustomIconView;

        private int mDefaultMaxLines = 2;

        public TabView(Context context) {
            super(context);
          /*  if (mTabBackgroundResId != 0) {
                setBackgroundDrawable(
                        AppCompatDrawableManager.get().getDrawable(context, mTabBackgroundResId));
            }*/
            ViewCompat.setPaddingRelative(this, mTabPaddingStart, mTabPaddingTop,
                    mTabPaddingEnd, mTabPaddingBottom);
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            setClickable(true);
        }

        public String getText() {
            return mTextView.getText().toString();
        }

        public int getTextWidth() {
            if (TextUtils.isEmpty(mTextView.getText().toString())) {
                return 0;
            }
            Rect rect = new Rect();
            String content = mTextView.getText().toString();
            mTextView.getPaint().getTextBounds(content, 0, content.length(), rect);
            return rect.width();
        }

        @Override
        public boolean performClick() {
            final boolean value = super.performClick();

            if (mTab != null) {
                mTab.select();
                return true;
            } else {
                return value;
            }
        }

        @Override
        public void setSelected(boolean selected) {
            final boolean changed = (isSelected() != selected);
            super.setSelected(selected);
            if (!selected) {
                setBackground(xTabItemBackground);
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                mTextView.setTypeface(Typeface.defaultFromStyle(xTabTextBold ? Typeface.BOLD : Typeface.NORMAL));
            }
            if (changed && selected) {
                setBackground(xTabItemSelectedBackground);
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);

                if (mTextView != null) {
                    mTextView.setSelected(selected);

                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize);
                    if (xTabTextSelectedBold) {
                        mTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    } else {
                        mTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    }
                }
                if (mIconView != null) {
                    mIconView.setSelected(selected);
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            // This view masquerades as an action bar tab.
            event.setClassName(ActionBar.Tab.class.getName());
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            // This view masquerades as an action bar tab.
            info.setClassName(ActionBar.Tab.class.getName());
        }

        @Override
        public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
            final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            final int maxWidth = getTabMaxWidth();

            final int widthMeasureSpec;
            final int heightMeasureSpec = origHeightMeasureSpec;

            if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED
                    || specWidthSize > maxWidth)) {
                // If we have a max width and a given spec which is either unspecified or
                // larger than the max width, update the width spec using the same mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mTabMaxWidth, MeasureSpec.AT_MOST);
            } else {
                // Else, use the original width spec
                widthMeasureSpec = origWidthMeasureSpec;
            }
            // Now lets measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // We need to switch the text size based on whether the text is spanning 2 lines or not
            if (mTextView != null) {
                float textSize = mTabTextSize;
                int maxLines = mDefaultMaxLines;

                if (mIconView != null && mIconView.getVisibility() == VISIBLE) {
                    // If the icon view is being displayed, we limit the text to 1 line
                    maxLines = 1;
                } else if (mTextView != null && mTextView.getLineCount() > 1) {
                    // Otherwise when we have text which wraps we reduce the text size
                    textSize = mTabTextMultiLineSize;
                }

                final float curTextSize = mTextView.getTextSize();
                final int curLineCount = mTextView.getLineCount();
                final int curMaxLines = TextViewCompat.getMaxLines(mTextView);

                if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
                    // We've got a new text size and/or max lines...
                    boolean updateTextView = true;

                    if (mMode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
                        // If we're in fixed mode, going up in text size and currently have 1 line
                        // then it's very easy to get into an infinite recursion.
                        // To combat that we check to see if the change in text size
                        // will cause a line count change. If so, abort the size change.
                        final Layout layout = mTextView.getLayout();
                        if (layout == null
                                || approximateLineWidth(layout, 0, textSize) > layout.getWidth()) {
                            updateTextView = false;
                        }
                    }

                    if (updateTextView) {
                        if (mTextView.isSelected()) {
                            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize);
                        } else {
                            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                        }
                        if (mTextView.isSelected()) {
                            mTextView.setTypeface(Typeface.defaultFromStyle(xTabTextSelectedBold ? Typeface.BOLD : Typeface.NORMAL));
                        } else {
                            mTextView.setTypeface(Typeface.defaultFromStyle(xTabTextBold ? Typeface.BOLD : Typeface.NORMAL));
                        }
                        mTextView.setMaxLines(maxLines);
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }
        }

        private void setTab(@Nullable final Tab tab) {
            if (tab != mTab) {
                mTab = tab;
                update();
            }
        }

        private void reset() {
            setTab(null);
            setSelected(false);
        }

        final void update() {
            final Tab tab = mTab;
            final View custom = tab != null ? tab.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                mCustomView = custom;
                if (mTextView != null) {
                    mTextView.setVisibility(GONE);
                }
                if (mIconView != null) {
                    mIconView.setVisibility(GONE);
                    mIconView.setImageDrawable(null);
                }

                mCustomTextView = (TextView) custom.findViewById(android.R.id.text1);
                if (mCustomTextView != null) {
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mCustomTextView);
                }
                mCustomIconView = (ImageView) custom.findViewById(android.R.id.icon);
            } else {
                // We do not have a custom view. Remove one if it already exists
                if (mCustomView != null) {
                    removeView(mCustomView);
                    mCustomView = null;
                }
                mCustomTextView = null;
                mCustomIconView = null;
            }

            if (mCustomView == null) {
                // If there isn't a custom view, we'll us our own in-built layouts
                if (mIconView == null) {
                    ImageView iconView = (ImageView) LayoutInflater.from(getContext())
                            .inflate(R.layout.design_layout_xtab_icon, this, false);
                    addView(iconView, 0);
                    mIconView = iconView;
                }
                if (mTextView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext())
                            .inflate(R.layout.design_layout_xtab_text, this, false);
                    addView(textView);
                    mTextView = textView;
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mTextView);
                }
                if (mTextView.isSelected()) {
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize);
                } else {
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                }
                if (mTextView.isSelected()) {
                    mTextView.setTypeface(Typeface.defaultFromStyle(xTabTextSelectedBold ? Typeface.BOLD : Typeface.NORMAL));
                } else {
                    mTextView.setTypeface(Typeface.defaultFromStyle(xTabTextBold ? Typeface.BOLD : Typeface.NORMAL));
                }
                mTextView.setTextAppearance(getContext(), mTabTextAppearance);
                if (mTabTextColors != null) {
                    mTextView.setTextColor(mTabTextColors);
                }
                updateTextAndIcon(mTextView, mIconView);
            } else {
                // Else, we'll see if there is a TextView or ImageView present and update them
                if (mCustomTextView != null || mCustomIconView != null) {
                    updateTextAndIcon(mCustomTextView, mCustomIconView);
                }
            }
        }

        private void updateTextAndIcon(@Nullable final TextView textView,
                                       @Nullable final ImageView iconView) {
            final Drawable icon = mTab != null ? mTab.getIcon() : null;
            final CharSequence text = mTab != null ? mTab.getText() : null;
            final CharSequence contentDesc = mTab != null ? mTab.getContentDescription() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
                iconView.setContentDescription(contentDesc);
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setAllCaps(xTabTextAllCaps);
                    textView.setText(text);
                    textView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
                textView.setContentDescription(contentDesc);
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int bottomMargin = 0;
                if (hasText && iconView.getVisibility() == VISIBLE) {
                    // If we're showing both text and icon, add some margin bottom to the icon
                    bottomMargin = iconAndTextSpace;
                }
                if (bottomMargin != lp.bottomMargin) {
                    lp.bottomMargin = bottomMargin;
                    iconView.requestLayout();
                }
            }

            if (!hasText && !TextUtils.isEmpty(contentDesc)) {
                setOnLongClickListener(this);
            } else {
                setOnLongClickListener(null);
                setLongClickable(false);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);

            final Context context = getContext();
            final int width = getWidth();
            final int height = getHeight();
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

            Toast cheatSheet = Toast.makeText(context, mTab.getContentDescription(),
                    Toast.LENGTH_SHORT);
            // Show under the tab
            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    (screenPos[0] + width / 2) - screenWidth / 2, height);

            cheatSheet.show();
            return true;
        }

        public Tab getTab() {
            return mTab;
        }

        /**
         * Approximates a given lines width with the new provided text size.
         */
        private float approximateLineWidth(Layout layout, int line, float textSize) {
            return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
        }
    }

    public class SlidingTabStrip extends LinearLayout {
        private int mSelectedIndicatorHeight;
        private int mSelectedIndicatorWidth;
        /**
         * 指示器长度是否随TextView长度变化
         */
        private boolean xTabDividerWidthText = false;
        private Drawable mSelectedIndicatorDrawable;
        private final Paint mSelectedIndicatorPaint;

        private int mSelectedPosition = -1;
        private float mSelectionOffset;

        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;

        private ValueAnimator mIndicatorAnimator;

        SlidingTabStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            mSelectedIndicatorPaint = new Paint();
        }

        /**
         * 指示器长度是否随TextView长度变化
         */
        public void setWidthText(boolean b) {
            if (xTabDividerWidthText != b) {
                xTabDividerWidthText = b;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        public void setSelectedIndicatorColor(int color) {
            if (mSelectedIndicatorPaint.getColor() != color) {
                mSelectedIndicatorPaint.setColor(color);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        public void setSelectedIndicatorDrawable(Drawable drawable) {
            if (drawable != null && mSelectedIndicatorDrawable != drawable) {
                mSelectedIndicatorDrawable = drawable;
                if (mSelectedIndicatorDrawable.getIntrinsicHeight() > 0) {
                    mSelectedIndicatorHeight = mSelectedIndicatorDrawable.getIntrinsicHeight();
                }
                if (mSelectedIndicatorDrawable.getIntrinsicWidth() > 0) {
                    mSelectedIndicatorWidth = mSelectedIndicatorDrawable.getIntrinsicWidth();
                }
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        public void setSelectedIndicatorHeight(int height) {
            if (mSelectedIndicatorHeight != height) {
                mSelectedIndicatorHeight = height;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        public void setSelectedIndicatorWidth(int width) {
            if (mSelectedIndicatorWidth != width) {
                mSelectedIndicatorWidth = width;
                ViewCompat.postInvalidateOnAnimation(this);

            }
        }

        public int getSelectedIndicatorWidth() {
            return mSelectedIndicatorWidth;
        }

        boolean childrenNeedLayout() {
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (child.getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        public void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            mSelectedPosition = position;
            mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        public float getIndicatorPosition() {
            return mSelectedPosition + mSelectionOffset;
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
                // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return;
            }
            boolean remeasure = false;
            boolean isAutoAndCenter = false;
            if (mMode == MODE_AUTO) {
                final int count = getChildCount();
                // First we'll find the widest tab
                int allTabWidth = 0;
                for (int i = 0, z = count; i < z; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == VISIBLE) {
                        allTabWidth += child.getMeasuredWidth();
                    }
                }

                if (allTabWidth < getMeasuredWidth()) {
                    setGravity(Gravity.START);
                    if (mTabGravity == GRAVITY_CENTER) {
                        isAutoAndCenter = true;
                    } else {
                        for (int i = 0; i < count; i++) {
                            final LayoutParams lp =
                                    (LayoutParams) getChildAt(i).getLayoutParams();
                            if (lp.width != 0 || lp.weight != 1) {
                                lp.width = 0;
                                lp.weight = count == 1 ? 0 : 1;
                                remeasure = true;
                            }
                        }
                    }
                } else if (allTabWidth > getMeasuredWidth()) {
                    setGravity(Gravity.START);
                    for (int i = 0; i < count; i++) {
                        final LayoutParams lp =
                                (LayoutParams) getChildAt(i).getLayoutParams();
                        if (lp.width != LinearLayout.LayoutParams.WRAP_CONTENT || lp.weight != 0) {
                            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            lp.weight = 0;
                            remeasure = true;
                        }
                    }
                }

            }

            if (isAutoAndCenter || (mMode == MODE_FIXED && mTabGravity == GRAVITY_CENTER)) {
                final int count = getChildCount();

                // First we'll find the widest tab
                int largestTabWidth = 0;
                for (int i = 0, z = count; i < z; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == VISIBLE) {
                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                    }
                }

                if (largestTabWidth <= 0) {
                    // If we don't have a largest child yet, skip until the next measure pass
                    return;
                }

                final int gutter = dpToPx(FIXED_WRAP_GUTTER_MIN);

                if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
                    // If the tabs fit within our width minus gutters, we will set all tabs to have
                    // the same width
                    for (int i = 0; i < count; i++) {
                        final LayoutParams lp =
                                (LayoutParams) getChildAt(i).getLayoutParams();
                        if (lp.width != largestTabWidth || lp.weight != 0) {
                            lp.width = largestTabWidth;
                            lp.weight = 0;
                            remeasure = true;
                        }
                    }
                } else {
                    // If the tabs will wrap to be larger than the width minus gutters, we need
                    // to switch to GRAVITY_FILL
                    mTabGravity = GRAVITY_FILL;
                    updateTabViews(false);
                    remeasure = true;
                }

            }
            if (remeasure) {
                // Now re-measure after our changes
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                // If we're currently running an animation, lets cancel it and start a
                // new animation with the remaining duration
                mIndicatorAnimator.cancel();
                final long duration = mIndicatorAnimator.getDuration();

                animateIndicatorToPosition(mSelectedPosition,
                        Math.round((1f - mIndicatorAnimator.getAnimatedFraction()) * duration));
            } else {
                // If we've been layed out, update the indicator position
                updateIndicatorPosition();
            }
        }

        public void updateIndicatorPosition() {
            final View selectedTitle = getChildAt(mSelectedPosition);
            int left, right;
            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();

                int haftWidth = 0;
                if (xTabDividerWidthText) {
                    mSelectedIndicatorWidth = mSelectedTab.getTextWidth();
                } else if (mSelectedIndicatorWidth == 0) {
                    mSelectedIndicatorWidth = maxWidth;
                }
                if (mSelectedIndicatorWidth != 0) {
                    int maxWidth = mIndicatorRight - mIndicatorLeft;
                    if (maxWidth > mSelectedIndicatorWidth) {
                        haftWidth = (maxWidth - mSelectedIndicatorWidth) / 2;
                        left += haftWidth;
                        right -= haftWidth;
                    }
                }

                if (mSelectionOffset > 0f && mSelectedPosition < getChildCount() - 1) {
                    // Draw the selection partway between the tabs
                    View nextTitle = getChildAt(mSelectedPosition + 1);
                    int nextLeft = nextTitle.getLeft() + haftWidth;
                    int nextRight = nextTitle.getRight() - haftWidth;
                    left = (int) (mSelectionOffset * nextLeft +
                            (1.0f - mSelectionOffset) * left);
                    right = (int) (mSelectionOffset * nextRight +
                            (1.0f - mSelectionOffset) * right);
                }
            } else {
                left = right = -1;
            }

            setIndicatorPosition(left, right);
        }

        private void setIndicatorPosition(int left, int right) {
            left = left + mTabPaddingStart;
            right = right - mTabPaddingEnd;
            if (left != mIndicatorLeft || right != mIndicatorRight) {
                // If the indicator's left/right has changed, invalidate
                mIndicatorLeft = left;
                mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void animateIndicatorToPosition(final int position, int duration) {

            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            final boolean isRtl = ViewCompat.getLayoutDirection(this)
                    == ViewCompat.LAYOUT_DIRECTION_RTL;

            final View targetView = getChildAt(position);
            if (targetView == null) {
                // If we don't have a view, just update the position now and return
                updateIndicatorPosition();
                return;
            }

            final int targetLeft = targetView.getLeft();
            final int targetRight = targetView.getRight();
            final int startLeft;
            final int startRight;

            if (Math.abs(position - mSelectedPosition) <= 1) {
                // If the views are adjacent, we'll animate from edge-to-edge
                startLeft = mIndicatorLeft;
                startRight = mIndicatorRight;
            } else {
                // Else, we'll just grow from the nearest edge
                final int offset = dpToPx(MOTION_NON_ADJACENT_OFFSET);
                if (position < mSelectedPosition) {
                    // We're going end-to-start
                    if (isRtl) {
                        startLeft = startRight = targetLeft - offset;
                    } else {
                        startLeft = startRight = targetRight + offset;
                    }
                } else {
                    // We're going start-to-end
                    if (isRtl) {
                        startLeft = startRight = targetRight + offset;
                    } else {
                        startLeft = startRight = targetLeft - offset;
                    }
                }
            }

            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimator animator = mIndicatorAnimator = new ValueAnimator();
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration(duration);
                animator.setFloatValues(0, 1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        final float fraction = animator.getAnimatedFraction();
                        setIndicatorPosition(
                                AnimationUtils.lerp(startLeft, targetLeft, fraction),
                                AnimationUtils.lerp(startRight, targetRight, fraction));
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mSelectedPosition = position;
                        mSelectionOffset = 0f;
                    }
                });
                animator.start();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            // Thick colored underline below the current selection
            if (mIndicatorLeft >= 0 && mIndicatorRight > mIndicatorLeft) {

               /* int maxWidth = mIndicatorRight - mIndicatorLeft;
                if (maxWidth > mSelectedIndicatorWidth) {
                    mIndicatorLeft += (maxWidth - mSelectedIndicatorWidth) / 2;
                    mIndicatorRight -= (maxWidth - mSelectedIndicatorWidth) / 2;
                }*/
                if (mSelectedIndicatorWidth != 0) {
                    int maxWidth = mIndicatorRight - mIndicatorLeft;
                    if (maxWidth > mSelectedIndicatorWidth) {
                        mIndicatorLeft += (maxWidth - mSelectedIndicatorWidth) / 2;
                        mIndicatorRight -= (maxWidth - mSelectedIndicatorWidth) / 2;
                    }
                } else {
                    int maxWidth = mIndicatorRight - mIndicatorLeft;
                    if (maxWidth > mSelectedTab.getTextWidth()) {
                        mIndicatorLeft += (maxWidth - mSelectedTab.getTextWidth()) / 2;
                        mIndicatorRight -= (maxWidth - mSelectedTab.getTextWidth()) / 2;
                    }
                }
                if (mSelectedIndicatorDrawable != null) {
                    mSelectedIndicatorDrawable.setBounds(0, 0, mIndicatorRight - mIndicatorLeft, mSelectedIndicatorHeight);
                    canvas.save();
                    canvas.translate(mIndicatorLeft, getHeight() - mSelectedIndicatorHeight);
                    mSelectedIndicatorDrawable.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.drawRect(mIndicatorLeft, getHeight() - mSelectedIndicatorHeight,
                            mIndicatorRight, getHeight(), mSelectedIndicatorPaint);
                }
            }
        }
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    private int getDefaultHeight() {
        boolean hasIconAndText = false;
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            Tab tab = mTabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                hasIconAndText = true;
                break;
            }
        }
        return hasIconAndText ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
    }

    private int getTabMinWidth() {
        if (mPagerAdapter != null && xTabDisplayNum != 0) {
            WindowManager wm = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            if (mPagerAdapter.getCount() == 1 || xTabDisplayNum == 1) {
                return wm.getDefaultDisplay().getWidth();
            } else if (mPagerAdapter.getCount() < xTabDisplayNum) {
                return wm.getDefaultDisplay().getWidth() / mPagerAdapter.getCount();
            } else {
                return wm.getDefaultDisplay().getWidth() / xTabDisplayNum;
            }
        }
        if (xTabDisplayNum != 0) {
            WindowManager wm = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            return wm.getDefaultDisplay().getWidth() / xTabDisplayNum;
        }
        if (mRequestedTabMinWidth != INVALID_WIDTH) {
            // If we have been given a min width, use it
            //默认再加上一点边距
            return mRequestedTabMinWidth;
        }
        // Else, we'll use the default value
        return mMode == MODE_SCROLLABLE || mMode == MODE_AUTO ? mScrollableTabMinWidth : 0;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        // We don't care about the layout params of any views added to us, since we don't actually
        // add them. The only view we add is the SlidingTabStrip, which is done manually.
        // We return the default layout params so that we don't blow up if we're given a TabItem
        // without android:layout_* values.
        return generateDefaultLayoutParams();
    }

    private int getTabMaxWidth() {
        return mTabMaxWidth;
    }

    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the
     * necessary calls back to the provided {@link XTabLayout} so that the tab position is
     * kept in sync.
     * <p>
     * <p>This class stores the provided TabLayout weakly, meaning that you can use
     * {@link ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and
     * not cause a leak.
     */
    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<XTabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public TabLayoutOnPageChangeListener(XTabLayout tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            final XTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                        mPreviousScrollState == SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                final boolean updateIndicator = !(mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(int position) {
            final XTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                final boolean updateIndicator = mScrollState == SCROLL_STATE_IDLE
                        || (mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator, true);
            }
        }

        private void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link OnTabSelectedListener} class which contains the necessary calls back
     * to the provided {@link ViewPager} so that the tab position is kept in sync.
     */
    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }
}
