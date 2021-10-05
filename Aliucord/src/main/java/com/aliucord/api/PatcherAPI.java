/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.api;

import androidx.annotation.NonNull;

import com.aliucord.patcher.*;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;


@SuppressWarnings({"unused"})
public class PatcherAPI {
    public List<Runnable> unpatches = new ArrayList<>();

    private Runnable createUnpatch(XC_MethodHook.Unhook unhook) {
        Runnable unpatch = new Runnable() {
            public void run() {
                unhook.unhook();
                unpatches.remove(this);
            }
        };
        unpatches.add(unpatch);
        return unpatch;
    }

    /**
     * Patches a method.
     * @param forClass Class to patch.
     * @param fn Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook Callback for the patch.
     * @return A {@link Runnable} object.
     * @see Patch
     * @see PrePatch
     */
    public Runnable patch(@NonNull String forClass, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(forClass, fn, paramTypes, hook));
    }

    /**
     * Patches a method.
     * @param clazz Class to patch.
     * @param fn Method to patch.
     * @param paramTypes Parameters of the <code>fn</code>. Useful for patching individual overloads.
     * @param hook Callback for the patch.
     * @return Method that will remove the patch when invoked
     * @see Patch
     * @see PrePatch
     */
    public Runnable patch(@NonNull Class<?> clazz, @NonNull String fn, @NonNull Class<?>[] paramTypes, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(clazz, fn, paramTypes, hook));
    }

    /**
     * Patches a method or constructor.
     * @param m Method or constructor to patch. see {@link Member}.
     * @param hook Callback for the patch.
     * @return Method that will remove the patch when invoked
     * @see PatcherAPI#patch(String, String, Class[], XC_MethodHook)
     * @see PatcherAPI#patch(Class, String, Class[], XC_MethodHook)
     * @see Patch
     * @see PrePatch
     */
    public Runnable patch(@NonNull Member m, @NonNull XC_MethodHook hook) {
        return createUnpatch(Patcher.addPatch(m, hook));
    }

    /**
     * Removes all patches.
     */
    public void unpatchAll() {
        Object[] runnables = unpatches.toArray();
        for (Object unpatch : runnables) ((Runnable) unpatch).run();
    }
}
