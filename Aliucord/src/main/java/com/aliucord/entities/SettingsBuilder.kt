package com.aliucord.entities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin.SettingsTab
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.discord.views.CheckedSetting
import com.aliucord.widgets.BottomSheet
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.Divider
import com.lytefast.flexinput.R
import java.util.*

class SettingsBuilder @kotlin.jvm.JvmOverloads constructor(
        private val settings: SettingsAPI,
        private val type: SettingsTab.Type = SettingsTab.Type.PAGE
) {
    interface SettingsItem {
        fun build(ctx: Context, settings: SettingsAPI): View
    }

    private class DividerItem : SettingsItem {
        override fun build(ctx: Context, settings: SettingsAPI) = Divider(ctx)
    }

    private class HeaderItem(private val mHeader: String) : SettingsItem {
        override fun build(ctx: Context, settings: SettingsAPI) =
            TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Header).apply {
                text = mHeader
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold)
            }
        }

    private class SwitchItem(private val mType: CheckedSetting.ViewType, private val mTitle: CharSequence?, private val mSubTitle: CharSequence?, private val mSettingsKey: String, private val mDefaultValue: Boolean) : SettingsItem {
        override fun build(ctx: Context, settings: SettingsAPI): View =
                Utils.createCheckedSetting(ctx, mType, mTitle, mSubTitle).apply {
                    isChecked = settings.getBool(mSettingsKey, mDefaultValue)
                    setOnCheckedListener { checked ->
                        settings.setBool(mSettingsKey, checked)
                    }
                }
    }

    class BuiltPage(
            private val settings: SettingsAPI,
            private val items: ArrayList<SettingsItem>,
            private val actionBarTitle: CharSequence?,
            private val actionBarSubtitle: CharSequence?
        ) : SettingsPage() {

        override fun onViewBound(view: View) {
            super.onViewBound(view)
            setActionBarTitle(actionBarTitle ?: "Settings")
            setActionBarSubtitle(actionBarSubtitle)
            buildPage(linearLayout, items, settings)
        }
    }

    class BuiltSheet(
            private val settings: SettingsAPI,
            private val items: ArrayList<SettingsItem>
    ) : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)
            buildPage(linearLayout, items, settings)
        }
    }

    private val mItems: MutableList<SettingsItem> = ArrayList()
    private var mActionBarTitle: CharSequence? = null
    private var mActionBarSubtitle: CharSequence? = null
    fun setActionBarTitle(title: CharSequence?): SettingsBuilder {
        if (type != SettingsTab.Type.PAGE) throw IllegalStateException(String.format("setActionBarTitle(%s) used in BottomSheet SettingsBuilder", title))
        mActionBarTitle = title
        return this
    }

    fun setActionBarSubtitle(subtitle: CharSequence?): SettingsBuilder {
        if (type != SettingsTab.Type.PAGE) throw IllegalStateException(String.format("setActionBarSubtitle(%s) used in BottomSheet SettingsBuilder", subtitle))
        mActionBarSubtitle = subtitle
        return this
    }

    fun addDivider(): SettingsBuilder {
        mItems.add(DividerItem())
        return this
    }

    fun addHeader(headerText: String): SettingsBuilder {
        mItems.add(HeaderItem(headerText))
        return this
    }

    fun addSwitch(type: CheckedSetting.ViewType, title: CharSequence?, subtitle: CharSequence?, settingsKey: String, defaultValue: Boolean): SettingsBuilder {
        mItems.add(SwitchItem(type, title, subtitle, settingsKey, defaultValue))
        return this
    }

    fun build(): SettingsTab {
        return if (type == SettingsTab.Type.PAGE) {
            SettingsTab(BuiltPage::class.java, type).withArgs(settings, mItems, mActionBarTitle, mActionBarSubtitle)
        } else {
            SettingsTab(BuiltSheet::class.java, type).withArgs(settings, mItems)
        }
    }

    companion object {
        private fun buildPage(layout: LinearLayout, items: List<SettingsItem>, settings: SettingsAPI) =
            items.forEach { i -> layout.addView(i.build(layout.context, settings))}
    }
}