package com.aven.floadingview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/1.
 */
public class PackageUtils {

    public static List<String> getLaunchers(Context context) {
        List<String> packageNames = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                packageNames.add(resolveInfo.activityInfo.processName);
                packageNames.add(resolveInfo.activityInfo.packageName);
            }
        }
        return packageNames;
    }

    static String lastResult = null;

    public static boolean isLauncherForeground(Context context) {
        UsageStatsManager sUsageStatsManager;
        List<String> lanuchers = getLaunchers(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                String packageName = appTasks.get(0).topActivity.getPackageName();
                if (lanuchers.contains(packageName)) {
                    return true;
                }
            }
        } else {
            if (RomUtils.getRomType() == RomUtils.RomType.MEIZU) {
                //魅族 -- 做特殊处理
                ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
                if (null != processInfos && !processInfos.isEmpty() && processInfos.size() > 1) {
                    String packageName = processInfos.get(0).processName;
                    Log.e("hyf", "packageName = " + packageName);
                    if (lanuchers.contains(packageName)) {
                        return true;
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            sUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            String result = "";
            List<UsageStats> queryUsageStats = sUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,endTime - 2000, endTime);
            UsageStats recentStats = null;
            for (UsageStats usageStats : queryUsageStats) {
                if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    recentStats = usageStats;
                }
            }
            if (recentStats != null) {
                result = recentStats.getPackageName();
            }
            if (!TextUtils.isEmpty(result) && !TextUtils.equals(lastResult, result)) {
                lastResult = result;
            }
            if (!android.text.TextUtils.isEmpty(lastResult)) {
                if (lanuchers.contains(lastResult)) {
                    return true;
                }
            }

        }
        return false;
    }


    public static boolean isNoOptions(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isNoSwitch(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, System.currentTimeMillis() );
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return false;
        }
        return true;
    }


    /**
     * 打开MIUI权限管理界面(MIUI v5, v6)
     * @param context
     */
    public static void openMiuiPermissionActivity(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        RomUtils.MiuiType miuiType = RomUtils.getMiuiType();

        if (miuiType == RomUtils.MiuiType.V5) {
            openAppDetailActivity(context, context.getPackageName());
            return;
        } else if (miuiType == RomUtils.MiuiType.V6) {
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
        }

        if (isIntentAvailable(context, intent)) {
            if (context instanceof MainActivity) {
                MainActivity a = (MainActivity) context;
                a.startActivityForResult(intent, MainActivity.SYSTEM_SHOW_FLOAT_VIEW);
            }
        } else {
        }
    }

    public static void openAppDetailActivity(Context context, String packageName) {
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= 9) {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
        }
        if (isIntentAvailable(context, intent)) {
            if (context instanceof MainActivity) {
                MainActivity a = (MainActivity) context;
                a.startActivityForResult(intent, MainActivity.SYSTEM_SHOW_FLOAT_VIEW);
            }
        } else {
        }
    }

    /**
     * 判断是否有可以接受的Activity
     * @param context
     * @return
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        if (intent == null) return false;
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_ACTIVITIES).size() > 0;
    }

}
