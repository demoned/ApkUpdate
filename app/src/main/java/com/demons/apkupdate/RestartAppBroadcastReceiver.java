package com.demons.apkupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.demons.update.utils.LogUtil;

public class RestartAppBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = RestartAppBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            startApp(context);
        }

        // 接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            LogUtil.d(TAG, "安装了:" + packageName + "包名的程序");
            startApp(context);
        }

        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            LogUtil.d(TAG, "卸载了:" + packageName + "包名的程序");
        }
    }

    public void startApp(Context context) {
        Intent resolveIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        context.startActivity(resolveIntent);
    }
}