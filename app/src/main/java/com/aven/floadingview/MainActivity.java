package com.aven.floadingview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int SYSTEM_ALERT_WINDOW_CODE = 110;
    public final static int SYSTEM_SHOW_FLOAT_VIEW = 111;
    public static boolean sHasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && !PackageUtils.isNoOptions(this)) {
            sHasPermission = true;
        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (!PackageUtils.isNoSwitch(this) && PackageUtils.isIntentAvailable(this, intent)) {
                startActivity(intent);
            }
        }
        if (RomUtils.getRomType() == RomUtils.RomType.XIAOMI) {
            if (!RomUtils.isMiuiFloatWindowOpAllowed(this) && Build.VERSION.SDK_INT < 19) {
                PackageUtils.openMiuiPermissionActivity(this);
            } else {
                Intent intent = new Intent(this, FloatingViewService.class);
                startService(intent);
            }

        } else
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {
                Intent intent = new Intent(this, FloatingViewService.class);
                startService(intent);
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, SYSTEM_ALERT_WINDOW_CODE);
            } else {
                Intent intent = new Intent(this, FloatingViewService.class);
                startService(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SYSTEM_ALERT_WINDOW_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, FloatingViewService.class);
                startService(intent);
            }
            else {
                Toast.makeText(getApplicationContext(), "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("hyf", "onActivityResult = " + requestCode);
        if (requestCode == SYSTEM_ALERT_WINDOW_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(this, FloatingViewService.class);
                    startService(intent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "没有权限", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SYSTEM_SHOW_FLOAT_VIEW) {
            Intent intent = new Intent(this, FloatingViewService.class);
            startService(intent);
        }
    }
}
