package com.kiwi.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.kiwi.phonelive.AppConfig;
import com.kiwi.phonelive.AppContext;
import com.kiwi.phonelive.R;
import com.kiwi.phonelive.bean.ConfigBean;
import com.kiwi.phonelive.bean.UserBean;
import com.kiwi.phonelive.glide.ImgLoader;
import com.kiwi.phonelive.http.HttpCallback;
import com.kiwi.phonelive.http.HttpConsts;
import com.kiwi.phonelive.http.HttpUtil;
import com.kiwi.phonelive.interfaces.CommonCallback;
import com.kiwi.phonelive.utils.LocalManageUtil;
import com.kiwi.phonelive.utils.SpUtil;

/**
 * Created by cxf on 2018/9/17.
 */

public class LauncherActivity extends AppCompatActivity {

    private Handler mHandler;
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //下面的代码是为了防止一个bug:
        // 收到极光通知后，点击通知，如果没有启动app,则启动app。然后切后台，再次点击桌面图标，app会重新启动，而不是回到前台。
        Intent intent = getIntent();
        if (!isTaskRoot()
                && intent != null
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.getAction() != null
                && intent.getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        setStatusBar();
        setContentView(R.layout.activity_launcher);
        mContext = this;
        String selectLanguage = LocalManageUtil.getSelectLanguage(mContext);
        if (TextUtils.isEmpty(AppConfig.getInstance().getUid())) {
            LocalManageUtil.saveSelectLanguage(this, 3);
        } else {
            if (selectLanguage.contains("系统语言")||selectLanguage.contains("中文")) {
                setLanguage("zh-Hans-US");
            } else {
                setLanguage("en-US");
            }
        }
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getConfig();
            }
        }, 800);
    }

    private void setLanguage(final String l) {
        HttpUtil.setLanguage(l, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (l.contains("zh")) {
                        selectLanguage(1);
                    } else {
                        selectLanguage(3);
                    }
                }
            }
        });
    }

    private void selectLanguage(int select) {
        LocalManageUtil.saveSelectLanguage(this, select);
    }
    /**
     * 获取Config信息
     */
    private void getConfig() {
        HttpUtil.getConfig(mContext,new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                if (bean != null) {
                    AppContext.sInstance.initBeautySdk(bean.getBeautyKey());
                    checkUidAndToken();
                }
            }
        });
    }

    /**
     * 检查uid和token是否存在
     */
    private void checkUidAndToken() {
        String[] uidAndToken = SpUtil.getInstance().getMultiStringValue(
                new String[]{SpUtil.UID, SpUtil.TOKEN});
        final String uid = uidAndToken[0];
        final String token = uidAndToken[1];
        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(token)) {
            HttpUtil.ifToken(uid, token, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        AppConfig.getInstance().setLoginInfo(uid, token, false);
                        getBaseUserInfo();
                    }
                }
            });
        } else {
            LoginActivity.forward();
        }
    }

    /**
     * 获取用户信息
     */
    private void getBaseUserInfo() {
        HttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                if (bean != null) {
                    Intent intent = new Intent(mContext, FlashActivity.class);
                    startActivity(intent);
                    finish();
//                    forwardMainActivity();
                }
            }
        });
    }

    /**
     * 跳转到首页
     */
    private void forwardMainActivity() {
        MainActivity.forward(mContext);
        finish();
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.IF_TOKEN);
        HttpUtil.cancel(HttpConsts.GET_BASE_INFO);
        HttpUtil.cancel(HttpConsts.GET_CONFIG);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    /**
     * 设置透明状态栏
     */
    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0);
        }
    }
}
