package com.ashlikun.xtablayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ashlikun.xviewpager2.view.XViewPager;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/5/12　16:41
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：XXTabLayout与XViewPager（v2）一起使用
 */
public class XTabLayoutMediator {
    @NonNull
    private final XTabLayout tabLayout;
    @NonNull
    private final XViewPager viewPager;
    private final boolean autoRefresh;
    private final TabConfigurationStrategy tabConfigurationStrategy;
    @Nullable
    private RecyclerView.Adapter<?> adapter;
    private boolean attached;

    @Nullable
    private XTabLayoutOnPageChangeCallback onPageChangeCallback;
    @Nullable
    private XTabLayout.OnTabSelectedListener onTabSelectedListener;
    @Nullable
    private RecyclerView.AdapterDataObserver pagerAdapterObserver;

    /**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    public interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls {@link
         * TabLayout.Tab#setText(CharSequence)}, but any form of styling can be applied.
         *
         * @param tab      The Tab which should be configured to represent the title of the item at the given
         *                 position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        void onConfigureTab(@NonNull XTabLayout.Tab tab, int position);
    }

    public XTabLayoutMediator(
            @NonNull XTabLayout tabLayout,
            @NonNull XViewPager viewPager,
            TabConfigurationStrategy tabConfigurationStrategy) {
        this(tabLayout, viewPager, true, tabConfigurationStrategy);
    }

    public XTabLayoutMediator(
            @NonNull XTabLayout tabLayout,
            @NonNull XViewPager viewPager,
            boolean autoRefresh,
            TabConfigurationStrategy tabConfigurationStrategy) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
        this.autoRefresh = autoRefresh;
        this.tabConfigurationStrategy = tabConfigurationStrategy;
    }

    /**
     * Link the XTabLayout and the XViewPager together. Must be called after XViewPager has an adapter
     * set. To be called on a new instance of XTabLayoutMediator or if the XViewPager's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the XViewPager has no
     *                               adapter.
     */
    public void attach() {
        if (attached) {
            throw new IllegalStateException("XTabLayoutMediator is already attached");
        }
        adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException(
                    "XTabLayoutMediator attached before XViewPager has an " + "adapter");
        }
        attached = true;

        // Add our custom OnPageChangeCallback to the ViewPager
        onPageChangeCallback = new XTabLayoutOnPageChangeCallback(tabLayout);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        // Now we'll add a tab selected listener to set ViewPager's current item
        onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        if (autoRefresh) {
            // Register our observer on the new adapter
            pagerAdapterObserver = new PagerAdapterObserver();
            adapter.registerAdapterDataObserver(pagerAdapterObserver);
        }

        populateTabsFromPagerAdapter();

        // Now update the scroll position to match the ViewPager's current item
        tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    }

    /**
     * Unlink the XTabLayout and the ViewPager. To be called on a stale XTabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before {@link #attach()} when a XViewPager's adapter is changed.
     */
    public void detach() {
        if (autoRefresh && adapter != null) {
            adapter.unregisterAdapterDataObserver(pagerAdapterObserver);
            pagerAdapterObserver = null;
        }
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        onTabSelectedListener = null;
        onPageChangeCallback = null;
        adapter = null;
        attached = false;
    }

    @SuppressWarnings("WeakerAccess")
    void populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs();

        if (adapter != null) {
            int adapterCount = adapter.getItemCount();
            for (int i = 0; i < adapterCount; i++) {
                XTabLayout.Tab tab = tabLayout.newTab();
                tabConfigurationStrategy.onConfigureTab(tab, i);
                tabLayout.addTab(tab, false);
            }
            // Make sure we reflect the currently set ViewPager item
            if (adapterCount > 0) {
                int lastItem = tabLayout.getTabCount() - 1;
                int currItem = Math.min(viewPager.getCurrentItem(), lastItem);
                if (currItem != tabLayout.getSelectedTabPosition()) {
                    tabLayout.selectTab(tabLayout.getTabAt(currItem));
                }
            }
        }
    }

    /**
     * A {@link ViewPager2.OnPageChangeCallback} class which contains the necessary calls back to the
     * provided {@link XTabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided XTabLayout weakly, meaning that you can use {@link
     * XViewPager#registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback)} without removing the
     * callback and not cause a leak.
     */
    private static class XTabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @NonNull
        private final WeakReference<XTabLayout> tabLayoutRef;
        private int previousScrollState;
        private int scrollState;

        XTabLayoutOnPageChangeCallback(XTabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
            reset();
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            XTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                boolean updateText =
                        scrollState != ViewPager2.SCROLL_STATE_SETTLING || previousScrollState == ViewPager2.SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                boolean updateIndicator =
                        !(scrollState == ViewPager2.SCROLL_STATE_SETTLING && previousScrollState == ViewPager2.SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            XTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                boolean updateIndicator =
                        scrollState == ViewPager2.SCROLL_STATE_IDLE
                                || (scrollState == ViewPager2.SCROLL_STATE_SETTLING
                                && previousScrollState == ViewPager2.SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator, true);
            }
        }

        void reset() {
            previousScrollState = scrollState = ViewPager2.SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link XTabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
     * provided {@link XViewPager} so that the tab position is kept in sync.
     */
    private static class ViewPagerOnTabSelectedListener implements XTabLayout.OnTabSelectedListener {
        private final XViewPager viewPager;

        ViewPagerOnTabSelectedListener(XViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void onTabSelected(@NonNull XTabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition(), true);
        }

        @Override
        public void onTabUnselected(XTabLayout.Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(XTabLayout.Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            populateTabsFromPagerAdapter();
        }
    }
}
