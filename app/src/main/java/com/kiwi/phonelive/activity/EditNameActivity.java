package com.kiwi.phonelive.activity;

import android.content.Intent;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kiwi.phonelive.AppConfig;
import com.kiwi.phonelive.Constants;
import com.kiwi.phonelive.R;
import com.kiwi.phonelive.bean.UserBean;
import com.kiwi.phonelive.event.UserInfoEvent;
import com.kiwi.phonelive.http.HttpCallback;
import com.kiwi.phonelive.http.HttpConsts;
import com.kiwi.phonelive.http.HttpUtil;
import com.kiwi.phonelive.utils.ToastUtil;
import com.kiwi.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2018/9/29.
 * 设置昵称
 */

public class EditNameActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_name;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.edit_profile_update_nickname));
        mEditText = (EditText) findViewById(R.id.edit);
        mEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(8)
        });
        findViewById(R.id.btn_save).setOnClickListener(this);
        String content = getIntent().getStringExtra(Constants.NICK_NAME);
        if (!TextUtils.isEmpty(content)) {
            if (content.length() > 8) {
                content = content.substring(0, 8);
            }
            mEditText.setText(content);
            mEditText.setSelection(content.length());
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        final String content = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(R.string.edit_profile_name_empty);
            return;
        }
        HttpUtil.updateFields("{\"user_nicename\":\"" + content + "\"}", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        ToastUtil.show(obj.getString("msg"));
                        UserBean u = AppConfig.getInstance().getUserBean();
                        if (u != null) {
                            u.setUserNiceName(content);
                        }
                        Intent intent = getIntent();
                        intent.putExtra(Constants.NICK_NAME, content);
                        setResult(RESULT_OK, intent);
                        EventBus.getDefault().post(new UserInfoEvent());
                        finish();
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.UPDATE_FIELDS);
        super.onDestroy();
    }
}
