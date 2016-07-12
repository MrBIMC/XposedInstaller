package de.robv.android.xposed.installer.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;

import java.lang.reflect.Field;

import de.robv.android.xposed.installer.XposedApp;

public final class ThemeUtil {

	public static int getThemeColor(Context context, int id) {
		Theme theme = context.getTheme();
		TypedArray a = theme.obtainStyledAttributes(new int[] { id });
		int result = a.getColor(0, 0);
		a.recycle();
		return result;
	}

    public static void colorizeNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(XposedApp.getPreferences().getBoolean("nav_bar", false) ? ThemeStore.primaryColor(activity) : Color.BLACK);
        }
    }

    public static void colorizeMenu(Activity activity, Menu menu, int... ids) {
        for (int id : ids) {
            MenuItem item = menu.findItem(id);
            if (item == null) continue;
            Drawable d = item.getIcon();
            if (d == null) continue;
            tintIcon(activity, d);
        }
    }

    public static void tintIcon(Activity activity, Toolbar toolbar) {
        if (toolbar.getNavigationIcon() != null) tintIcon(activity, toolbar.getNavigationIcon());
        if (toolbar.getOverflowIcon() != null) tintIcon(activity, toolbar.getOverflowIcon());
    }

    public static void tintIcon(Activity activity, SearchView searchView) {
        try {
            Class cls = searchView.getClass();
            Field f = cls.getDeclaredField("mSearchHintIcon");
            f.setAccessible(true);

            Drawable mSearchHintIcon = (Drawable) f.get(searchView);
            if (mSearchHintIcon != null) {
                tintIcon(activity, mSearchHintIcon);
            }
            int color = ColorUtil.isColorLight(ThemeStore.primaryColor(activity)) ? MaterialValueHelper.getPrimaryTextColor(activity, true) : MaterialValueHelper.getPrimaryTextColor(activity, false);

            ToolbarContentTintHelper.InternalToolbarContentTintUtil.SearchViewTintUtil.setSearchViewContentColor(searchView, color);
        } catch (Exception ignored) {
        }
    }

    public static Drawable tintIcon(Activity activity, @DrawableRes int drawable) {
        int color = MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(ThemeStore.primaryColor(activity)));
        Drawable icon = activity.getResources().getDrawable(drawable);

        assert icon != null;
        icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        return icon;
    }

    public static void tintIcon(Activity activity, Drawable icon) {
        if (ColorUtil.isColorLight(ThemeStore.primaryColor(activity))) {
            int color = MaterialValueHelper.getPrimaryTextColor(activity, true);

            icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            ATH.setLightStatusbar(activity, true);
        }
    }
}
