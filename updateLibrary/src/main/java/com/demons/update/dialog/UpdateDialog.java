package com.demons.update.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.azhon.appupdate.R;
import com.demons.update.config.UpdateConfiguration;
import com.demons.update.listener.OnButtonClickListener;
import com.demons.update.listener.OnDownloadListener;
import com.demons.update.manager.DownloadManager;
import com.demons.update.service.DownloadService;
import com.demons.update.utils.ApkUtil;
import com.demons.update.utils.DensityUtil;
import com.demons.update.utils.ScreenUtil;

import java.io.File;

/**
 * 显示升级对话框
 */
public class UpdateDialog extends Dialog implements View.OnClickListener, OnDownloadListener {

    private Context context;
    private DownloadManager manager;
    private boolean forcedUpgrade;
    private Button update;
    private NumberProgressBar progressBar;
    private OnButtonClickListener buttonClickListener;
    private int dialogImage, dialogButtonTextColor, dialogButtonColor, dialogProgressBarColor;
    private File apk;
    private final int install = 0x45F;

    public UpdateDialog(@NonNull Context context) {
        super(context, R.style.UpdateDialog);
        init(context);
    }

    /**
     * 初始化布局
     */
    private void init(Context context) {
        this.context = context;
        manager = DownloadManager.getInstance();
        UpdateConfiguration configuration = manager.getConfiguration();
        configuration.setOnDownloadListener(this);
        forcedUpgrade = configuration.isForcedUpgrade();
        buttonClickListener = configuration.getOnButtonClickListener();
        dialogImage = configuration.getDialogImage();
        dialogButtonTextColor = configuration.getDialogButtonTextColor();
        dialogButtonColor = configuration.getDialogButtonColor();
        dialogProgressBarColor = configuration.getDialogProgressBarColor();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        setContentView(view);
        setWindowSize(context);
        initView(view);
    }

    private void initView(View view) {
        View ibClose = view.findViewById(R.id.ib_close);
        ImageView ivBg = view.findViewById(R.id.iv_bg);
        TextView title = view.findViewById(R.id.tv_title);
        TextView size = view.findViewById(R.id.tv_size);
        TextView description = view.findViewById(R.id.tv_description);
        progressBar = view.findViewById(R.id.np_bar);
        progressBar.setVisibility(forcedUpgrade ? View.VISIBLE : View.GONE);
        update = view.findViewById(R.id.btn_update);
        update.setTag(0);
        View line = view.findViewById(R.id.line);
        update.setOnClickListener(this);
        ibClose.setOnClickListener(this);
        //自定义
        if (dialogImage != -1) {
            ivBg.setBackgroundResource(dialogImage);
        }
        if (dialogButtonTextColor != -1) {
            update.setTextColor(dialogButtonTextColor);
        }
        if (dialogButtonColor != -1) {
            StateListDrawable drawable = new StateListDrawable();
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(dialogButtonColor);
            colorDrawable.setCornerRadius(DensityUtil.dip2px(context, 3));
            drawable.addState(new int[]{android.R.attr.state_pressed}, colorDrawable);
            drawable.addState(new int[]{}, colorDrawable);
            update.setBackgroundDrawable(drawable);
        }
        if (dialogProgressBarColor != -1) {
            progressBar.setReachedBarColor(dialogProgressBarColor);
            progressBar.setProgressTextColor(dialogProgressBarColor);
        }
        //强制升级
        if (forcedUpgrade) {
            line.setVisibility(View.GONE);
            ibClose.setVisibility(View.GONE);
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    //屏蔽返回键
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
        }
        //设置界面数据
        if (!TextUtils.isEmpty(manager.getApkVersionName())) {
            String newVersion = context.getResources().getString(R.string.dialog_new);
            title.setText(String.format(newVersion, manager.getApkVersionName()));
        }
        if (!TextUtils.isEmpty(manager.getApkSize())) {
            String newVersionSize = context.getResources().getString(R.string.dialog_new_size);
            size.setText(String.format(newVersionSize, manager.getApkSize()));
            size.setVisibility(View.VISIBLE);
        }
        description.setText(manager.getApkDescription());
    }

    private void setWindowSize(Context context) {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenUtil.getWith(context) * 0.7f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ib_close) {
            if (!forcedUpgrade) {
                dismiss();
            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.CANCEL);
            }
        } else if (id == R.id.btn_update) {
            if ((int) update.getTag() == install) {
                installApk();
                return;
            }
            if (forcedUpgrade) {
                update.setEnabled(false);
                update.setText(R.string.background_downloading);
            } else {
                dismiss();
            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.UPDATE);
            }
            context.startService(new Intent(context, DownloadService.class));
        }
    }

    /**
     * 强制更新，点击进行安装
     */
    private void installApk() {
        String authorities = manager.getAuthorities();
        //如果没有设置则为包名
        if (TextUtils.isEmpty(authorities)) {
            authorities = context.getPackageName();
        }
        if(apk.getName().endsWith("apk")){
            ApkUtil.install(context, apk);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int progressPercent) {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setProgress(progressPercent);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void done(File apk) {
        context.stopService(new Intent(context, DownloadService.class));
        this.apk = apk;
        if (forcedUpgrade) {
            update.setTag(install);
            update.setEnabled(true);
            update.setText(R.string.click_hint);
        }
    }

    @Override
    public void error(Exception e) {

    }
}
