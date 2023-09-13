/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ashlikun.xtablayout;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;
import static com.google.android.material.animation.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XTabLayout provides a horizontal layout to display tabs.
 *
 * <p>Population of the tabs to display is done through {@link Tab} instances. You create tabs via
 * {@link #newTab()}. From there you can change the tab's label or icon via {@link Tab#setText(int)}
 * and {@link Tab#setIcon(int)} respectively. To display the tab, you need to add it to the layout
 * via one of the {@link #addTab(Tab)} methods. For example:
 *
 * <pre>
 * XTabLayout tabLayout = ...;
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
 * </pre>
 * <p>
 * You should add a listener via {@link #addOnTabSelectedListener(OnTabSelectedListener)} to be
 * notified when any tab's selection state has been changed.
 *
 * <p>You can also add items to XTabLayout in your layout through the use of {@link TabItem}. An
 * example usage is like so:
 *
 * <pre>
 * &lt;com.google.android.material.tabs.XTabLayout
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_width=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.google.android.material.tabs.TabItem
 *             android:text=&quot;@string/tab_text&quot;/&gt;
 *
 *     &lt;com.google.android.material.tabs.TabItem
 *             android:icon=&quot;@drawable/ic_android&quot;/&gt;
 *
 * &lt;/com.google.android.material.tabs.XTabLayout&gt;
 * </pre>
 *
 * <h3>ViewPager integration</h3>
 *
 * <p>If you're using a {@link androidx.viewpager.widget.ViewPager} together with this layout, you
 * can call {@link #setupWithViewPager(ViewPager)} to link the two together. This layout will be
 * automatically populated from the {@link androidx.viewpager.widget.PagerAdapter}'s page titles.
 *
 * <p>This view also supports being used as part of a ViewPager's decor, and can be added directly
 * to the ViewPager in a layout resource file like so:
 *
 * <pre>
 * &lt;androidx.viewpager.widget.ViewPager
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.google.android.material.tabs.XTabLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_gravity=&quot;top&quot; /&gt;
 *
 * &lt;/androidx.viewpager.widget.ViewPager&gt;
 * </pre>
 *
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabPadding
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabPaddingStart
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabPaddingTop
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabPaddingEnd
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabPaddingBottom
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabContentStart
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabBackground
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabMinWidth
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabMaxWidth
 * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabTextAppearance
 * @see <a href="http://www.google.com/design/spec/components/tabs.html">Tabs</a>
 */

/**
 * @author　　: 李坤
 * 创建时间: 2022/11/9 12:42
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：修改官方的XTabLayout（1.7.0版本）
 */
@SuppressLint("RestrictedApi")
@ViewPager.DecorView
public class XTabLayout extends HorizontalScrollView {

    private static final int DEF_STYLE_RES = R.style.XTabLayout_Default;

    @Dimension(unit = Dimension.DP)
    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;

    @Dimension(unit = Dimension.DP)
    static final int DEFAULT_GAP_TEXT_ICON = 8;

    @Dimension(unit = Dimension.DP)
    private static final int DEFAULT_HEIGHT = 48;

    @Dimension(unit = Dimension.DP)
    private static final int TAB_MIN_WIDTH_MARGIN = 56;

    @Dimension(unit = Dimension.DP)
    static final int FIXED_WRAP_GUTTER_MIN = 16;

    private static final int INVALID_WIDTH = -1;

    private static final int ANIMATION_DURATION = 300;

    private static final int SELECTED_INDICATOR_HEIGHT_DEFAULT = -1;

    private static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool<>(16);

    private static final String LOG_TAG = "XTabLayout";

    /**
     * 可滚动选项卡在任何给定时刻显示选项卡的子集，并且可以包含更长的选项卡标签以及更大数量的选项卡。当用户不需要直接比较标签。
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_SCROLLABLE = 0;

    /**
     * 固定选项卡同时显示所有选项卡，最好与受益于选项卡之间的快速枢轴。选项卡的最大数量受视图宽度的限制。固定的选项卡的宽度相等，基于最宽的选项卡标签。
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_FIXED = 1;

    /**
     * 自动调整选项卡大小的行为类似于带有GRAVITY_CENTER的MODE_FIXED，而选项卡适合
     * XTabLayout的内容宽度。固定选项卡具有相同的宽度，基于最宽的选项卡标签。一旦
     * 选项卡超出视图的宽度，自动调整选项卡大小的行为类似于MODE_SCROLLABLE，允许
     * 动态数量的选项卡，而不需要额外的布局逻辑。
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_AUTO = 2;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    /**
     * 如果使用{@link Tab#setText(CharSequence)}实例化选项卡，并且设置了此模式，则文本
     * 将被保存并用于内容描述，但不会创建可见标签。
     *
     * @see Tab#setTabLabelVisibility(int)
     */
    public static final int TAB_LABEL_VISIBILITY_UNLABELED = 0;

    /**
     * 此模式是默认设置的。如果选项卡是用{@link Tab#setText(CharSequence)}实例化的，则
     * 将创建可见标签。
     *
     * @see Tab#setTabLabelVisibility(int)
     */
    public static final int TAB_LABEL_VISIBILITY_LABELED = 1;

    /**
     * @hide
     */
    @IntDef(value = {TAB_LABEL_VISIBILITY_UNLABELED, TAB_LABEL_VISIBILITY_LABELED})
    public @interface LabelVisibility {
    }

    /**
     * 重力用于尽可能多地填充{@link XTabLayout}。此选项仅生效
     * 当在宽度小于600dp的非横向屏幕上与{@link #MODE_FIXED}一起使用时。
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_FILL = 0;

    /**
     * 重力用于将选项卡布置在
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_CENTER = 1;

    /**
     * 重力用于将选项卡排列为与 {@link XTabLayout}.
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_START = 1 << 1;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
            flag = true,
            value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_START})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    /**
     * Indicator gravity used to align the tab selection indicator to the bottom of the {@link
     * XTabLayout}. This will only take effect if the indicator height is set via the custom indicator
     * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
     * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
     * indicator will not be shown. This is the default value.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorGravity
     * @see #setSelectedTabIndicatorGravity(int)
     * @see #getTabIndicatorGravity()
     */
    public static final int INDICATOR_GRAVITY_BOTTOM = 0;

    /**
     * Indicator gravity used to align the tab selection indicator to the center of the {@link
     * XTabLayout}. This will only take effect if the indicator height is set via the custom indicator
     * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
     * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
     * indicator will not be shown.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorGravity
     * @see #setSelectedTabIndicatorGravity(int)
     * @see #getTabIndicatorGravity()
     */
    public static final int INDICATOR_GRAVITY_CENTER = 1;

    /**
     * Indicator gravity used to align the tab selection indicator to the top of the {@link
     * XTabLayout}. This will only take effect if the indicator height is set via the custom indicator
     * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
     * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
     * indicator will not be shown.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorGravity
     * @see #setSelectedTabIndicatorGravity(int)
     * @see #getTabIndicatorGravity()
     */
    public static final int INDICATOR_GRAVITY_TOP = 2;

    /**
     * Indicator gravity used to stretch the tab selection indicator across the entire height and
     * width of the {@link XTabLayout}. This will disregard {@code tabIndicatorHeight} and the
     * indicator drawable's intrinsic height, if set.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorGravity
     * @see #setSelectedTabIndicatorGravity(int)
     * @see #getTabIndicatorGravity()
     */
    public static final int INDICATOR_GRAVITY_STRETCH = 3;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
            value = {
                    INDICATOR_GRAVITY_BOTTOM,
                    INDICATOR_GRAVITY_CENTER,
                    INDICATOR_GRAVITY_TOP,
                    INDICATOR_GRAVITY_STRETCH
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorGravity {
    }

    /**
     * Indicator animation mode used to translate the selected tab indicator between two tabs using a
     * linear motion.
     *
     * <p>The left and right side of the selection indicator translate in step over the duration of
     * the animation. The only exception to this is when the indicator needs to change size to fit the
     * width of its new destination tab's label.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorAnimationMode
     * @see #setTabIndicatorAnimationMode(int)
     * @see #getTabIndicatorAnimationMode()
     */
    public static final int INDICATOR_ANIMATION_MODE_LINEAR = 0;

    /**
     * Indicator animation mode used to translate the selected tab indicator by growing and then
     * shrinking the indicator, making the indicator look like it is stretching while translating
     * between destinations.
     *
     * <p>The left and right side of the selection indicator translate out of step - with the right
     * decelerating and the left accelerating (when moving right). This difference in velocity between
     * the sides of the indicator, over the duration of the animation, make the indicator look like it
     * grows and then shrinks back down to fit it's new destination's width.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorAnimationMode
     * @see #setTabIndicatorAnimationMode(int)
     * @see #getTabIndicatorAnimationMode()
     */
    public static final int INDICATOR_ANIMATION_MODE_ELASTIC = 1;

    /**
     * Indicator animation mode used to switch the selected tab indicator from one tab to another
     * by sequentially fading it out from the current destination and in at its new destination.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorAnimationMode
     * @see #setTabIndicatorAnimationMode(int)
     * @see #getTabIndicatorAnimationMode()
     */
    public static final int INDICATOR_ANIMATION_MODE_FADE = 2;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {
            INDICATOR_ANIMATION_MODE_LINEAR,
            INDICATOR_ANIMATION_MODE_ELASTIC,
            INDICATOR_ANIMATION_MODE_FADE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorAnimationMode {
    }

    /**
     * 选项卡的选择状态更改时调用的回调接口。
     */
    public interface OnTabSelectedListener<T extends Tab> {
        /**
         * 当选项卡进入选定状态时调用。
         */
        public void onTabSelected(T tab);

        /**
         * 当选项卡退出选定状态时调用。
         */
        public void onTabUnselected(T tab);

        /**
         * 当用户再次选择已选择的选项卡时调用。某些应用程序可能
         * 使用此操作可以返回到类别的顶级。
         */
        public void onTabReselected(T tab);
    }

    /**
     * 选项卡的选择状态更改时调用的回调接口。
     */
    public interface OnTabCanSelected<T extends Tab> {
        /**
         * 是否可以选择
         *
         * @return true:可以选择，false：不可以
         */
        public boolean canSelect(Tab tab);
    }


    private final ArrayList<Tab> tabs = new ArrayList<>();
    @Nullable
    private Tab selectedTab;

    @NonNull
    final SlidingTabIndicator slidingTabIndicator;

    int tabPaddingStart;
    int tabPaddingTop;
    int tabPaddingEnd;
    int tabPaddingBottom;

    int tabTextAppearance;
    ColorStateList tabTextColors;
    ColorStateList tabIconTint;
    ColorStateList tabRippleColorStateList;
    @NonNull
    Drawable tabSelectedIndicator;
    private int tabSelectedIndicatorColor = Color.TRANSPARENT;

    android.graphics.PorterDuff.Mode tabIconTintMode;
    float tabTextSize;
    float tabSelectedTextSize = 0;
    int tabTextMaxLines = 1;
    float tabTextMultiLineSize;
    private Boolean tabTextBold;
    private Boolean tabTextSelectedBold;
    Drawable tabBackground;

    int tabMaxWidth = Integer.MAX_VALUE;
    private final int requestedTabMinWidth;
    private final int requestedTabMaxWidth;
    private final int scrollableTabMinWidth;

    private int contentInsetStart;
    private int tabDisplayNum;
    @TabGravity
    int tabGravity;
    int tabIndicatorAnimationDuration;
    @TabIndicatorGravity
    int tabIndicatorGravity;
    @Mode
    int mode;
    private int tabOneCountGravity;
    private int dividerWidth;
    private int dividerHeight;
    private int dividerColor;
    private int dividerGravity;
    /**
     * 图标与文字距离
     */
    private int iconAndTextSpace;

    boolean inlineLabel;
    boolean tabIndicatorFullWidth;
    int tabIndicatorWidth;
    int tabIndicatorHeight = SELECTED_INDICATOR_HEIGHT_DEFAULT;
    @TabIndicatorAnimationMode
    int tabIndicatorAnimationMode;
    boolean unboundedRipple;

    private TabIndicatorInterpolator tabIndicatorInterpolator;


    private final ArrayList<OnTabSelectedListener> selectedListeners = new ArrayList<>();
    final ArrayList<OnTabCanSelected> tabCanSelected = new ArrayList<>();

    /**
     * 适配ViewPager
     */
    @Nullable
    public OnTabSelectedListener currentVpSelectedListener;

    private ValueAnimator scrollAnimator;

    @Nullable
    ViewPager viewPager;
    @Nullable
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private XTabLayoutOnPageChangeListener pageChangeListener;
    private AdapterChangeListener adapterChangeListener;
    private boolean setupViewPagerImplicitly;

    // Pool we use as a simple RecyclerBin
    private final Pools.Pool<TabView> tabViewPool = new Pools.SimplePool<>(12);

    public XTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public XTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.tabStyle);
    }

    public XTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(wrap(context, attrs, 0, DEF_STYLE_RES), attrs, 0);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);

        // Add the TabStrip
        slidingTabIndicator = new SlidingTabIndicator(context);
        super.addView(
                slidingTabIndicator,
                0,
                new HorizontalScrollView.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        //context.obtainStyledAttributes(attrs,  R.styleable.XTabLayout, defStyleAttr, DEF_STYLE_RES)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XTabLayout, 0, DEF_STYLE_RES);
//        TypedArray a = ThemeEnforcement.obtainStyledAttributes(
//                context,
//                attrs,
//                R.styleable.XTabLayout,
//                defStyleAttr,
//                DEF_STYLE_RES,
//                R.styleable.XTabLayout_xTabTextAppearance);
        if (getBackground() == null) {
            setBackground(new ColorDrawable(0));
        }
        if (getBackground() instanceof ColorDrawable) {
            ColorDrawable background = (ColorDrawable) getBackground();
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
            materialShapeDrawable.initializeElevationOverlay(context);
            materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
            ViewCompat.setBackground(this, materialShapeDrawable);
        }

        setSelectedTabIndicator(
                MaterialResources.getDrawable(context, a, R.styleable.XTabLayout_xTabIndicator));
        setSelectedTabIndicatorColor(
                a.getColor(R.styleable.XTabLayout_xTabIndicatorColor, Color.TRANSPARENT));
        slidingTabIndicator.setSelectedIndicatorHeight(
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIndicatorHeight, -1));
        setSelectedTabIndicatorGravity(
                a.getInt(R.styleable.XTabLayout_xTabIndicatorGravity, INDICATOR_GRAVITY_BOTTOM));
        setTabIndicatorAnimationMode(
                a.getInt(R.styleable.XTabLayout_xTabIndicatorAnimationMode, INDICATOR_ANIMATION_MODE_LINEAR));
        boolean fullwidth = a.getBoolean(R.styleable.XTabLayout_xTabIndicatorFullWidth, true);
        if (a.hasValue(R.styleable.XTabLayout_xTabIndicatorWidthWidthText)) {
            fullwidth = !a.getBoolean(R.styleable.XTabLayout_xTabIndicatorWidthWidthText, !fullwidth);
        }
        setTabIndicatorFullWidth(fullwidth);
        setSelectedIndicatorWidth(
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIndicatorWidth, 0));
        tabPaddingStart = tabPaddingTop = tabPaddingEnd = tabPaddingBottom = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPadding, 0);
        tabPaddingStart =
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingStart, tabPaddingStart);
        tabPaddingTop = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingTop, tabPaddingTop);
        tabPaddingEnd = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingEnd, tabPaddingEnd);
        tabPaddingBottom =
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabPaddingBottom, tabPaddingBottom);

        tabTextAppearance =
                a.getResourceId(R.styleable.XTabLayout_xTabTextAppearance, R.style.TextAppearance_Design_XTab);

        tabTextSize = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabTextSize, 0);
        tabTextMaxLines = a.getInt(R.styleable.XTabLayout_xTabTextMaxLines, tabTextMaxLines);
        if (a.hasValue(R.styleable.XTabLayout_xTabTextBold)) {
            tabTextBold = a.getBoolean(R.styleable.XTabLayout_xTabTextBold, false);
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabTextSelectedBold)) {
            tabTextSelectedBold = a.getBoolean(R.styleable.XTabLayout_xTabTextSelectedBold, false);
        }
        // Text colors/sizes come from the text appearance first
        final TypedArray ta =
                context.obtainStyledAttributes(
                        tabTextAppearance, androidx.appcompat.R.styleable.TextAppearance);
        try {
            tabTextSize =
                    ta.getDimensionPixelSize(
                            androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            tabTextColors =
                    MaterialResources.getColorStateList(
                            context,
                            ta,
                            androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        } finally {
            ta.recycle();
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabSelectedTextSize)) {
            tabSelectedTextSize = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabSelectedTextSize, 0);
        } else {
            tabSelectedTextSize = tabTextSize;
        }
        if (a.hasValue(R.styleable.XTabLayout_xTabTextColor)) {
            // If we have an explicit text color set, use it instead
            tabTextColors =
                    MaterialResources.getColorStateList(context, a, R.styleable.XTabLayout_xTabTextColor);
        }

        if (a.hasValue(R.styleable.XTabLayout_xTabSelectedTextColor)) {
            // We have an explicit selected text color set, so we need to make merge it with the
            // current colors. This is exposed so that developers can use theme attributes to set
            // this (theme attrs in ColorStateLists are Lollipop+)
            final int selected = a.getColor(R.styleable.XTabLayout_xTabSelectedTextColor, 0);
            tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), selected);
        }

        tabIconTint =
                MaterialResources.getColorStateList(context, a, R.styleable.XTabLayout_xTabIconTint);
        tabIconTintMode =
                ViewUtils.parseTintMode(a.getInt(R.styleable.XTabLayout_xTabIconTintMode, -1), null);

        tabRippleColorStateList =
                MaterialResources.getColorStateList(context, a, R.styleable.XTabLayout_xTabRippleColor);

        tabIndicatorAnimationDuration =
                a.getInt(R.styleable.XTabLayout_xTabIndicatorAnimationDuration, ANIMATION_DURATION);
        tabDisplayNum = a.getInt(R.styleable.XTabLayout_xTabDisplayNum, 0);
        requestedTabMinWidth =
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabMinWidth, INVALID_WIDTH);
        requestedTabMaxWidth =
                a.getDimensionPixelSize(R.styleable.XTabLayout_xTabMaxWidth, INVALID_WIDTH);
        tabBackground = MaterialResources.getDrawable(context, a, R.styleable.XTabLayout_xTabBackground);
        contentInsetStart = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabContentStart, 0);
        // noinspection WrongConstant
        mode = a.getInt(R.styleable.XTabLayout_xTabMode, MODE_AUTO);
        tabOneCountGravity = a.getInt(R.styleable.XTabLayout_xTabOneCountGravity, Gravity.LEFT);
        tabGravity = a.getInt(R.styleable.XTabLayout_xTabGravity, GRAVITY_FILL);
        inlineLabel = a.getBoolean(R.styleable.XTabLayout_xTabInlineLabel, false);
        unboundedRipple = a.getBoolean(R.styleable.XTabLayout_xTabUnboundedRipple, false);


        dividerWidth = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabDividerWidth, 0);
        dividerHeight = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabDividerHeight, 0);
        dividerColor = a.getColor(R.styleable.XTabLayout_xTabDividerColor, Color.TRANSPARENT);
        dividerGravity = a.getInteger(R.styleable.XTabLayout_xTabDividerGravity, DividerDrawable.CENTER);
        iconAndTextSpace = a.getDimensionPixelSize(R.styleable.XTabLayout_xTabIconAndTextSpace, (int) ViewUtils.dpToPx(getContext(), DEFAULT_GAP_TEXT_ICON));
        a.recycle();

        // TODO add attr for these
        final Resources res = getResources();
        tabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
        scrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);

        // Now apply the tab mode and gravity
        applyModeAndGravity();
        addDivider();
    }

    /**
     * 添加分割线
     */
    private void addDivider() {
        if (dividerWidth > 0) {
            slidingTabIndicator.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            DividerDrawable dividerDrawable = new DividerDrawable(getContext());
            dividerDrawable.setDividerSize(dividerWidth, dividerHeight);
            dividerDrawable.setColor(dividerColor);
            dividerDrawable.setGravity(dividerGravity);
            slidingTabIndicator.setDividerDrawable(dividerDrawable);
        }
    }

    /**
     * Sets the tab indicator's color for the currently selected tab.
     *
     * <p>If the tab indicator color is not {@code Color.TRANSPARENT}, the indicator will be wrapped
     * and tinted right before it is drawn by {@link SlidingTabIndicator#draw(Canvas)}. If you'd like
     * the inherent color or the tinted color of a custom drawable to be used, make sure this color is
     * set to {@code Color.TRANSPARENT} to avoid your color/tint being overridden.
     *
     * @param color color to use for the indicator
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorColor
     */
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        this.tabSelectedIndicatorColor = color;
        MyTabUtils.setTint(tabSelectedIndicator, tabSelectedIndicatorColor);
        updateTabViews(false);
    }

    /**
     * Sets the tab indicator's height for the currently selected tab.
     *
     * @param height height to use for the indicator in pixels
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorHeight
     * @deprecated If possible, set the intrinsic height directly on a custom indicator drawable
     * passed to {@link #setSelectedTabIndicator(Drawable)}.
     */
    @Deprecated
    public void setSelectedTabIndicatorHeight(int height) {
        tabIndicatorHeight = height;
        slidingTabIndicator.setSelectedIndicatorHeight(height);
    }

    /**
     * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
     * part of a scrolling container such as {@link androidx.viewpager.widget.ViewPager}.
     *
     * <p>Calling this method does not update the selected tab, it is only used for drawing purposes.
     *
     * @param position           current scroll position
     * @param positionOffset     Value from [0, 1) indicating the offset from {@code position}.
     * @param updateSelectedText Whether to update the text's selected state.
     * @see #setScrollPosition(int, float, boolean, boolean)
     */
    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    /**
     * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
     * part of a scrolling container such as {@link androidx.viewpager.widget.ViewPager}.
     *
     * <p>Calling this method does not update the selected tab, it is only used for drawing purposes.
     *
     * @param position                current scroll position
     * @param positionOffset          Value from [0, 1) indicating the offset from {@code position}.
     * @param updateSelectedText      Whether to update the text's selected state.
     * @param updateIndicatorPosition Whether to set the indicator to the given position and offset.
     * @see #setScrollPosition(int, float, boolean)
     */
    public void setScrollPosition(
            int position,
            float positionOffset,
            boolean updateSelectedText,
            boolean updateIndicatorPosition) {
        final int roundedPosition = Math.round(position + positionOffset);
        if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
            return;
        }

        // Set the indicator position, if enabled
        if (updateIndicatorPosition) {
            slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
        }

        // Now update the scroll position, canceling any running animation
        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
        scrollTo(position < 0 ? 0 : calculateScrollXForTab(position, positionOffset), 0);

        // Update the 'selected state' view as we scroll, if enabled
        if (updateSelectedText) {
            setSelectedTabView(roundedPosition);
        }
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list. If this is the first
     * tab to be added it will become the selected tab.
     *
     * @param tab Tab to add
     */
    public void addTab(@NonNull Tab tab) {
        addTab(tab, tabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>. If this is the
     * first tab to be added it will become the selected tab.
     *
     * @param tab      The tab to add
     * @param position The new position of the tab
     */
    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, tabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     *
     * @param tab         Tab to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        addTab(tab, tabs.size(), setSelected);
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>.
     *
     * @param tab         The tab to add
     * @param position    The new position of the tab
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.parent != this) {
            throw new IllegalArgumentException("Tab belongs to a different XTabLayout.");
        }
        configureTab(tab, position);
        addTabView(tab);

        if (setSelected) {
            tab.select();
        }
    }

    private void addTabFromItemView(@NonNull TabItem item) {
        final Tab tab = newTab();
        if (item.text != null) {
            tab.setText(item.text);
        }
        if (item.icon != null) {
            tab.setIcon(item.icon);
        }
        if (item.customLayout != 0) {
            tab.setCustomView(item.customLayout);
        }
        if (!TextUtils.isEmpty(item.getContentDescription())) {
            tab.setContentDescription(item.getContentDescription());
        }
        addTab(tab);
    }

    private boolean isScrollingEnabled() {
        return getTabMode() == MODE_SCROLLABLE || getTabMode() == MODE_AUTO;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // When a touch event is intercepted and the tab mode is fixed, do not continue to process the
        // touch event. This will prevent unexpected scrolling from occurring in corner cases (i.e. a
        // layout in fixed mode that has padding should not scroll for the width of the padding).
        return isScrollingEnabled() && super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_SCROLL && !isScrollingEnabled()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Add a {@link XTabLayout.OnTabSelectedListener} that will be invoked when tab selection changes.
     *
     * <p>Components that add a listener should take care to remove it when finished via {@link
     * #removeOnTabSelectedListener(OnTabSelectedListener)}.
     *
     * @param listener listener to add
     */
    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        if (!selectedListeners.contains(listener)) {
            selectedListeners.add(listener);
        }
    }

    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        selectedListeners.remove(listener);
    }


    /**
     * Remove all previously added {@link XTabLayout.OnTabSelectedListener}s.
     */
    public void clearOnTabSelectedListeners() {
        selectedListeners.clear();
    }

    /**
     * Create and return a new {@link Tab}. You need to manually add this using {@link #addTab(Tab)}
     * or a related method.
     *
     * @return A new Tab
     * @see #addTab(Tab)
     */
    @NonNull
    public Tab newTab() {
        Tab tab = createTabFromPool();
        tab.parent = this;
        tab.view = createTabView(tab);
        if (tab.id != NO_ID) {
            tab.view.setId(tab.id);
        }

        return tab;
    }

    // TODO(b/76413401): remove this method and just create the final field after the widget migration
    protected Tab createTabFromPool() {
        Tab tab = tabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }
        return tab;
    }

    // TODO(b/76413401): remove this method and just create the final field after the widget migration
    protected boolean releaseFromTabPool(Tab tab) {
        return tabPool.release(tab);
    }

    /**
     * Returns the number of tabs currently registered with the action bar.
     *
     * @return Tab count
     */
    public int getTabCount() {
        return tabs.size();
    }

    /**
     * Returns the tab at the specified index.
     */
    @Nullable
    public Tab getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or {@code -1} if there isn't a selected tab.
     */
    public int getSelectedTabPosition() {
        return selectedTab != null ? selectedTab.getPosition() : -1;
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
     * tab will be selected if present.
     *
     * @param tab The tab to remove
     */
    public void removeTab(@NonNull Tab tab) {
        if (tab.parent != this) {
            throw new IllegalArgumentException("Tab does not belong to this XTabLayout.");
        }

        removeTabAt(tab.getPosition());
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
     * tab will be selected if present.
     *
     * @param position Position of the tab to remove
     */
    public void removeTabAt(int position) {
        final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
        removeTabViewAt(position);

        final Tab removedTab = tabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            releaseFromTabPool(removedTab);
        }

        final int newTabCount = tabs.size();
        for (int i = position; i < newTabCount; i++) {
            tabs.get(i).setPosition(i);
        }

        if (selectedTabPosition == position) {
            selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
        }
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    public void removeAllTabs() {
        // Remove all the views
        for (int i = slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
            removeTabViewAt(i);
        }

        for (final Iterator<Tab> i = tabs.iterator(); i.hasNext(); ) {
            final Tab tab = i.next();
            i.remove();
            tab.reset();
            releaseFromTabPool(tab);
        }

        selectedTab = null;
    }

    /**
     * Set the behavior mode for the Tabs in this layout. The valid input options are:
     *
     * <ul>
     *   <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used with
     *       content that benefits from quick pivots between tabs.
     *   <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
     *       and can contain longer tab labels and a larger number of tabs. They are best used for
     *       browsing contexts in touch interfaces when users don’t need to directly compare the tab
     *       labels. This mode is commonly used with a {@link androidx.viewpager.widget.ViewPager}.
     * </ul>
     *
     * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}.
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabMode
     */
    public void setTabMode(@Mode int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            applyModeAndGravity();
        }
    }

    /**
     * Returns the current mode used by this {@link XTabLayout}.
     *
     * @see #setTabMode(int)
     */
    @Mode
    public int getTabMode() {
        return mode;
    }

    /**
     * Set the gravity to use when laying out the tabs.
     *
     * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabGravity
     */
    public void setTabGravity(@TabGravity int gravity) {
        if (tabGravity != gravity) {
            tabGravity = gravity;
            applyModeAndGravity();
        }
    }

    /**
     * The current gravity used for laying out tabs.
     *
     * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     */
    @TabGravity
    public int getTabGravity() {
        return tabGravity;
    }

    /**
     * Set the indicator gravity used to align the tab selection indicator in the {@link XTabLayout}.
     * You must set the indicator height via the custom indicator drawable's intrinsic height
     * (preferred), via the {@code tabIndicatorHeight} attribute (deprecated), or via {@link
     * #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the indicator will not be shown
     * unless gravity is set to {@link #INDICATOR_GRAVITY_STRETCH}, in which case it will ignore
     * indicator height and stretch across the entire height and width of the {@link XTabLayout}. This
     * defaults to {@link #INDICATOR_GRAVITY_BOTTOM} if not set.
     *
     * @param indicatorGravity one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link
     *                         #INDICATOR_GRAVITY_CENTER}, {@link #INDICATOR_GRAVITY_TOP}, or {@link
     *                         #INDICATOR_GRAVITY_STRETCH}
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorGravity
     */
    public void setSelectedTabIndicatorGravity(@TabIndicatorGravity int indicatorGravity) {
        if (tabIndicatorGravity != indicatorGravity) {
            tabIndicatorGravity = indicatorGravity;
            ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
        }
    }

    /**
     * Get the current indicator gravity used to align the tab selection indicator in the {@link
     * XTabLayout}.
     *
     * @return one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link #INDICATOR_GRAVITY_CENTER}, {@link
     * #INDICATOR_GRAVITY_TOP}, or {@link #INDICATOR_GRAVITY_STRETCH}
     */
    @TabIndicatorGravity
    public int getTabIndicatorGravity() {
        return tabIndicatorGravity;
    }

    /**
     * Set the mode by which the selection indicator should animate when moving between destinations.
     *
     * <p>Defaults to {@link #INDICATOR_ANIMATION_MODE_LINEAR}. Changing this is useful as a stylistic
     * choice.
     *
     * @param tabIndicatorAnimationMode one of {@link #INDICATOR_ANIMATION_MODE_LINEAR} or {@link
     *                                  #INDICATOR_ANIMATION_MODE_ELASTIC}
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorAnimationMode
     * @see #getTabIndicatorAnimationMode()
     */
    public void setTabIndicatorAnimationMode(
            @TabIndicatorAnimationMode int tabIndicatorAnimationMode) {
        this.tabIndicatorAnimationMode = tabIndicatorAnimationMode;
        switch (tabIndicatorAnimationMode) {
            case INDICATOR_ANIMATION_MODE_LINEAR:
                this.tabIndicatorInterpolator = new TabIndicatorInterpolator();
                break;
            case INDICATOR_ANIMATION_MODE_ELASTIC:
                this.tabIndicatorInterpolator = new ElasticTabIndicatorInterpolator();
                break;
            case INDICATOR_ANIMATION_MODE_FADE:
                this.tabIndicatorInterpolator = new FadeTabIndicatorInterpolator();
                break;
            default:
                throw new IllegalArgumentException(
                        tabIndicatorAnimationMode + " is not a valid TabIndicatorAnimationMode");
        }
    }

    /**
     * Get the current indicator animation mode used to animate the selection indicator between
     * destinations.
     *
     * @return one of {@link #INDICATOR_ANIMATION_MODE_LINEAR} or {@link
     * #INDICATOR_ANIMATION_MODE_ELASTIC}
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorAnimationMode
     * @see #setTabIndicatorAnimationMode(int)
     */
    @TabIndicatorAnimationMode
    public int getTabIndicatorAnimationMode() {
        return tabIndicatorAnimationMode;
    }

    /**
     * 启用或禁用选项，以使选项卡选择指示器适合选项卡项的全宽度，而不是选项卡项的内容。
     * 默认为true。如果设置为false且选项卡项目有文本标签，则选择指示器宽度将设置为文本标签的宽度。
     * 如果选项卡项没有文本标签，但有图标，则选择指示器宽度将设置为图标。如果选项卡项没有这两个选项，
     * 或者计算的宽度小于最小宽度值，则选择指示器宽度将设置为最小宽度值。
     *
     * @param tabIndicatorFullWidth 是否将选择指示器的宽度贴合到全宽度
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorFullWidth
     * @see #isTabIndicatorFullWidth()
     */
    public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
        this.tabIndicatorFullWidth = tabIndicatorFullWidth;
        slidingTabIndicator.jumpIndicatorToSelectedPosition();
        ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }

    public void setSelectedIndicatorWidth(int width) {
        if (tabIndicatorWidth != width) {
            tabIndicatorWidth = width;
            ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
        }
    }

    /**
     * Get whether or not selection indicator width is fit to full width of the tab item, or fit to
     * the tab item's content.
     *
     * @return whether or not selection indicator width is fit to the full width of the tab item
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabIndicatorFullWidth
     * @see #setTabIndicatorFullWidth(boolean)
     */
    public boolean isTabIndicatorFullWidth() {
        return tabIndicatorFullWidth;
    }


    public int getTabIndicatorWidth() {
        return tabIndicatorWidth;
    }

    /**
     * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
     * underneath tab icons.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabInlineLabel
     * @see #isInlineLabel()
     */
    public void setInlineLabel(boolean inline) {
        if (inlineLabel != inline) {
            inlineLabel = inline;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateOrientation();
                }
            }
            applyModeAndGravity();
        }
    }

    /**
     * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
     * underneath tab icons.
     *
     * @param inlineResourceId Resource ID for boolean inline flag
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabInlineLabel
     * @see #isInlineLabel()
     */
    public void setInlineLabelResource(@BoolRes int inlineResourceId) {
        setInlineLabel(getResources().getBoolean(inlineResourceId));
    }

    /**
     * Returns whether tab labels will be displayed inline with tab icons, or if they will be
     * displayed underneath tab icons.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabInlineLabel
     * @see #setInlineLabel(boolean)
     */
    public boolean isInlineLabel() {
        return inlineLabel;
    }

    /**
     * Set whether this {@link XTabLayout} will have an unbounded ripple effect or if ripple will be
     * bound to the tab item size.
     *
     * <p>Defaults to false.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabUnboundedRipple
     * @see #hasUnboundedRipple()
     */
    public void setUnboundedRipple(boolean unboundedRipple) {
        if (this.unboundedRipple != unboundedRipple) {
            this.unboundedRipple = unboundedRipple;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    /**
     * Set whether this {@link XTabLayout} will have an unbounded ripple effect or if ripple will be
     * bound to the tab item size. Defaults to false.
     *
     * @param unboundedRippleResourceId Resource ID for boolean unbounded ripple value
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabUnboundedRipple
     * @see #hasUnboundedRipple()
     */
    public void setUnboundedRippleResource(@BoolRes int unboundedRippleResourceId) {
        setUnboundedRipple(getResources().getBoolean(unboundedRippleResourceId));
    }

    /**
     * Returns whether this {@link XTabLayout} has an unbounded ripple effect, or if ripple is bound to
     * the tab item size.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabUnboundedRipple
     * @see #setUnboundedRipple(boolean)
     */
    public boolean hasUnboundedRipple() {
        return unboundedRipple;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @see #getTabTextColors()
     */
    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (tabTextColors != textColor) {
            tabTextColors = textColor;
            updateAllTabs();
        }
    }

    /**
     * Gets the text colors for the different states (normal, selected) used for the tabs.
     */
    @Nullable
    public ColorStateList getTabTextColors() {
        return tabTextColors;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabTextColor
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabSelectedTextColor
     */
    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    /**
     * Sets the icon tint for the different states (normal, selected) used for the tabs.
     *
     * @see #getTabIconTint()
     */
    public void setTabIconTint(@Nullable ColorStateList iconTint) {
        if (tabIconTint != iconTint) {
            tabIconTint = iconTint;
            updateAllTabs();
        }
    }

    /**
     * Sets the icon tint resource for the different states (normal, selected) used for the tabs.
     *
     * @param iconTintResourceId A color resource to use as icon tint.
     * @see #getTabIconTint()
     */
    public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
        setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
    }

    /**
     * Gets the icon tint for the different states (normal, selected) used for the tabs.
     */
    @Nullable
    public ColorStateList getTabIconTint() {
        return tabIconTint;
    }

    /**
     * Returns the ripple color for this XTabLayout.
     *
     * @return the color (or ColorStateList) used for the ripple
     * @see #setTabRippleColor(ColorStateList)
     */
    @Nullable
    public ColorStateList getTabRippleColor() {
        return tabRippleColorStateList;
    }

    /**
     * Sets the ripple color for this XTabLayout.
     *
     * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
     * than a ripple.
     *
     * @param color color (or ColorStateList) to use for the ripple
     * @attr ref com.google.android.material.R.styleable#XTabLayout_xTabRippleColor
     * @see #getTabRippleColor()
     */
    public void setTabRippleColor(@Nullable ColorStateList color) {
        if (tabRippleColorStateList != color) {
            tabRippleColorStateList = color;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    /**
     * Sets the ripple color resource for this XTabLayout.
     *
     * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
     * than a ripple.
     *
     * @param tabRippleColorResourceId A color resource to use as ripple color.
     * @see #getTabRippleColor()
     */
    public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
        setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
    }

    /**
     * Returns the selection indicator drawable for this XTabLayout.
     *
     * @return The drawable used as the tab selection indicator, if set.
     * @see #setSelectedTabIndicator(Drawable)
     * @see #setSelectedTabIndicator(int)
     */
    @NonNull
    public Drawable getTabSelectedIndicator() {
        return tabSelectedIndicator;
    }

    /**
     * Sets the selection indicator for this XTabLayout. By default, this is a line along the bottom of
     * the tab. If {@code tabIndicatorColor} is specified via the XTabLayout's style or via {@link
     * #setSelectedTabIndicatorColor(int)} the selection indicator will be tinted that color.
     * Otherwise, it will use the colors specified in the drawable.
     *
     * <p>Setting the indicator drawable to null will cause {@link XTabLayout} to use the default,
     * {@link GradientDrawable} line indicator.
     *
     * @param tabSelectedIndicator A drawable to use as the selected tab indicator.
     * @see #setSelectedTabIndicatorColor(int)
     * @see #setSelectedTabIndicator(int)
     */
    public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
        if (tabSelectedIndicator == null) {
            tabSelectedIndicator = new GradientDrawable();
        }
        this.tabSelectedIndicator = DrawableCompat.wrap(tabSelectedIndicator).mutate();
        MyTabUtils.setTint(this.tabSelectedIndicator, tabSelectedIndicatorColor);
        int indicatorHeight =
                tabIndicatorHeight == SELECTED_INDICATOR_HEIGHT_DEFAULT
                        ? this.tabSelectedIndicator.getIntrinsicHeight()
                        : tabIndicatorHeight;
        slidingTabIndicator.setSelectedIndicatorHeight(indicatorHeight);
    }

    /**
     * Sets the drawable resource to use as the selection indicator for this XTabLayout. By default,
     * this is a line along the bottom of the tab. If {@code tabIndicatorColor} is specified via the
     * XTabLayout's style or via {@link #setSelectedTabIndicatorColor(int)} the selection indicator
     * will be tinted that color. Otherwise, it will use the colors specified in the drawable.
     *
     * @param tabSelectedIndicatorResourceId A drawable resource to use as the selected tab indicator.
     * @see #setSelectedTabIndicatorColor(int)
     * @see #setSelectedTabIndicator(Drawable)
     */
    public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
        if (tabSelectedIndicatorResourceId != 0) {
            setSelectedTabIndicator(
                    AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
        } else {
            setSelectedTabIndicator(null);
        }
    }

    /**
     * The one-stop shop for setting up this {@link XTabLayout} with a {@link ViewPager}.
     *
     * <p>This is the same as calling {@link #setupWithViewPager(ViewPager, boolean)} with
     * auto-refresh enabled.
     *
     * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
     */
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    /**
     * The one-stop shop for setting up this {@link XTabLayout} with a {@link ViewPager}.
     *
     * <p>This method will link the given ViewPager and this XTabLayout together so that changes in one
     * are automatically reflected in the other. This includes scroll state changes and clicks. The
     * tabs displayed in this layout will be populated from the ViewPager adapter's page titles.
     *
     * <p>If {@code autoRefresh} is {@code true}, any changes in the {@link PagerAdapter} will trigger
     * this layout to re-populate itself from the adapter's titles.
     *
     * <p>If the given ViewPager is non-null, it needs to already have a {@link PagerAdapter} set.
     *
     * @param viewPager   the ViewPager to link to, or {@code null} to clear any previous link
     * @param autoRefresh whether this layout should refresh its contents if the given ViewPager's
     *                    content changes
     */
    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
        setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(
            @Nullable final ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
        if (this.viewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (pageChangeListener != null) {
                this.viewPager.removeOnPageChangeListener(pageChangeListener);
            }
            if (adapterChangeListener != null) {
                this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
            }
        }

        if (currentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(currentVpSelectedListener);
            currentVpSelectedListener = null;
        }

        if (viewPager != null) {
            this.viewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (pageChangeListener == null) {
                pageChangeListener = new XTabLayoutOnPageChangeListener(this);
            }
            pageChangeListener.reset();
            viewPager.addOnPageChangeListener(pageChangeListener);

            // Now we'll add a tab selected listener to set ViewPager's current item
            currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(currentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (adapterChangeListener == null) {
                adapterChangeListener = new AdapterChangeListener();
            }
            adapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(adapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            setScrollPosition(viewPager.getCurrentItem(), 0f, true);
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            this.viewPager = null;
            setPagerAdapter(null, false);
        }

        setupViewPagerImplicitly = implicitSetup;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        // Only delay the pressed state if the tabs can scroll
        return getTabScrollRange() > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        MaterialShapeUtils.setParentAbsoluteElevation(this);

        if (viewPager == null) {
            // If we don't have a ViewPager already, check if our parent is a ViewPager to
            // setup with it automatically
            final ViewParent vp = getParent();
            if (vp instanceof ViewPager) {
                // If we have a ViewPager parent and we've been added as part of its decor, let's
                // assume that we should automatically setup to display any titles
                setupWithViewPager((ViewPager) vp, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (setupViewPagerImplicitly) {
            // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
            setupWithViewPager(null);
            setupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(
                0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (pagerAdapter != null && pagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
        }

        pagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (pagerAdapterObserver == null) {
                pagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(pagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllTabs();

        if (pagerAdapter != null) {
            final int adapterCount = pagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
            }

            // Make sure we reflect the currently set ViewPager item
            if (viewPager != null && adapterCount > 0) {
                final int curItem = viewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem));
                }
            }
        }
    }

    private void updateAllTabs() {
        for (int i = 0, z = tabs.size(); i < z; i++) {
            tabs.get(i).updateView();
        }
    }

    @NonNull
    private TabView createTabView(@NonNull final Tab tab) {
        TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        if (TextUtils.isEmpty(tab.contentDesc)) {
            tabView.setContentDescription(tab.text);
        } else {
            tabView.setContentDescription(tab.contentDesc);
        }
        return tabView;
    }

    private void configureTab(@NonNull Tab tab, int position) {
        tab.setPosition(position);
        tabs.add(position, tab);

        final int count = tabs.size();
        for (int i = position + 1; i < count; i++) {
            tabs.get(i).setPosition(i);
        }
    }

    private void addTabView(@NonNull Tab tab) {
        final TabView tabView = tab.view;
        tabView.setSelected(false);
        tabView.setActivated(false);
        slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
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
            throw new IllegalArgumentException("Only TabItem instances can be added to XTabLayout");
        }
    }

    @NonNull
    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(@NonNull LinearLayout.LayoutParams lp) {

        if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else if (mode == MODE_AUTO) {
            if (slidingTabIndicator.getMeasuredWidth() < getValidWidth()) {
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

    /**
     * 有效宽度
     */
    private int getValidWidth() {
        if (getMeasuredWidth() == 0) {
            return 0;
        }
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);

        MaterialShapeUtils.setElevation(this, elevation);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
        infoCompat.setCollectionInfo(
                CollectionInfoCompat.obtain(
                        /* rowCount= */ 1,
                        /* columnCount= */ getTabCount(),
                        /* hierarchical= */ false,
                        /* selectionMode = */ CollectionInfoCompat.SELECTION_MODE_SINGLE));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw tab background layer for each tab item
        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View tabView = slidingTabIndicator.getChildAt(i);
            if (tabView instanceof TabView) {
                ((TabView) tabView).drawBackground(canvas);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If we have a MeasureSpec which allows us to decide our height, try and use the default
        // height
        final int idealHeight = Math.round(ViewUtils.dpToPx(getContext(), getDefaultHeight()));
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
                    getChildAt(0).setMinimumHeight(idealHeight);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(
                                idealHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
                break;
            default:
                break;
        }

        final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            // If we don't have an unspecified width spec, use the given size to calculate
            // the max tab width
            if (pagerAdapter != null && tabDisplayNum != 0) {
                if (pagerAdapter.getCount() == 1 || tabDisplayNum == 1) {
                    WindowManager wm = (WindowManager) getContext()
                            .getSystemService(Context.WINDOW_SERVICE);
                    tabMaxWidth = wm.getDefaultDisplay().getWidth();
                } else {
                    tabMaxWidth =
                            requestedTabMaxWidth > 0
                                    ? requestedTabMaxWidth
                                    : (int) (specWidth - ViewUtils.dpToPx(getContext(), TAB_MIN_WIDTH_MARGIN));
                }
            } else {
                tabMaxWidth =
                        requestedTabMaxWidth > 0
                                ? requestedTabMaxWidth
                                : (int) (specWidth - ViewUtils.dpToPx(getContext(), TAB_MIN_WIDTH_MARGIN));
            }
        }

        // Now super measure itself using the (possibly) modified height spec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 1) {
            // If we're in fixed mode then we need to make sure the tab strip is the same width as us
            // so we don't scroll
            final View child = getChildAt(0);
            boolean remeasure = false;

            switch (mode) {
                case MODE_AUTO:
                case MODE_SCROLLABLE:
                    // We only need to resize the child if it's smaller than us. This is similar
                    // to fillViewport
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
                case MODE_FIXED:
                    // Resize the child so that it doesn't scroll
                    remeasure = child.getMeasuredWidth() != getMeasuredWidth();
                    break;
            }

            if (remeasure) {
                // Re-measure the child with a widthSpec set to be exactly our measure width
                int childHeightMeasureSpec =
                        getChildMeasureSpec(
                                heightMeasureSpec,
                                getPaddingTop() + getPaddingBottom(),
                                child.getLayoutParams().height);

                int childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    private void removeTabViewAt(int position) {
        final TabView view = (TabView) slidingTabIndicator.getChildAt(position);
        slidingTabIndicator.removeViewAt(position);
        if (view != null) {
            view.reset();
            tabViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition == Tab.INVALID_POSITION) {
            return;
        }

        if (getWindowToken() == null
                || !ViewCompat.isLaidOut(this)
                || slidingTabIndicator.childrenNeedLayout()) {
            // If we don't have a window token, or we haven't been laid out yet just draw the new
            // position now
            setScrollPosition(newPosition, 0f, true);
            return;
        }

        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(newPosition, 0);

        if (startScrollX != targetScrollX) {
            ensureScrollAnimator();

            scrollAnimator.setIntValues(startScrollX, targetScrollX);
            scrollAnimator.start();
        }

        // Now animate the indicator
        slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
    }

    private void ensureScrollAnimator() {
        if (scrollAnimator == null) {
            scrollAnimator = new ValueAnimator();
            scrollAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            scrollAnimator.setDuration(tabIndicatorAnimationDuration);
            scrollAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                            scrollTo((int) animator.getAnimatedValue(), 0);
                        }
                    });
        }
    }

    void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
        ensureScrollAnimator();
        scrollAnimator.addListener(listener);
    }

    /**
     * Called when a selected tab is added. Unselects all other tabs in the XTabLayout.
     *
     * @param position Position of the selected tab.
     */
    private void setSelectedTabView(int position) {
        final int tabCount = slidingTabIndicator.getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                final View child = slidingTabIndicator.getChildAt(i);
                child.setSelected(i == position);
                child.setActivated(i == position);
            }
        }
    }

    /**
     * Selects the given tab.
     *
     * @param tab The tab to select, or {@code null} to select none.
     * @see #selectTab(Tab, boolean)
     */
    public void selectTab(@Nullable Tab tab) {
        selectTab(tab, true);
    }

    /**
     * 选择给定的选项卡。如果当前选项卡为
     * 无论{@codeupdateIndicator}的值如何，都可以重新选择。
     *
     * @param tab             要选择的选项卡，或｛@code null｝选择无。
     * @param updateIndicator 是否为选定选项卡设置动画。
     * @see #selectTab(Tab)
     */
    public void selectTab(@Nullable final Tab tab, boolean updateIndicator) {
        //拦截事件
        boolean isCan = true;
        for (int i = tabCanSelected.size() - 1; i >= 0; i--) {
            if (!tabCanSelected.get(i).canSelect(tab)) {
                isCan = false;
            }
        }
        if (!isCan) return;
        final Tab currentTab = selectedTab;
        if (currentTab == tab) {
            if (currentTab != null) {
                dispatchTabReselected(tab);
                animateToTab(tab.getPosition());
            }
        } else {
            final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
            if (updateIndicator) {
                if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
                        && newPosition != Tab.INVALID_POSITION) {
                    // If we don't currently have a tab, just draw the indicator
                    setScrollPosition(newPosition, 0f, true);
                } else {
                    animateToTab(newPosition);
                }
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition);
                }
            }
            // 在调度“tab unselected”事件之前设置selectedTab，以便currentTab的状态
            //将被解释为未选中
            selectedTab = tab;
            if (currentTab != null) {
                dispatchTabUnselected(currentTab);
            }
            if (tab != null) {
                dispatchTabSelected(tab);
            }
        }
    }

    private void dispatchTabSelected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabReselected(tab);
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
            final View selectedChild = slidingTabIndicator.getChildAt(position);
            if (selectedChild == null) {
                return 0;
            }
            final View nextChild =
                    position + 1 < slidingTabIndicator.getChildCount()
                            ? slidingTabIndicator.getChildAt(position + 1)
                            : null;
            final int selectedWidth = selectedChild.getWidth();
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            // base scroll amount: places center of tab in center of parent
            int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
            // offset amount: fraction of the distance between centers of tabs
            int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

            return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
                    ? scrollBase + scrollOffset
                    : scrollBase - scrollOffset;
        }
        return 0;
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
            // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, contentInsetStart - tabPaddingStart);
        }
        ViewCompat.setPaddingRelative(slidingTabIndicator, paddingStart, 0, 0, 0);

        switch (mode) {
            case MODE_FIXED:
                if (tabGravity == GRAVITY_START) {
                    Log.w(
                            LOG_TAG,
                            "GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be"
                                    + " used instead");
                }
                slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case MODE_AUTO:
            case MODE_SCROLLABLE:
                applyGravityForModeScrollable(tabGravity);
                break;
        }

        updateTabViews(true);
    }

    private void applyGravityForModeScrollable(int tabGravity) {
        switch (tabGravity) {
            case GRAVITY_CENTER:
                slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case GRAVITY_FILL:
                Log.w(
                        LOG_TAG,
                        "MODE_SCROLLABLE + GRAVITY_FILL is not supported, GRAVITY_START will be used"
                                + " instead");
                // Fall through
            case GRAVITY_START:
                slidingTabIndicator.setGravity(GravityCompat.START);
                break;
            default:
                break;
        }
    }

    void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View child = slidingTabIndicator.getChildAt(i);
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
    // TODO(b/76413401): make class final after the widget migration is finished
    public static class Tab {

        /**
         * An invalid position for a tab.
         *
         * @see #getPosition()
         */
        public static final int INVALID_POSITION = -1;

        @Nullable
        private Object tag;
        @Nullable
        private Drawable icon;
        @Nullable
        private CharSequence text;
        // This represents the content description that has been explicitly set on the Tab or TabItem
        // in XML or through #setContentDescription. If the content description is empty, text should
        // be used as the content description instead, but contentDesc should remain empty.
        @Nullable
        private CharSequence contentDesc;
        private int position = INVALID_POSITION;
        @Nullable
        private View customView;
        private @LabelVisibility
        int labelVisibilityMode = TAB_LABEL_VISIBILITY_LABELED;

        // TODO(b/76413401): make package private after the widget migration is finished
        @Nullable
        public XTabLayout parent;
        // TODO(b/76413401): make package private after the widget migration is finished
        @NonNull
        public TabView view;
        private int id = NO_ID;

        // TODO(b/76413401): make package private constructor after the widget migration is finished
        public Tab() {
            // Private constructor
        }

        /**
         * @return This Tab's tag object.
         */
        @Nullable
        public Object getTag() {
            return tag;
        }

        /**
         * Give this Tab an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setTag(@Nullable Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Give this tab an id, useful for testing.
         *
         * <p>Do not rely on this if using {@link XTabLayout#setupWithViewPager(ViewPager)}
         *
         * @param id, unique id for this tab
         */
        @NonNull
        public Tab setId(int id) {
            this.id = id;
            if (view != null) {
                view.setId(id);
            }
            return this;
        }

        /**
         * Returns the id for this tab, {@code View.NO_ID} if not set.
         */
        public int getId() {
            return id;
        }

        /**
         * Returns the custom view used for this tab.
         *
         * @see #setCustomView(View)
         * @see #setCustomView(int)
         */
        @Nullable
        public View getCustomView() {
            return customView;
        }

        /**
         * Set a custom view to be used for this tab.
         *
         * <p>If the provided view contains a {@link TextView} with an ID of {@link android.R.id#text1}
         * then that will be updated with the value given to {@link #setText(CharSequence)}. Similarly,
         * if this layout contains an {@link ImageView} with ID {@link android.R.id#icon} then it will
         * be updated with the value given to {@link #setIcon(Drawable)}.
         *
         * @param view Custom view to be used as a tab.
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@Nullable View view) {
            customView = view;
            updateView();
            return this;
        }

        /**
         * Set a custom view to be used for this tab.
         *
         * <p>If the inflated layout contains a {@link TextView} with an ID of {@link
         * android.R.id#text1} then that will be updated with the value given to {@link
         * #setText(CharSequence)}. Similarly, if this layout contains an {@link ImageView} with ID
         * {@link android.R.id#icon} then it will be updated with the value given to {@link
         * #setIcon(Drawable)}.
         *
         * @param resId A layout resource to inflate and use as a custom tab view
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@LayoutRes int resId) {
            final LayoutInflater inflater = LayoutInflater.from(view.getContext());
            return setCustomView(inflater.inflate(resId, view, false));
        }

        /**
         * Return the icon associated with this tab.
         *
         * @return The tab's icon
         */
        @Nullable
        public Drawable getIcon() {
            return icon;
        }

        /**
         * Return the current position of this tab in the action bar.
         *
         * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in the
         * action bar.
         */
        public int getPosition() {
            return position;
        }

        void setPosition(int position) {
            this.position = position;
        }

        /**
         * Return the text of this tab.
         *
         * @return The tab's text
         */
        @Nullable
        public CharSequence getText() {
            return text;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param icon The drawable to use as an icon
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            this.icon = icon;
            if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
                parent.updateTabViews(true);
            }
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
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a XTabLayout");
            }
            return setIcon(AppCompatResources.getDrawable(parent.getContext(), resId));
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display the
         * entire string.
         *
         * @param text The text to display
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
                // If no content description has been set, use the text as the content description of the
                // TabView. If the text is null, don't update the content description.
                view.setContentDescription(text);
            }

            this.text = text;
            updateView();
            return this;
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display the
         * entire string.
         *
         * @param resId A resource ID referring to the text that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a XTabLayout");
            }
            return setText(parent.getResources().getText(resId));
        }

        /**
         * Creates an instance of {@link BadgeDrawable} if none exists. Initializes (if needed) and
         * returns the associated instance of {@link BadgeDrawable}.
         *
         * @return an instance of BadgeDrawable associated with {@code Tab}.
         */
        @NonNull
        public BadgeDrawable getOrCreateBadge() {
            return view.getOrCreateBadge();
        }

        /**
         * Removes the {@link BadgeDrawable}. Do nothing if none exists. Consider changing the
         * visibility of the {@link BadgeDrawable} if you only want to hide it temporarily.
         */
        public void removeBadge() {
            view.removeBadge();
        }

        /**
         * Returns an instance of {@link BadgeDrawable} associated with this tab, null if none was
         * initialized.
         */
        @Nullable
        public BadgeDrawable getBadge() {
            return view.getBadge();
        }

        /**
         * Sets the visibility mode for the Labels in this Tab. The valid input options are:
         *
         * <ul>
         *   <li>{@link #TAB_LABEL_VISIBILITY_UNLABELED}: Tabs will appear without labels regardless of
         *       whether text is set.
         *   <li>{@link #TAB_LABEL_VISIBILITY_LABELED}: Tabs will appear labeled if text is set.
         * </ul>
         *
         * @param mode one of {@link #TAB_LABEL_VISIBILITY_UNLABELED} or {@link
         *             #TAB_LABEL_VISIBILITY_LABELED}.
         * @return The current instance for call chaining.
         */
        @NonNull
        public Tab setTabLabelVisibility(@LabelVisibility int mode) {
            this.labelVisibilityMode = mode;
            if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
                parent.updateTabViews(true);
            }
            this.updateView();
            return this;
        }

        /**
         * Gets the visibility mode for the Labels in this Tab.
         *
         * @return the label visibility mode, one of {@link #TAB_LABEL_VISIBILITY_UNLABELED} or {@link
         * #TAB_LABEL_VISIBILITY_LABELED}.
         * @see #setTabLabelVisibility(int)
         */
        @LabelVisibility
        public int getTabLabelVisibility() {
            return this.labelVisibilityMode;
        }

        /**
         * Select this tab. Only valid if the tab has been added to the action bar.
         */
        public void select() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a XTabLayout");
            }
            parent.selectTab(this);
        }

        /**
         * Returns true if this tab is currently selected.
         */
        public boolean isSelected() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a XTabLayout");
            }
            int selectedPosition = parent.getSelectedTabPosition();
            return selectedPosition != INVALID_POSITION && selectedPosition == position;
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param resId A resource ID referring to the description text
         * @return The current instance for call chaining
         * @see #setContentDescription(CharSequence)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a XTabLayout");
            }
            return setContentDescription(parent.getResources().getText(resId));
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
            this.contentDesc = contentDesc;
            updateView();
            return this;
        }

        /**
         * Gets a brief description of this tab's content for use in accessibility support.
         *
         * @return Description of this tab's content
         * @see #setContentDescription(CharSequence)
         * @see #setContentDescription(int)
         */
        @Nullable
        public CharSequence getContentDescription() {
            // This returns the view's content description instead of contentDesc because if the title
            // is used as a replacement for the content description, contentDesc will be empty.
            return (view == null) ? null : view.getContentDescription();
        }

        void updateView() {
            if (view != null) {
                view.update();
            }
        }

        void reset() {
            parent = null;
            view = null;
            tag = null;
            icon = null;
            id = NO_ID;
            text = null;
            contentDesc = null;
            position = INVALID_POSITION;
            customView = null;
        }
    }

    /**
     * A {@link LinearLayout} containing {@link Tab} instances for use with {@link XTabLayout}.
     */
    public final class TabView extends LinearLayout {
        private Tab tab;
        private TextView textView;
        private ImageView iconView;
        @Nullable
        private View badgeAnchorView;
        @Nullable
        private BadgeDrawable badgeDrawable;

        @Nullable
        private View customView;
        @Nullable
        private TextView customTextView;
        @Nullable
        private ImageView customIconView;
        @Nullable
        private Drawable baseBackgroundDrawable;

        public TabView(@NonNull Context context) {
            super(context);
            updateBackgroundDrawable(context);
            ViewCompat.setPaddingRelative(
                    this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom);
            setGravity(Gravity.CENTER);
            setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
            setClickable(true);
            ViewCompat.setPointerIcon(
                    this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
            updateBaseBackgroundDrawable();
        }

        private void updateBaseBackgroundDrawable() {
            if (tabBackground != null) {
                if (baseBackgroundDrawable == null) {
                    baseBackgroundDrawable = DrawableCompat.wrap(tabBackground).mutate();
                }
            } else {
                baseBackgroundDrawable = null;
            }
        }

        private void updateBackgroundDrawable(Context context) {
            updateBaseBackgroundDrawable();
            Drawable background;
            Drawable contentDrawable = new GradientDrawable();
            ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);

            if (tabRippleColorStateList != null) {
                GradientDrawable maskDrawable = new GradientDrawable();
                // TODO: Find a workaround for this. Currently on certain devices/versions,
                // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
                // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
                maskDrawable.setCornerRadius(0.00001F);
                maskDrawable.setColor(Color.WHITE);

                ColorStateList rippleColor =
                        RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);

                background = new RippleDrawable(
                        rippleColor,
                        unboundedRipple ? null : contentDrawable,
                        unboundedRipple ? null : maskDrawable);
            } else {
                background = contentDrawable;
            }
            this.setBackground(background);
            XTabLayout.this.invalidate();
        }

        /**
         * 设置自定义的背景
         */
        public void setBaseBackgroundDrawable(@Nullable Drawable baseBackgroundDrawable) {
            this.baseBackgroundDrawable = baseBackgroundDrawable;
            updateBaseBackgroundDrawable();
        }

        /**
         * Draw the background drawable specified by tabBackground attribute onto the canvas provided.
         * This method will draw the background to the full bounds of this TabView. We provide a
         * separate method for drawing this background rather than just setting this background on the
         * TabView so that we can control when this background gets drawn. This allows us to draw the
         * tab background underneath the XTabLayout selection indicator, and then draw the XTabLayout
         * content (icons + labels) on top of the selection indicator.
         *
         * @param canvas canvas to draw the background on
         */
        private void drawBackground(@NonNull Canvas canvas) {
            if (baseBackgroundDrawable != null) {
                baseBackgroundDrawable.setState(getDrawableState());
                baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                baseBackgroundDrawable.draw(canvas);
            }
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            boolean changed = false;
            int[] state = getDrawableState();
            if (baseBackgroundDrawable != null) {
                changed |= baseBackgroundDrawable.setState(state);
            }

            if (changed) {
                invalidate();
                XTabLayout.this.invalidate(); // Invalidate XTabLayout, which draws mBaseBackgroundDrawable
            }
        }

        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();

            if (tab != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                tab.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(final boolean selected) {
            final boolean changed = isSelected() != selected;

            super.setSelected(selected);

            if (changed && selected && Build.VERSION.SDK_INT < 16) {
                // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            }

            // Always dispatch this to the child views, regardless of whether the value has
            // changed
            if (textView != null) {
                textView.setSelected(selected);
                if (!selected) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                    if (tabTextBold != null) {
                        textView.setTypeface(Typeface.defaultFromStyle(tabTextBold ? Typeface.BOLD : Typeface.NORMAL));
                    }
                }
                if (changed && selected) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabSelectedTextSize);
                    if (tabTextSelectedBold != null) {
                        textView.setTypeface(Typeface.defaultFromStyle(tabTextSelectedBold ? Typeface.BOLD : Typeface.NORMAL));
                    }
                }
            }
            if (iconView != null) {
                iconView.setSelected(selected);
            }
            if (customView != null) {
                customView.setSelected(selected);
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            if (badgeDrawable != null && badgeDrawable.isVisible()) {
                CharSequence customContentDescription = getContentDescription();
                info.setContentDescription(
                        customContentDescription + ", " + badgeDrawable.getContentDescription());
            }
            AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
            infoCompat.setCollectionItemInfo(
                    CollectionItemInfoCompat.obtain(
                            /* rowIndex= */ 0,
                            /* rowSpan= */ 1,
                            /* columnIndex= */ tab.getPosition(),
                            /* columnSpan= */ 1,
                            /* heading= */ false,
                            /* selected= */ isSelected()));
            if (isSelected()) {
                infoCompat.setClickable(false);
                infoCompat.removeAction(AccessibilityActionCompat.ACTION_CLICK);
            }
            infoCompat.setRoleDescription(getResources().getString(R.string.item_view_role_description));
        }

        @Override
        public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
            final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            final int maxWidth = getTabMaxWidth();

            final int widthMeasureSpec;
            final int heightMeasureSpec = origHeightMeasureSpec;

            if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
                // If we have a max width and a given spec which is either unspecified or
                // larger than the max width, update the width spec using the same mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.AT_MOST);
            } else {
                // Else, use the original width spec
                widthMeasureSpec = origWidthMeasureSpec;
            }

            // Now lets measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // We need to switch the text size based on whether the text is spanning 2 lines or not
            if (textView != null) {
                float textSize = tabTextSize;
                int maxLines = tabTextMaxLines;

                if (iconView != null && iconView.getVisibility() == VISIBLE) {
                    // If the icon view is being displayed, we limit the text to 1 line
                    maxLines = 1;
                } else if (textView != null && textView.getLineCount() > 1) {
                    // Otherwise when we have text which wraps we reduce the text size
                    textSize = tabTextMultiLineSize;
                }

                final float curTextSize = textView.getTextSize();
                final Typeface curTypeface = textView.getTypeface();
                final int curLineCount = textView.getLineCount();
                final int curMaxLines = TextViewCompat.getMaxLines(textView);
                if (textView.isSelected()) {
                    textSize = tabSelectedTextSize;
                }
                Typeface newTypeface = null;
                if (textView.isSelected()) {
                    if (tabTextSelectedBold != null) {
                        newTypeface = Typeface.defaultFromStyle(tabTextSelectedBold ? Typeface.BOLD : Typeface.NORMAL);
                    }
                } else {
                    if (tabTextBold != null) {
                        newTypeface = Typeface.defaultFromStyle(tabTextBold ? Typeface.BOLD : Typeface.NORMAL);
                    }
                }
                if (curTypeface != newTypeface) {
                    textView.setTypeface(newTypeface);
                }
                if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
                    // We've got a new text size and/or max lines...
                    boolean updateTextView = true;

                    if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
                        // If we're in fixed mode, going up in text size and currently have 1 line
                        // then it's very easy to get into an infinite recursion.
                        // To combat that we check to see if the change in text size
                        // will cause a line count change. If so, abort the size change and stick
                        // to the smaller size.
                        final Layout layout = textView.getLayout();
                        if (layout == null
                                || approximateLineWidth(layout, 0, textSize)
                                > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                            updateTextView = false;
                        }
                    }

                    if (updateTextView) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        textView.setMaxLines(maxLines);
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }
        }


        void setTab(@Nullable final Tab tab) {
            if (tab != this.tab) {
                this.tab = tab;
                update();
            }
        }

        void reset() {
            setTab(null);
            setSelected(false);
        }

        final void update() {
            final Tab tab = this.tab;
            final View custom = tab != null ? tab.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                customView = custom;
                if (this.textView != null) {
                    this.textView.setVisibility(GONE);
                }
                if (this.iconView != null) {
                    this.iconView.setVisibility(GONE);
                    this.iconView.setImageDrawable(null);
                }

                customTextView = custom.findViewById(android.R.id.text1);
                customIconView = custom.findViewById(android.R.id.icon);
            } else {
                // We do not have a custom view. Remove one if it already exists
                if (customView != null) {
                    removeView(customView);
                    customView = null;
                }
                customTextView = null;
                customIconView = null;
            }

            if (customView == null) {
                // If there isn't a custom view, we'll us our own in-built layouts
                if (this.iconView == null) {
                    inflateAndAddDefaultIconView();
                }
                if (this.textView == null) {
                    inflateAndAddDefaultTextView();
                }
                TextViewCompat.setTextAppearance(this.textView, tabTextAppearance);
                if (tabTextColors != null) {
                    this.textView.setTextColor(tabTextColors);
                }
                updateTextAndIcon(this.textView, this.iconView);

                tryUpdateBadgeAnchor();
                addOnLayoutChangeListener(iconView);
                addOnLayoutChangeListener(textView);
            } else {
                // Else, we'll see if there is a TextView or ImageView present and update them
                if (customTextView != null || customIconView != null) {
                    updateTextAndIcon(customTextView, customIconView);
                }
            }

            if (tab != null && !TextUtils.isEmpty(tab.contentDesc)) {
                // Only update the TabView's content description from Tab if the Tab's content description
                // has been explicitly set.
                setContentDescription(tab.contentDesc);
            }
            // Finally update our selected state
            setSelected(tab != null && tab.isSelected());
        }

        private void inflateAndAddDefaultIconView() {
            ViewGroup iconViewParent = this;
            this.iconView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_xtab_icon, iconViewParent, false);
            iconViewParent.addView(iconView, 0);
        }


        private void inflateAndAddDefaultTextView() {
            ViewGroup textViewParent = this;
            this.textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_xtab_text, textViewParent, false);
            textViewParent.addView(textView);
        }


        /**
         * Creates an instance of {@link BadgeDrawable} if none exists. Initializes (if needed) and
         * returns the associated instance of {@link BadgeDrawable}.
         *
         * @return an instance of BadgeDrawable associated with {@code Tab}.
         */
        @NonNull
        private BadgeDrawable getOrCreateBadge() {
            // Creates a new instance if one is not already initialized for this TabView.
            if (badgeDrawable == null) {
                badgeDrawable = BadgeDrawable.create(getContext());
            }
            tryUpdateBadgeAnchor();
            if (badgeDrawable == null) {
                throw new IllegalStateException("Unable to create badge");
            }
            return badgeDrawable;
        }

        @Nullable
        private BadgeDrawable getBadge() {
            return badgeDrawable;
        }

        private void removeBadge() {
            if (badgeAnchorView != null) {
                tryRemoveBadgeFromAnchor();
            }
            badgeDrawable = null;
        }

        private void addOnLayoutChangeListener(@Nullable final View view) {
            if (view == null) {
                return;
            }
            view.addOnLayoutChangeListener(
                    new OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(
                                View v,
                                int left,
                                int top,
                                int right,
                                int bottom,
                                int oldLeft,
                                int oldTop,
                                int oldRight,
                                int oldBottom) {
                            if (view.getVisibility() == VISIBLE) {
                                tryUpdateBadgeDrawableBounds(view);
                            }
                        }
                    });
        }

        private void tryUpdateBadgeAnchor() {
            if (!hasBadgeDrawable()) {
                return;
            }
            if (customView != null) {
                // TODO(b/123406505): Support badging on custom tab views.
                tryRemoveBadgeFromAnchor();
            } else {
                if (iconView != null && tab != null && tab.getIcon() != null) {
                    if (badgeAnchorView != iconView) {
                        tryRemoveBadgeFromAnchor();
                        // Anchor badge to icon.
                        tryAttachBadgeToAnchor(iconView);
                    } else {
                        tryUpdateBadgeDrawableBounds(iconView);
                    }
                } else if (textView != null
                        && tab != null
                        && tab.getTabLabelVisibility() == TAB_LABEL_VISIBILITY_LABELED) {
                    if (badgeAnchorView != textView) {
                        tryRemoveBadgeFromAnchor();
                        // Anchor badge to label.
                        tryAttachBadgeToAnchor(textView);
                    } else {
                        tryUpdateBadgeDrawableBounds(textView);
                    }
                } else {
                    tryRemoveBadgeFromAnchor();
                }
            }
        }

        private void tryAttachBadgeToAnchor(@Nullable View anchorView) {
            if (!hasBadgeDrawable()) {
                return;
            }
            if (anchorView != null) {
                clipViewToPaddingForBadge(false);
                MyTabUtils.attachBadgeDrawable(
                        badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
                badgeAnchorView = anchorView;
            }
        }

        private void tryRemoveBadgeFromAnchor() {
            if (!hasBadgeDrawable()) {
                return;
            }
            clipViewToPaddingForBadge(true);
            if (badgeAnchorView != null) {
                MyTabUtils.detachBadgeDrawable(badgeDrawable, badgeAnchorView);
                badgeAnchorView = null;
            }
        }

        private void clipViewToPaddingForBadge(boolean flag) {
            // Avoid clipping a badge if it's displayed.
            // Clip children / view to padding when no badge is displayed.
            setClipChildren(flag);
            setClipToPadding(flag);
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                parent.setClipChildren(flag);
                parent.setClipToPadding(flag);
            }
        }

        final void updateOrientation() {
            setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
            if (customTextView != null || customIconView != null) {
                updateTextAndIcon(customTextView, customIconView);
            } else {
                updateTextAndIcon(textView, iconView);
            }
        }

        private void updateTextAndIcon(
                @Nullable final TextView textView, @Nullable final ImageView iconView) {
            final Drawable icon =
                    (tab != null && tab.getIcon() != null)
                            ? DrawableCompat.wrap(tab.getIcon()).mutate()
                            : null;
            if (icon != null) {
                DrawableCompat.setTintList(icon, tabIconTint);
                if (tabIconTintMode != null) {
                    DrawableCompat.setTintMode(icon, tabIconTintMode);
                }
            }

            final CharSequence text = tab != null ? tab.getText() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    if (tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED) {
                        textView.setVisibility(VISIBLE);
                    } else {
                        textView.setVisibility(GONE);
                    }
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int bottomMargin = 0;
                if (hasText && iconView.getVisibility() == VISIBLE) {
                    bottomMargin = iconAndTextSpace;
                }
                if (inlineLabel) {
                    if (bottomMargin != MarginLayoutParamsCompat.getMarginEnd(lp)) {
                        MarginLayoutParamsCompat.setMarginEnd(lp, bottomMargin);
                        lp.bottomMargin = bottomMargin;
                        // Calls resolveLayoutParams(), necessary for layout direction
                        iconView.setLayoutParams(lp);
                        iconView.requestLayout();
                    }
                } else {
                    if (bottomMargin != lp.bottomMargin) {
                        lp.bottomMargin = bottomMargin;
                        MarginLayoutParamsCompat.setMarginEnd(lp, 0);
                        // Calls resolveLayoutParams(), necessary for layout direction
                        iconView.setLayoutParams(lp);
                        iconView.requestLayout();
                    }
                }
            }

            final CharSequence contentDesc = tab != null ? tab.contentDesc : null;
            // Avoid calling tooltip for L and M devices because long pressing twice may freeze devices.
            if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || VERSION.SDK_INT > VERSION_CODES.M) {
                TooltipCompat.setTooltipText(this, hasText ? text : contentDesc);
            }
        }

        private void tryUpdateBadgeDrawableBounds(@NonNull View anchor) {
            // Check that this view is the badge's current anchor view.
            if (hasBadgeDrawable() && anchor == badgeAnchorView) {
                MyTabUtils.setBadgeDrawableBounds(badgeDrawable, anchor, getCustomParentForBadge(anchor));
            }
        }

        private boolean hasBadgeDrawable() {
            return badgeDrawable != null;
        }

        @Nullable
        private FrameLayout getCustomParentForBadge(@NonNull View anchor) {
            if (anchor != iconView && anchor != textView) {
                return null;
            }
            return null;
        }

        /**
         * Calculates the width of the TabView's content.
         *
         * @return Width of the tab label, if present, or the width of the tab icon, if present. If tabs
         * is in inline mode, returns the sum of both the icon and tab label widths.
         */
        int getContentWidth() {
            boolean initialized = false;
            int left = 0;
            int right = 0;

            for (View view : new View[]{textView, iconView, customView}) {
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
                    right = initialized ? Math.max(right, view.getRight()) : view.getRight();
                    initialized = true;
                }
            }

            return right - left;
        }

        /**
         * Calculates the height of the TabView's content.
         *
         * @return Height of the tab label, if present, or the height of the tab icon, if present. If
         * the tab contains both a label and icon, the combined will be returned.
         */
        int getContentHeight() {
            boolean initialized = false;
            int top = 0;
            int bottom = 0;

            for (View view : new View[]{textView, iconView, customView}) {
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    top = initialized ? Math.min(top, view.getTop()) : view.getTop();
                    bottom = initialized ? Math.max(bottom, view.getBottom()) : view.getBottom();
                    initialized = true;
                }
            }

            return bottom - top;
        }

        @Nullable
        public Tab getTab() {
            return tab;
        }

        /**
         * Approximates a given lines width with the new provided text size.
         */
        private float approximateLineWidth(@NonNull Layout layout, int line, float textSize) {
            return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
        }
    }

    class SlidingTabIndicator extends LinearLayout {
        ValueAnimator indicatorAnimator;

        private int layoutDirection = -1;

        SlidingTabIndicator(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        void setSelectedIndicatorHeight(int height) {
            Rect bounds = tabSelectedIndicator.getBounds();
            tabSelectedIndicator.setBounds(bounds.left, 0, bounds.right, height);
            this.requestLayout();
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

        /**
         * Set the indicator position based on an offset between two adjacent tabs.
         *
         * @param position       Position index of the first tab (with less index) currently being displayed.
         *                       Tab position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from the tab at position.
         */
        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                indicatorAnimator.cancel();
            }

            // The title view refers to the one indicated when offset is 0.
            final View firstTitle = getChildAt(position);
            // The title view refers to the one indicated when offset is 1.
            final View nextTitle = getChildAt(position + 1);

            tweenIndicatorPosition(firstTitle, nextTitle, positionOffset);
        }

        @Override
        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);

            // Workaround for a bug before Android M where LinearLayout did not re-layout itself when
            // layout direction changed
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (this.layoutDirection != layoutDirection) {
                    requestLayout();
                    this.layoutDirection = layoutDirection;
                }
            }
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
                // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return;
            }
            //一个的时候特殊处理
            if (getChildCount() == 1) {
                setGravity(tabOneCountGravity);
            }
            // GRAVITY_CENTER will make all tabs the same width as the largest tab, and center them in the
            // SlidingTabIndicator's width (with a "gutter" of padding on either side). If the Tabs do not
            // fit in the SlidingTabIndicator, then fall back to GRAVITY_FILL behavior.
            if ((tabGravity == GRAVITY_CENTER) || mode == MODE_AUTO) {
                final int count = getChildCount();

                // First we'll find the widest tab
                int allTabWidth = 0;
                for (int i = 0, z = count; i < z; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == VISIBLE) {
                        allTabWidth += child.getMeasuredWidth();
                    }
                }

                if (allTabWidth <= 0) {
                    // If we don't have a largest child yet, skip until the next measure pass
                    return;
                }
                //加上分割线
                if (getDividerDrawable() != null) {
                    Drawable dividerDrawable = getDividerDrawable();
                    allTabWidth += dividerDrawable.getIntrinsicWidth() * (count - 1);
                }

