package com.jw.media.jvideoplayer.player.base.render;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Created by Joyce.wang on 2024/3/11 19:59
 *
 * @Description simplifying view operation tool class
 */
public class ViewUtils {
    private Context context;
    private View rootView;
    private View findView;

    /**
     * Constructor for use with a specific root view.
     *
     * @param context The context.
     * @param rootView The root view to search within.
     */
    public ViewUtils(@NonNull Context context, @NonNull View rootView) {
        this.context = context;
        this.rootView = rootView;
    }

    /**
     * Constructor for use within an Activity.
     *
     * @param activity The current Activity.
     */public ViewUtils(@NonNull Activity activity) {
        this.context = activity;
        this.rootView = activity.getWindow().getDecorView();
    }

    /**
     * Finds a view by its ID within the current scope.
     *
     * @param id The ID of the view to find.
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils id(int id) {
        if (rootView != null) {
            findView = rootView.findViewById(id);
        }
        return this;
    }

    /**
     * Sets the image resource of an ImageView.
     *
     * @param resId The resource ID of the image.
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils image(int resId) {
        if (findView instanceof ImageView) {
            ((ImageView) findView).setImageResource(resId);
        }
        return this;
    }

    /**
     * Sets the visibility of the view to VISIBLE.
     *
     * @return This ViewUtils instance for chaining.*/
    public ViewUtils visible() {
        if (findView != null) {
            findView.setVisibility(View.VISIBLE);
        }
        return this;
    }

    /**
     * Sets the visibility of the view to GONE.
     *
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils gone() {
        if (findView != null) {
            findView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Sets the visibility of the view to INVISIBLE.
     *
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils invisible() {
        if (findView != null) {
            findView.setVisibility(View.INVISIBLE);
        }
        return this;
    }

    /**
     * Sets an OnClickListener for the view.
     *
     * @param handler The OnClickListener to set.
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils clicked(View.OnClickListener handler) {
        if (findView != null) {
            findView.setOnClickListener(handler);
        }
        return this;
    }

    /**
     * Sets the text of a TextView.
     *
     * @param text The text to set.
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils text(CharSequence text) {
        if (findView != null && findView instanceof TextView) {
            ((TextView) findView).setText(text);
        }
        return this;
    }

    /**
     * Sets the visibility of the view.
     *
     * @param visibility The visibility value (e.g., View.VISIBLE).
     * @return This ViewUtils instance for chaining.
     */
    public ViewUtils visibility(int visibility) {
        if (findView != null) {
            findView.setVisibility(visibility);
        }
        return this;
    }

    /**
     * Sets the width or height of the view.
     *
     * @param isWidth  True to set width, false to set height.
     * @param width   The width value.
     * @param isDip True if the size is in dips, false if in pixels.
     */
    private void size(boolean isWidth, int width, boolean isDip) {
        if (findView != null) {
            ViewGroup.LayoutParams lp = findView.getLayoutParams();
            if (width > 0 && isDip) {
                width = dip2pixel(context, width);
            }
            if (isWidth) {
                lp.width = width;
            } else {
                lp.height = width;
            }
            findView.setLayoutParams(lp);
        }
    }

    /**
     * Sets the width of the view.
     *
     * @param width  The width value.
     * @param isDip True if the width is in dips, false if in pixels.
     */
    public void width(int width, boolean isDip) {
        size(true, width, isDip);
    }

    /**
     * Sets the height of the view.
     *
     * @param height The height value.
     * @param isDip True if the height is in dips, false if in pixels.
     */
    public void height(int height, boolean isDip) {
        size(false, height, isDip);
    }

    /**
     * Converts dips to pixels.
     *
     * @param context The context.
     * @param dips    The value in dips.
     * @return The value in pixels.
     */
    public int dip2pixel(Context context, float dips) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics());
        return value;
    }

    /**
     * Converts pixels to dips.
     *
     * @param context The context.
     * @param pixels The value in pixels.
     * @return The value in dips.
     */
    public float pixel2dip(Context context, float pixels) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = pixels / (metrics.densityDpi / 160f);
        return dp;
    }
}
