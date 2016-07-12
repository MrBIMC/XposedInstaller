package de.robv.android.xposed.installer;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.WindowManager;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;

import de.robv.android.xposed.installer.util.ThemeUtil;

public abstract class XposedBaseActivity extends ATHToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        if (!ThemeStore.isConfigured(this, 0)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.Theme_XposedInstaller_Light)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .autoGeneratePrimaryDark(true)
                    .commit();
        }
        super.onCreate(savedInstanceBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeUtil.colorizeNavigationBar(this);
    }

    public void setFloating(android.support.v7.widget.Toolbar toolbar, @StringRes int details) {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.height = getResources().getDimensionPixelSize(R.dimen.floating_height);
            params.width = getResources().getDimensionPixelSize(R.dimen.floating_width);
            params.alpha = 1.0f;
            params.dimAmount = 0.6f;
            params.flags |= 2;
            getWindow().setAttributes(params);

            if (details != 0) {
                toolbar.setTitle(details);
            }
            toolbar.setNavigationIcon(R.drawable.ic_close);

            setFinishOnTouchOutside(true);
        }
        ThemeUtil.tintIcon(this, toolbar);
    }
}