package com.kiwi.phonelive.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.kiwi.phonelive.Constants;
import com.kiwi.phonelive.R;
import com.kiwi.phonelive.activity.LiveActivity;
import com.kiwi.phonelive.adapter.GuardAdapter;
import com.kiwi.phonelive.adapter.RefreshAdapter;
import com.kiwi.phonelive.bean.GuardUserBean;
import com.kiwi.phonelive.bean.LiveGuardInfo;
import com.kiwi.phonelive.custom.RefreshView;
import com.kiwi.phonelive.http.HttpCallback;
import com.kiwi.phonelive.http.HttpConsts;
import com.kiwi.phonelive.http.HttpUtil;
import com.kiwi.phonelive.utils.DpUtil;
import com.kiwi.phonelive.utils.ToastUtil;
import com.kiwi.phonelive.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/6.
 */

public class LiveGuardDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private RefreshView mRefreshView;
    private TextView mGuardNum;
    private View mBottom;
    private TextView mTip;
    private TextView mBtnBuy;
    private GuardAdapter mGuardAdapter;
    private String mLiveUid;
    private boolean mIsAnchor;//是否是主播
    private int num;//是否是主播
    private LiveGuardInfo mLiveGuardInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_guard_list;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = DpUtil.dp2px(360);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    public void setLiveGuardInfo(LiveGuardInfo info) {
        mLiveGuardInfo = info;
    }

    public void setmGuardNum(int num){
        this.num = num;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mIsAnchor = bundle.getBoolean(Constants.ANCHOR, false);
        mLiveUid = bundle.getString(Constants.LIVE_UID);
        mGuardNum = (TextView) mRootView.findViewById(R.id.guard_num);
        mBottom = mRootView.findViewById(R.id.bottom);
        if (mIsAnchor) {
            mBottom.setVisibility(View.GONE);
            if (mLiveGuardInfo != null) {
                mGuardNum.setText(WordUtil.getString(R.string.guard_guard) + "(" + num + ")");
            }
        } else {
            mTip = (TextView) mRootView.findViewById(R.id.tip);
            mBtnBuy = (TextView) mRootView.findViewById(R.id.btn_buy);
            mBtnBuy.setOnClickListener(this);
            if (mLiveGuardInfo != null) {
                mGuardNum.setText(WordUtil.getString(R.string.guard_guard) + "(" + num + ")");
                int guardType = mLiveGuardInfo.getMyGuardType();
                if (guardType == Constants.GUARD_TYPE_NONE) {
                    mTip.setText(R.string.guard_tip_0);
                } else if (guardType == Constants.GUARD_TYPE_MONTH) {
                    mTip.setText(WordUtil.getString(R.string.guard_tip_1) + mLiveGuardInfo.getMyGuardEndTime());
                    mBtnBuy.setText(R.string.guard_buy_3);
                } else if (guardType == Constants.GUARD_TYPE_YEAR) {
                    mTip.setText(WordUtil.getString(R.string.guard_tip_2) + mLiveGuardInfo.getMyGuardEndTime());
                    mBtnBuy.setText(R.string.guard_buy_3);
                }
            }
        }
        mRefreshView = (RefreshView) mRootView.findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(mIsAnchor ? R.layout.view_no_data_guard_anc : R.layout.view_no_data_guard_aud);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<GuardUserBean>() {
            @Override
            public RefreshAdapter<GuardUserBean> getAdapter() {
                if (mGuardAdapter == null) {
                    mGuardAdapter = new GuardAdapter(mContext, true);
                }
                return mGuardAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getGuardList(mLiveUid, p, callback);
            }

            @Override
            public List<GuardUserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), GuardUserBean.class);
            }

            @Override
            public void onRefresh(List<GuardUserBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        mRefreshView.initData();
    }

    @Override
    public void onClick(View v) {
        dismiss();
        ((LiveActivity) mContext).openBuyGuardWindow();
    }

    @Override
    public void onDestroy() {
        mLiveGuardInfo=null;
        HttpUtil.cancel(HttpConsts.GET_GUARD_LIST);
        super.onDestroy();
    }
}
