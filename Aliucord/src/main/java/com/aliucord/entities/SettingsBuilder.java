package com.aliucord.entities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsBuilder {
    private interface SettingsItem {
        View build(Context ctx, SettingsAPI settings);
    }

    private static class DividerItem implements SettingsItem {
        @Override
        public View build(Context ctx, SettingsAPI settings) {
            return new Divider(ctx);
        }
    }

    private static class HeaderItem implements SettingsItem {
        private final String mHeader;

        public HeaderItem(String header) {
            mHeader = header;
        }

        @Override
        public View build(Context ctx, SettingsAPI settings) {
            var textView = new TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Header);
            textView.setText(mHeader);
            textView.setTypeface(ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold));
            return textView;
        }
    }

    private static class SwitchItem implements SettingsItem {
        private final CheckedSetting.ViewType mType;
        private final CharSequence mTitle;
        private final CharSequence mSubTitle;
        private final String mSettingsKey;
        private final boolean mDefaultValue;

        public SwitchItem(CheckedSetting.ViewType type, CharSequence title, CharSequence subtitle, String settingsKey, boolean defaultValue) {
            mType = type;
            mTitle = title;
            mSubTitle = subtitle;
            mSettingsKey = settingsKey;
            mDefaultValue = defaultValue;
        }

        @Override
        public View build(Context ctx, SettingsAPI settings) {
            var cs = Utils.createCheckedSetting(ctx, mType, mTitle, mSubTitle);
            cs.setChecked(settings.getBool(mSettingsKey, mDefaultValue));
            cs.setOnCheckedListener(checked -> settings.setBool(mSettingsKey, checked));
            return cs;
        }
    }

    private static void buildPage(LinearLayout layout, List<SettingsItem> items, SettingsAPI settings) {
        var ctx = layout.getContext();
        for (var item : items)
            layout.addView(item.build(ctx, settings));
    }

    public static class BuiltPage extends SettingsPage {
        private final SettingsAPI mSettings;
        private final List<SettingsItem> mItems;
        private final CharSequence mActionBarTitle;
        private final CharSequence mActionBarSubtitle;

        public BuiltPage(SettingsAPI settings, ArrayList<SettingsItem> items, CharSequence actionBarTitle, CharSequence actionBarSubtitle) {
            mSettings = settings;
            mItems = items;
            mActionBarTitle = actionBarTitle;
            mActionBarSubtitle = actionBarSubtitle;
        }

        @Override
        public void onViewBound(android.view.View view) {
            super.onViewBound(view);

            setActionBarTitle(mActionBarTitle != null ? mActionBarTitle : "Settings");
            setActionBarSubtitle(mActionBarSubtitle);

            buildPage(getLinearLayout(), mItems, mSettings);
        }
    }

    public static class BuiltSheet extends BottomSheet {
        private final SettingsAPI mSettings;
        private final List<SettingsItem> mItems;

        public BuiltSheet(SettingsAPI settings, ArrayList<SettingsItem> items) {
            mSettings = settings;
            mItems = items;
        }

        @Override
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);

            buildPage(getLinearLayout(), mItems, mSettings);
        }
    }

    private final SettingsAPI mSettings;
    private final Plugin.SettingsTab.Type mType;
    private final List<SettingsItem> mItems = new ArrayList<>();
    private CharSequence mActionBarTitle;
    private CharSequence mActionBarSubtitle;

    public SettingsBuilder(SettingsAPI settings) {
        this(settings, Plugin.SettingsTab.Type.PAGE);
    }

    public SettingsBuilder(SettingsAPI settings, Plugin.SettingsTab.Type type) {
        mSettings = settings;
        mType = type;
    }

    public SettingsBuilder setActionBarTitle(CharSequence title) {
        if (mType != Plugin.SettingsTab.Type.PAGE)
            throw new IllegalStateException(String.format("setActionBarTitle(%s) used in BottomSheet SettingsBuilder", title));
        mActionBarTitle = title;
        return this;
    }

    public SettingsBuilder setActionBarSubtitle(CharSequence subtitle) {
        if (mType != Plugin.SettingsTab.Type.PAGE)
            throw new IllegalStateException(String.format("setActionBarSubtitle(%s) used in BottomSheet SettingsBuilder", subtitle));
        mActionBarSubtitle = subtitle;
        return this;
    }

    public SettingsBuilder addDivider() {
        mItems.add(new DividerItem());
        return this;
    }

    public SettingsBuilder addHeader(@NonNull String headerText) {
        mItems.add(new HeaderItem(headerText));
        return this;
    }

    public SettingsBuilder addSwitch(CheckedSetting.ViewType type, CharSequence title, CharSequence subtitle, String settingsKey, boolean defaultValue) {
        mItems.add(new SwitchItem(type, title, subtitle, settingsKey, defaultValue));
        return this;
    }

    public Plugin.SettingsTab build() {
        if (mType == Plugin.SettingsTab.Type.PAGE) {
            return new Plugin.SettingsTab(BuiltPage.class, mType).withArgs(mSettings, mItems, mActionBarTitle, mActionBarSubtitle);
        } else {
            return new Plugin.SettingsTab(BuiltSheet.class, mType).withArgs(mSettings, mItems);
        }
    }
}
