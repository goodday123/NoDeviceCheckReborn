package org.nift4.xposed.nodevicecheckreborn;

import java.io.File;

import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if ("android".equals(lpparam.packageName) || "com.google.android.gms".equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod(File.class, "exists",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            File file = (File) param.thisObject;
                            // Disable check for enforced SELinux state
                            if (new File("/sys/fs/selinux/enforce").equals(file)) {
                                param.setResult(true);
                                return;
                            }

                            // Disable check for SU binary files
                            if (new File("/system/bin/su").equals(file) || new File("/system/xbin/su").equals(file)) {
                                param.setResult(false);
                            }
                        }
                    });
        }

        XposedHelpers.findAndHookMethod(JSONObject.class, "getBoolean",
                String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String name = (String) param.args[0];
                        // Modify server response to pass SafetyNet check
                        if ("ctsProfileMatch".equals(name)
                                || "isValidSignature".equals(name)
                                || "basicIntegrity".equals(name)) {
                            param.setResult(true);
                        }
                    }
                });
    }
}

