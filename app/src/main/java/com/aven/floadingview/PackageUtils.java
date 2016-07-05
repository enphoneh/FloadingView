package com.aven.floadingview;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

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
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<String> lanuchers = getLaunchers(context);
//        List<ActivityManager.RunningTaskInfo> runningTaskInfos =  activityManager.getRunningTasks(1);
//
//        if(lanuchers.contains(runningTaskInfos.get(0).baseActivity.getPackageName())) {
//            isLauncherForeground = true;
//        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                String packageName = appTasks.get(0).topActivity.getPackageName();
                if (lanuchers.contains(packageName)) {
                    return true;
                }
//            isLauncherForeground = true;
            }
        } else {
            long endTime = System.currentTimeMillis();
            sUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            String result = "";
//            UsageEvents.Event event = new UsageEvents.Event();
//            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);
            List<UsageStats> queryUsageStats = sUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,endTime - 2000, endTime);
//            while (usageEvents.hasNextEvent()) {
//                usageEvents.getNextEvent(event);
//                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
//                    result = event.getPackageName();
//                }
//            }
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


}
