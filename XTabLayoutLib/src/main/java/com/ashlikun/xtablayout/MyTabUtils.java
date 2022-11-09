package com.ashlikun.xtablayout;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.badge.BadgeDrawable;

/**
 * 作者　　: 李坤
 * 创建时间: 2022/11/9　12:53
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
class MyTabUtils {
    public static void setTint(@NonNull Drawable drawable, @ColorInt int color) {
        boolean hasTint = color != Color.TRANSPARENT;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            // On API 21, AppCompat's WrappedDrawableApi21 class only supports tinting certain types of
            // drawables. Replicates the logic here to support all types of drawables.
            if (hasTint) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                drawable.setColorFilter(null);
            }
        } else {
            if (hasTint) {
                DrawableCompat.setTint(drawable, color);
            } else {
                DrawableCompat.setTintList(drawable, null);
            }
        }
    }

    public static void setBadgeDrawableBounds(
            @NonNull BadgeDrawable badgeDrawable,
            @NonNull View anchor,
            @Nullable FrameLayout compatBadgeParent) {
        Rect badgeBounds = new Rect();
        anchor.getDrawingRect(badgeBounds);
        badgeDrawable.setBounds(badgeBounds);
        badgeDrawable.updateBadgeCoordinates(anchor, compatBadgeParent);
    }

    public static void detachBadgeDrawable(
            @Nullable BadgeDrawable badgeDrawable, @NonNull View anchor) {
        if (badgeDrawable == null) {
            return;
        }
        if (badgeDrawable.getCustomBadgeParent() != null) {
            badgeDrawable.getCustomBadgeParent().setForeground(null);
        } else {
            anchor.getOverlay().remove(badgeDrawable);
        }
    }

    public static void attachBadgeDrawable(
            @NonNull BadgeDrawable badgeDrawable,
            @NonNull View anchor,
            @Nullable FrameLayout customBadgeParent) {
        setBadgeDrawableBounds(badgeDrawable, anchor, customBadgeParent);

        if (badgeDrawable.getCustomBadgeParent() != null) {
            badgeDrawable.getCustomBadgeParent().setForeground(badgeDrawable);
        } else {
            anchor.getOverlay().add(badgeDrawable);
        }

    }
}