//                final int gutter = (int) ViewUtils.dpToPx(getContext(), FIXED_WRAP_GUTTER_MIN);
                boolean remeasure = false;
                if (allTabWidth < getValidWidth()) {
                    if (getChildCount() != 1) {
                        setGravity(Gravity.CENTER);
                    }
                    for (int i = 0; i < count; i++) {
                        final LayoutParams lp =
                                (LayoutParams) getChildAt(i).getLayoutParams();

                        if (lp.width != 0 || lp.weight != 1) {
                            lp.width = 0;
                            lp.weight = 1;
                            remeasure = true;
                        }
                    }
                } else if (allTabWidth > getValidWidth()) {
                    setGravity(Gravity.START);
                    for (int i = 0; i < count; i++) {
                        final LayoutParams lp =
                                (LayoutParams) getChildAt(i).getLayoutParams();
                        if (lp.width != LayoutParams.WRAP_CONTENT || lp.weight != 0) {
                            lp.width = LayoutParams.WRAP_CONTENT;
                            lp.weight = 0;
                            remeasure = true;
                        }
                    }
                }


                if (remeasure) {
                    // Now re-measure after our changes
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                // It's possible that the tabs' layout is modified while the indicator is animating (ex. a
                // new tab is added, or a tab is removed in onTabSelected). This would change the target end
                // position of the indicator, since the tab widths are different. We need to modify the
                // animation's updateListener to pick up the new target positions.
                updateOrRecreateIndicatorAnimation(
                        /* recreateAnimation= */ false, getSelectedTabPosition(), /* duration= */ -1);
            } else {
                // If we've been laid out, update the indicator position
                jumpIndicatorToSelectedPosition();
            }
        }

        /**
         * Immediately update the indicator position to the currently selected position.
         */
        private void jumpIndicatorToSelectedPosition() {
            final View currentView = getChildAt(getSelectedTabPosition());
            tabIndicatorInterpolator.setIndicatorBoundsForTab(
                    XTabLayout.this, currentView, tabSelectedIndicator);
        }

        /**
         * Update the position of the indicator by tweening between the currently selected tab and the
         * destination tab.
         *
         * <p>This method is called for each frame when either animating the indicator between
         * destinations or driving an animation through gesture, such as with a viewpager.
         *
         * @param startTitle The tab which should be selected (as marked by the indicator), when
         *                   fraction is 0.0.
         * @param endTitle   The tab which should be selected (as marked by the indicator), when fraction
         *                   is 1.0.
         * @param fraction   A value between 0.0 and 1.0 that indicates how far between currentTitle and
         *                   endTitle the indicator should be drawn. e.g. If a viewpager attached to this XTabLayout is
         *                   currently half way slid between page 0 and page 1, fraction will be 0.5.
         */
        private void tweenIndicatorPosition(View startTitle, View endTitle, float fraction) {
            boolean hasVisibleTitle = startTitle != null && startTitle.getWidth() > 0;
            if (hasVisibleTitle) {
                tabIndicatorInterpolator.updateIndicatorForOffset(
                        XTabLayout.this, startTitle, endTitle, fraction, tabSelectedIndicator);
            } else {
                // Hide the indicator by setting the drawable's width to 0 and off screen.
                tabSelectedIndicator.setBounds(
                        -1, tabSelectedIndicator.getBounds().top, -1, tabSelectedIndicator.getBounds().bottom);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        }

        /**
         * Animate the position of the indicator from its current position to a new position.
         *
         * <p>This is typically used when a tab destination is tapped. If the indicator should be moved
         * as a result of a gesture, see {@link #setIndicatorPositionFromTabPosition(int, float)}.
         *
         * @param position The new position to animate the indicator to.
         * @param duration The duration over which the animation should take place.
         */
        void animateIndicatorToPosition(final int position, int duration) {
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                indicatorAnimator.cancel();
            }

            updateOrRecreateIndicatorAnimation(/* recreateAnimation= */ true, position, duration);
        }

        /**
         * Animate the position of the indicator from its current position to a new position.
         *
         * @param recreateAnimation Whether a currently running animator should be re-targeted to move
         *                          the indicator to it's new position.
         * @param position          The new position to animate the indicator to.
         * @param duration          The duration over which the animation should take place.
         */

        private void updateOrRecreateIndicatorAnimation(
                boolean recreateAnimation, final int position, int duration) {
            final View currentView = getChildAt(getSelectedTabPosition());
            final View targetView = getChildAt(position);
            if (targetView == null) {
                // If we don't have a view, just update the position now and return
                jumpIndicatorToSelectedPosition();
                return;
            }

            // Create the update listener with the new target indicator positions. If we're not recreating
            // then animationStartLeft/Right will be the same as when the previous animator was created.
            ValueAnimator.AnimatorUpdateListener updateListener =
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                            tweenIndicatorPosition(currentView, targetView, valueAnimator.getAnimatedFraction());
                        }
                    };

            if (recreateAnimation) {
                // Create & start a new indicatorAnimator.
                ValueAnimator animator = indicatorAnimator = new ValueAnimator();
                animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration(duration);
                animator.setFloatValues(0F, 1F);
                animator.addUpdateListener(updateListener);
                animator.start();
            } else {
                // Reuse the existing animator. Updating the listener only modifies the target positions.
                indicatorAnimator.removeAllUpdateListeners();
                indicatorAnimator.addUpdateListener(updateListener);
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int indicatorHeight = tabSelectedIndicator.getBounds().height();
            if (indicatorHeight < 0) {
                indicatorHeight = tabSelectedIndicator.getIntrinsicHeight();
            }

            int indicatorTop = 0;
            int indicatorBottom = 0;

            switch (tabIndicatorGravity) {
                case INDICATOR_GRAVITY_BOTTOM:
                    indicatorTop = getHeight() - indicatorHeight;
                    indicatorBottom = getHeight();
                    break;
                case INDICATOR_GRAVITY_CENTER:
                    indicatorTop = (getHeight() - indicatorHeight) / 2;
                    indicatorBottom = (getHeight() + indicatorHeight) / 2;
                    break;
                case INDICATOR_GRAVITY_TOP:
                    indicatorTop = 0;
                    indicatorBottom = indicatorHeight;
                    break;
                case INDICATOR_GRAVITY_STRETCH:
                    indicatorTop = 0;
                    indicatorBottom = getHeight();
                    break;
                default:
                    break;
            }

            // Ensure the drawable actually has a width and is worth drawing
            if (tabSelectedIndicator.getBounds().width() > 0) {
                // Use the left and right bounds of the drawable, as set by the indicator interpolator.
                // Update the top and bottom to respect the indicator gravity property.
                Rect indicatorBounds = tabSelectedIndicator.getBounds();
                tabSelectedIndicator.setBounds(
                        indicatorBounds.left, indicatorTop, indicatorBounds.right, indicatorBottom);
                tabSelectedIndicator.draw(canvas);
            }

            // Draw the tab item contents (icon and label) on top of the background + indicator layers
            super.draw(canvas);
        }
    }

    @NonNull
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

    @Dimension(unit = Dimension.DP)
    private int getDefaultHeight() {
        boolean hasIconAndText = false;
        for (int i = 0, count = tabs.size(); i < count; i++) {
            Tab tab = tabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                hasIconAndText = true;
                break;
            }
        }
        return (hasIconAndText && !inlineLabel) ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
    }

    private int getTabMinWidth() {
        if (pagerAdapter != null && tabDisplayNum != 0) {
            WindowManager wm = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            if (pagerAdapter.getCount() == 1 || tabDisplayNum == 1) {
                return wm.getDefaultDisplay().getWidth();
            } else if (pagerAdapter.getCount() < tabDisplayNum) {
                return wm.getDefaultDisplay().getWidth() / pagerAdapter.getCount();
            } else {
                return wm.getDefaultDisplay().getWidth() / tabDisplayNum;
            }
        }
        if (tabDisplayNum != 0) {
            WindowManager wm = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            return wm.getDefaultDisplay().getWidth() / tabDisplayNum;
        }
        if (requestedTabMinWidth != INVALID_WIDTH) {
            //默认再加上一点边距
            return requestedTabMinWidth;
        }
        // Else, we'll use the default value
        return (mode == MODE_SCROLLABLE || mode == MODE_AUTO) ? scrollableTabMinWidth : 0;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        // We don't care about the layout params of any views added to us, since we don't actually
        // add them. The only view we add is the SlidingTabStrip, which is done manually.
        // We return the default layout params so that we don't blow up if we're given a TabItem
        // without android:layout_* values.
        return generateDefaultLayoutParams();
    }

    int getTabMaxWidth() {
        return tabMaxWidth;
    }

    public int getTabDisplayNum() {
        return tabDisplayNum;
    }

    public void setTabDisplayNum(int tabDisplayNum) {
        this.tabDisplayNum = tabDisplayNum;
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
     * 一条数据时候的对齐方式
     */
    public void setTabGravityOneCount(@XTabLayout.TabGravity int gravity) {
        if (tabOneCountGravity != gravity) {
            tabOneCountGravity = gravity;
            applyModeAndGravity();
        }
    }

    public int getTabOneCountGravity() {
        return tabOneCountGravity;
    }

    /**
     * 设置Item背景
     */
    public void setTabBackground(Drawable tabBackground) {
        this.tabBackground = tabBackground;
        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View tabView = slidingTabIndicator.getChildAt(i);
            if (tabView instanceof TabView) {
                ((TabView) tabView).setBaseBackgroundDrawable(tabBackground);
            }
        }
    }

    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the necessary calls back to the
     * provided {@link XTabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided XTabLayout weakly, meaning that you can use {@link
     * ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and not cause a
     * leak.
     */
    public static class XTabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @NonNull
        private final WeakReference<XTabLayout> tabLayoutRef;
        private int previousScrollState;
        private int scrollState;

        public XTabLayoutOnPageChangeListener(XTabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }

        @Override
        public void onPageScrolled(
                final int position, final float positionOffset, final int positionOffsetPixels) {
            final XTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                final boolean updateText =
                        scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                final boolean updateIndicator =
                        !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final XTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                final boolean updateIndicator =
                        scrollState == SCROLL_STATE_IDLE
                                || (scrollState == SCROLL_STATE_SETTLING
                                && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
            }
        }

        void reset() {
            previousScrollState = scrollState = SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link XTabLayout.OnTabSelectedListener} 类，该类包含对
     * 提供了｛@link ViewPager｝，以便选项卡位置保持同步。
     */
    public static class ViewPagerOnTabSelectedListener implements XTabLayout.OnTabSelectedListener {
        private final ViewPager viewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.viewPager = viewPager;
        }


        @Override
        public void onTabSelected(@NonNull XTabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(XTabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(XTabLayout.Tab tab) {
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

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
        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(
                @NonNull ViewPager viewPager,
                @Nullable PagerAdapter oldAdapter,
                @Nullable PagerAdapter newAdapter) {
            if (XTabLayout.this.viewPager == viewPager) {
                setPagerAdapter(newAdapter, autoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
        }
    }
}
