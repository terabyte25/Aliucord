package com.discord.app;

import android.os.Bundle;

import com.aliucord.fragments.ConfirmDialog;

import java.io.File;
import java.io.IOException;

import kotlin.jvm.internal.DefaultConstructorMarker;
import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

/**
 * This is a class by Discord which conveniently happens to be empty
 * Thus it offers an amazing entry point for an injection and is overwritten with Aliucord's Injector
 */
public class App$a {
    private static MethodHook.Unhook unhook;

    static {
        // If we get here, it means that this dex was injected directly using the Installer
        // instead of being loaded properly via the Injector, so display a notification to the user
        PineConfig.debug = false;
        PineConfig.debuggable = false;
        PineConfig.disableHiddenApiPolicy = false;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = false;

        try {
            unhook = Pine.hook(AppActivity.class.getDeclaredMethod("onCreate", Bundle.class), new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame callFrame) {
                    var appActivity = (AppActivity) callFrame.thisObject;
                    new ConfirmDialog()
                            .setTitle("Oops")
                            .setDescription("Aliucord was not properly installed. This probably means your updater is outdated! Please update it and reinstall Aliucord")
                            .showNow(appActivity.getSupportFragmentManager(), "ALIUCORD_INSTALLED_INCORRECTLY");

                    unhook.unhook();
                    unhook = null;
                }
            });
        } catch (Throwable ignored) { }
    }

    /**
     * Method that will replace Aliucord with the latest Dex from github
     * @param outputFile Output file to write to. MUST be new File(context.getCodeCacheDir(), "Aliucord.zip");
     */
    public static void downloadLatestAliucordDex(File outputFile) throws IOException {
        // STUB
    }

    /**
     * Default constructor Discord's class has
     */
    public App$a(DefaultConstructorMarker defaultConstructorMarker) {}
}