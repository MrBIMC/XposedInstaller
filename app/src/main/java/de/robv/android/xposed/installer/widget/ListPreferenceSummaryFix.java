package de.robv.android.xposed.installer.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ListPreferenceSummaryFix extends com.kabouzeid.appthemehelper.common.prefs.ATEListPreference {
    public ListPreferenceSummaryFix(Context context) {
        super(context);
    }

    public ListPreferenceSummaryFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        notifyChanged();
    }
}