package com.aven.floadingview;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 **/
public class RomUtils {

    public enum RomType {
        UNKNOWTYPE, XIAOMI, MEIZU
    }

    public enum MiuiType {
        UNKNOW, V5,V6,V7,V8
    }

    public static RomType getRomType(){

        if (Build.MANUFACTURER.toLowerCase().contains("xiaomi")) {
            return RomType.XIAOMI;
        } else if(Build.MANUFACTURER.toLowerCase().contains("meizu")) {
            return RomType.MEIZU;
        }
        return RomType.UNKNOWTYPE;
    }

    public static String getMIUISystemProperty() {
        String line = null;
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name" );
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = reader.readLine();
            reader.close();
            return line;
        } catch (IOException e) {
        } finally {
        }
        return "UNKNOWN";
    }

    public static MiuiType getMiuiType() {
        String miuiVersion = getMIUISystemProperty();
        if (miuiVersion.toLowerCase().contains("v5")) {
            return MiuiType.V5;
        } else if (miuiVersion.toLowerCase().contains("v6")) {
            return MiuiType.V6;
        } if (miuiVersion.toLowerCase().contains("v7")) {
            return MiuiType.V7;
        } if (miuiVersion.toLowerCase().contains("v8")) {
            return MiuiType.V8;
        }
        return MiuiType.UNKNOW;
    }

    /**
     * 判断MIUI的悬浮窗权限
     * @param context
     * @return
     */
    public static boolean isMiuiFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            return getAppOps(context);
        } else {
            if ((context.getApplicationInfo().flags & 0x8000000) == 1 << 27) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService(context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

}
