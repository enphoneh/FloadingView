package com.aven.floadingview;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/6/27.
 */
public class FloatingViewService extends Service {

    public static final int PARAMS_UASAGE_PERMISSION = 111;

    private final int MOVE_GAP = 10;
    WindowManager.LayoutParams mLayoutParams;
    Button mFloatView;
    WindowManager mWindowManager;
    LinearLayout mFloatLayout;
    float mOriX = 0;
    float mOriY = 0;
    private Handler mHandler;
    private boolean flag = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        genFloatingView();
    }

    private void genFloatingView() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        //设置window type
        if (Build.VERSION.SDK_INT <= 18) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
//        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //设置图片格式，效果为背景透明
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        mLayoutParams.x = 200;
        mLayoutParams.y = 200;

        //设置悬浮窗口长宽数据
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.floating_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, mLayoutParams);
        //浮动窗口按钮
        mFloatView = (Button) mFloatLayout.findViewById(R.id.float_id);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mOriX = event.getRawX();
                        mOriY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float slide_x = event.getRawX() - mOriX;
                        float slide_y = event.getRawY() - mOriY;
                        if (Math.abs(slide_x) > MOVE_GAP || Math.abs(slide_y) > MOVE_GAP) {
                            //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            mLayoutParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth() / 2;
                            //减25为状态栏的高度
                            mLayoutParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2 - getStatusBarHeight();
                            //刷新
                            mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
                        }
                        break;

                }

                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatingViewService.this, "onClick", Toast.LENGTH_SHORT).show();
            }
        });
//        if (Build.VERSION.SDK_INT < 19) {
//
//        } else {
//            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//            startActivity(intent);
//        }
            checkIsHome();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void checkIsHome() {
        new Thread()
        {
            public void run()
            {
                while (flag)
                {
                    try
                    {
                        final boolean isHome = PackageUtils.isLauncherForeground(FloatingViewService.this);
                        Log.e("hyf", "isHome = " + isHome);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFloatLayout.setVisibility(isHome ? View.VISIBLE : View.GONE);
                                }
                            });
                        sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


}
