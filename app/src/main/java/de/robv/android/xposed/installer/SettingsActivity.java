package de.robv.android.xposed.installer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.ATECheckBoxPreference;
import com.kabouzeid.appthemehelper.common.prefs.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.ATEListPreference;
import com.kabouzeid.appthemehelper.common.prefs.ATESwitchPreference;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.installer.util.RepoLoader;
import de.robv.android.xposed.installer.util.ThemeUtil;
import de.robv.android.xposed.installer.util.UpdateService;

public class SettingsActivity extends XposedBaseActivity implements ColorChooserDialog.ColorCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ATH.setActivityToolbarColorAuto(this, getATHToolbar());
        ATH.setStatusbarColorAuto(this);
        ATH.setTaskDescriptionColorAuto(this);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_item_settings);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, 0);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment()).commit();
        }

    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final ThemeStore themeStore = ThemeStore.editTheme(this);
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                themeStore.primaryColor(selectedColor);
                break;
            case R.string.accent_color:
                themeStore.accentColor(selectedColor);
                break;
        }
        themeStore.commit();
        recreate();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static final File mDisableResourcesFlag = new File(XposedApp.BASE_DIR + "conf/disable_resources");
        private PackageManager pm;
        private String packName;
        private Context mContext;

        private Preference.OnPreferenceChangeListener iconChange = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {

                String act = ".WelcomeActivity-";
                String[] iconsValues = new String[]{"dvdandroid", "hjmodi", "rovo", "rovo-old", "staol"};

                for (String s : iconsValues) {
                    pm.setComponentEnabledSetting(new ComponentName(mContext, packName + act + s), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }

                act += iconsValues[Integer.parseInt((String) newValue)];

                int drawable = XposedApp.iconsValues[Integer
                        .parseInt((String) newValue)];

                if (Build.VERSION.SDK_INT >= 21) {

                    ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(getString(R.string.app_name),
                            XposedApp.drawableToBitmap(mContext.getDrawable(drawable)),
                            ThemeStore.primaryColor(mContext));
                    getActivity().setTaskDescription(tDesc);
                }

                pm.setComponentEnabledSetting(new ComponentName(mContext, packName + act), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                return true;
            }
        };

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            ATESwitchPreference navBarPref = (ATESwitchPreference) findPreference("nav_bar");
            if (Build.VERSION.SDK_INT < 21) {
                Preference heads_up = findPreference("heads_up");

                heads_up.setEnabled(false);
                heads_up.setSummary(heads_up.getSummary() + " LOLLIPOP+");
                navBarPref.setSummary(navBarPref.getSummary() + " LOLLIPOP+");
            }

            mContext = getActivity();

            findPreference("release_type_global").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    RepoLoader.getInstance().setReleaseTypeGlobal((String) newValue);
                    return true;
                }
            });

            ATECheckBoxPreference prefDisableResources = (ATECheckBoxPreference) findPreference("disable_resources");
            prefDisableResources.setChecked(mDisableResourcesFlag.exists());
            prefDisableResources.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (Boolean) newValue;
                    if (enabled) {
                        try {
                            mDisableResourcesFlag.createNewFile();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDisableResourcesFlag.delete();
                    }
                    return (enabled == mDisableResourcesFlag.exists());
                }
            });

            ATEListPreference customIcon = (ATEListPreference) findPreference("custom_icon");

            pm = mContext.getPackageManager();
            packName = mContext.getPackageName();

            customIcon.setOnPreferenceChangeListener(iconChange);

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setColor(ThemeStore.primaryColor(getActivity()), Color.BLACK);
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                            .preselect(ThemeStore.primaryColor(getActivity()))
                            .allowUserColorInputAlpha(false)
                            .dynamicButtonColor(false)
                            .show();
                    return true;
                }
            });

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            accentColorPref.setColor(ThemeStore.accentColor(getActivity()), Color.BLACK);
            accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                            .preselect(ThemeStore.accentColor(getActivity()))
                            .dynamicButtonColor(false)
                            .accentMode(true)
                            .show();
                    return true;
                }
            });

            findPreference("theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int theme = 0;
                    switch ((String) newValue) {
                        case "0":
                            theme = R.style.Theme_XposedInstaller_Light;
                            break;
                        case "1":
                            theme = R.style.Theme_XposedInstaller_Dark;
                            break;
                        case "2":
                            theme = R.style.Theme_XposedInstaller_Dark_Black;
                            break;
                    }

                    ThemeStore.editTheme(getActivity())
                            .activityTheme(theme)
                            .commit();
                    getActivity().recreate();
                    return true;
                }
            });

            navBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ThemeUtil.colorizeNavigationBar(getActivity());
                    getActivity().recreate();
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("theme"))
                getActivity().recreate();

            if (key.equals("update_service_interval")) {
                final Intent intent = new Intent(getActivity(), UpdateService.class);
                getActivity().stopService(intent);
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().startService(intent);
                    }
                }, 1000);

            }
        }

    }
}