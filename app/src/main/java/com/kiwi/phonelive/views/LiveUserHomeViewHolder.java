package com.kiwi.phonelive.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kiwi.phonelive.AppConfig;
import com.kiwi.phonelive.Constants;
import com.kiwi.phonelive.R;
import com.kiwi.phonelive.activity.AbsActivity;
import com.kiwi.phonelive.activity.ChatRoomActivity;
import com.kiwi.phonelive.activity.FansActivity;
import com.kiwi.phonelive.activity.FollowActivity;
import com.kiwi.phonelive.activity.LiveContributeActivity;
import com.kiwi.phonelive.activity.LiveGuardListActivity;
import com.kiwi.phonelive.activity.UserHomeActivity;
import com.kiwi.phonelive.adapter.ViewPagerAdapter;
import com.kiwi.phonelive.bean.ImpressBean;
import com.kiwi.phonelive.bean.LevelBean;
import com.kiwi.phonelive.bean.SearchUserBean;
import com.kiwi.phonelive.bean.UserBean;
import com.kiwi.phonelive.bean.UserHomeConBean;
import com.kiwi.phonelive.custom.MyTextView;
import com.kiwi.phonelive.custom.MyViewPager;
import com.kiwi.phonelive.custom.ViewPagerIndicator;
import com.kiwi.phonelive.dialog.LiveShareDialogFragment;
import com.kiwi.phonelive.event.AddFriendEvent;
import com.kiwi.phonelive.event.FollowEvent;
import com.kiwi.phonelive.glide.ImgLoader;
import com.kiwi.phonelive.http.HttpCallback;
import com.kiwi.phonelive.http.HttpConsts;
import com.kiwi.phonelive.http.HttpUtil;
import com.kiwi.phonelive.interfaces.CommonCallback;
import com.kiwi.phonelive.interfaces.LifeCycleAdapter;
import com.kiwi.phonelive.interfaces.LifeCycleListener;
import com.kiwi.phonelive.presenter.UserHomeSharePresenter;
import com.kiwi.phonelive.utils.IconUtil;
import com.kiwi.phonelive.utils.StringUtil;
import com.kiwi.phonelive.utils.ToastUtil;
import com.kiwi.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by cxf on 2018/10/18.
 * 用户个人主页
 */

public class LiveUserHomeViewHolder extends AbsLivePageViewHolder implements AppBarLayout.OnOffsetChangedListener, LiveShareDialogFragment.ActionListener {
    private VideoHomeViewHolder mVideoHomeViewHolder;
    private LiveRecordViewHolder mLiveRecordViewHolder;
    private List<LifeCycleListener> mLifeCycleListeners;
    private LayoutInflater mInflater;
    private AppBarLayout mAppBarLayout;
    private ImageView mAvatarBg;
    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private ImageView mLevelAnchor;
    private ImageView mLevel;
    private TextView mID;
    private TextView mBtnFans;
    private TextView mBtnFollow;
    private TextView mBtnFollow2;
    private TextView mSign;
    private LinearLayout mImpressGroup;
    private View mNoImpressTip;
    private TextView mVotesName;
    private LinearLayout mConGroup;
    private LinearLayout mGuardGroup;
    private TextView mTitleView;
    private ImageView mBtnBack;
    private ImageView mBtnShare;
    private TextView mBtnBlack;
    private MyViewPager mViewPager;
    private ViewPagerIndicator mIndicator;
    private String mToUid;
    private boolean mSelf;
    private float mRate;
    private int[] mWhiteColorArgb;
    private int[] mBlackColorArgb;
    private View.OnClickListener mAddImpressOnClickListener;
    private UserHomeSharePresenter mUserHomeSharePresenter;
    private SearchUserBean mSearchUserBean;
    private String mVideoString;
    private String mLiveString;
    private boolean mLoadLiveRecord;
    private int mVideoCount;
    private TextView add_friend;
    private boolean isSend;


    public LiveUserHomeViewHolder(Context context, ViewGroup parentView, String toUid) {
        super(context, parentView, toUid);
    }

    @Override
    protected void processArguments(Object... args) {
        mToUid = (String) args[0];
        if (!TextUtils.isEmpty(mToUid)) {
            mSelf = mToUid.equals(AppConfig.getInstance().getUid());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_user_home;
    }

    @Override
    public void init() {
        super.init();
        mInflater = LayoutInflater.from(mContext);
        View bottom = findViewById(R.id.bottom);
        if (mSelf) {
            if (bottom.getVisibility() == View.VISIBLE) {
                bottom.setVisibility(View.GONE);
            }
        } else {
            if (bottom.getVisibility() != View.VISIBLE) {
                bottom.setVisibility(View.VISIBLE);
            }
        }
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int i, String s, List<UserInfo> list) {
                Log.e("zlg","list====="+list.size());
                for (int j = 0; j < list.size(); j++) {
                    list.get(j).removeFromFriendList(new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if(i == 0){
                                Log.e("zlg","删除成功");
                            }
                        }
                    });
                }
            }
        });
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        add_friend = (TextView) findViewById(R.id.btn_add_friend);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mAvatarBg = (ImageView) findViewById(R.id.bg_avatar);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mSex = (ImageView) findViewById(R.id.sex);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mLevel = (ImageView) findViewById(R.id.level);
        mID = (TextView) findViewById(R.id.id_val);
        mBtnFans = (TextView) findViewById(R.id.btn_fans);
        mBtnFollow = (TextView) findViewById(R.id.btn_follow);
        mBtnFollow2 = (TextView) findViewById(R.id.btn_follow_2);
        mBtnBlack = (TextView) findViewById(R.id.btn_black);
        mSign = (TextView) findViewById(R.id.sign);
        mImpressGroup = (LinearLayout) findViewById(R.id.impress_group);
        mNoImpressTip = findViewById(R.id.no_impress_tip);
        mVotesName = (TextView) findViewById(R.id.votes_name);
        mConGroup = (LinearLayout) findViewById(R.id.con_group);
        mGuardGroup = (LinearLayout) findViewById(R.id.guard_group);
        mTitleView = (TextView) findViewById(R.id.titleView);
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mBtnShare = (ImageView) findViewById(R.id.btn_share);
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    if (!mLoadLiveRecord) {
                        mLoadLiveRecord = true;
                        if (mSearchUserBean != null) {
                            if (mLiveRecordViewHolder != null) {
                                mLiveRecordViewHolder.loadData(mSearchUserBean);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mVideoHomeViewHolder = new VideoHomeViewHolder(mContext, mViewPager, mToUid);
        mVideoHomeViewHolder.setRefreshEnable(false);
        mVideoHomeViewHolder.setActionListener(new VideoHomeViewHolder.ActionListener() {
            @Override
            public void onVideoDelete(int deleteCount) {
                mVideoCount -= deleteCount;
                if (mVideoCount < 0) {
                    mVideoCount = 0;
                }
                if (mIndicator != null) {
                    mIndicator.setTitleText(0, mVideoString + " " + mVideoCount);
                }
            }
        });
        mLiveRecordViewHolder = new LiveRecordViewHolder(mContext, mViewPager);
        mLifeCycleListener = new LifeCycleAdapter() {
            @Override
            public void onDestroy() {
                HttpUtil.cancel(HttpConsts.GET_USER_HOME);
                HttpUtil.cancel(HttpConsts.SET_ATTENTION);
                HttpUtil.cancel(HttpConsts.SET_BLACK);
            }
        };
        mLifeCycleListeners = new ArrayList<>();
        mLifeCycleListeners.add(mLifeCycleListener);
        mLifeCycleListeners.add(mVideoHomeViewHolder.getLifeCycleListener());
        mLifeCycleListeners.add(mLiveRecordViewHolder.getLifeCycleListener());
        List<View> viewList = new ArrayList<>();
        viewList.add(mVideoHomeViewHolder.getContentView());
        //UI说只让显示视频   屏蔽掉直播记录
//        viewList.add(mLiveRecordViewHolder.getContentView());
        mViewPager.setAdapter(new ViewPagerAdapter(viewList));
        mIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
        mIndicator.setVisibleChildCount(1);
        mVideoString = WordUtil.getString(R.string.video);
        mLiveString = WordUtil.getString(R.string.live);
        mIndicator.setTitles(new String[]{mVideoString});
//        mIndicator.setTitles(new String[]{mVideoString, mLiveString});
        mIndicator.setViewPager(mViewPager);
        mBtnShare.setOnClickListener(this);
        mBtnFans.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mBtnFollow2.setOnClickListener(this);
        mBtnBlack.setOnClickListener(this);
        add_friend.setOnClickListener(this);
        findViewById(R.id.btn_pri_msg).setOnClickListener(this);
        findViewById(R.id.con_group_wrap).setOnClickListener(this);
        findViewById(R.id.guard_group_wrap).setOnClickListener(this);
        mWhiteColorArgb = getColorArgb(0xffffffff);
        mBlackColorArgb = getColorArgb(0xff323232);
        mAddImpressOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImpress();
            }
        };
        mUserHomeSharePresenter = new UserHomeSharePresenter(mContext);
        EventBus.getDefault().register(this);
    }

    public List<LifeCycleListener> getLifeCycleListenerList() {
        return mLifeCycleListeners;
    }


    @Override
    public void loadData() {
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        HttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    SearchUserBean userBean = JSON.toJavaObject(obj, SearchUserBean.class);
                    mSearchUserBean = userBean;
                    if (mViewPager != null) {
                        mViewPager.setCanScroll(true);
                    }
                    if (mVideoHomeViewHolder != null) {
                        mVideoHomeViewHolder.loadData();
                    }
                    String avatar = userBean.getAvatar();
                    ImgLoader.displayBlur(avatar, mAvatarBg);
                    ImgLoader.displayAvatar(avatar, mAvatar);
                    String toName = userBean.getUserNiceName();
                    mName.setText(toName);
                    mTitleView.setText(toName);
                    mSex.setImageResource(IconUtil.getSexIcon(userBean.getSex()));
                    AppConfig appConfig = AppConfig.getInstance();
                    LevelBean levelAnchor = appConfig.getAnchorLevel(userBean.getLevelAnchor());
                    ImgLoader.display(levelAnchor.getThumb(), mLevelAnchor);
                    LevelBean level = appConfig.getLevel(userBean.getLevel());
                    ImgLoader.display(level.getThumb(), mLevel);
                    mID.setText(userBean.getLiangNameTip());
                    String fansNum = StringUtil.toWan(userBean.getFans());
                    mBtnFans.setText(fansNum + " " + WordUtil.getString(R.string.fans));
                    mBtnFollow.setText(StringUtil.toWan(userBean.getFollows()) + " " + WordUtil.getString(R.string.follow));
                    mSign.setText(userBean.getSignature());
                    mBtnFollow2.setText(obj.getIntValue("isattention") == 1 ? R.string.following : R.string.follow);
                    mBtnBlack.setText(obj.getIntValue("isblack") == 1 ? R.string.black_ing : R.string.black);
                    mVideoCount = obj.getIntValue("videonums");
                    mIndicator.setTitleText(0, mVideoString + " " + mVideoCount);
                    mIndicator.setTitleText(1, mLiveString + " " + obj.getString("livenums"));
                    showImpress(obj.getString("label"));
                    mVotesName.setText(WordUtil.getString(R.string.live_user_home_con));
                    mUserHomeSharePresenter.setToUid(mToUid).setToName(toName).setAvatarThumb(userBean.getAvatarThumb()).setFansNum(fansNum);
                    showContribute(obj.getString("contribute"));
                    showGuardList(obj.getString("guardlist"));
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    /**
     * 显示印象
     */
    private void showImpress(String impressJson) {
        List<ImpressBean> list = JSON.parseArray(impressJson, ImpressBean.class);
        if (list.size() > 3) {
            list = list.subList(0, 3);
        }
        if (!mSelf) {
            ImpressBean lastBean = new ImpressBean();
            lastBean.setName("+ " + WordUtil.getString(R.string.impress_add));
            lastBean.setColor("#ff0088");
            list.add(lastBean);
        } else {
            if (list.size() == 0) {
                mNoImpressTip.setVisibility(View.VISIBLE);
                return;
            }
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            MyTextView myTextView = (MyTextView) mInflater.inflate(R.layout.view_impress_item_3, mImpressGroup, false);
            ImpressBean bean = list.get(i);
            if (mSelf) {
                bean.setCheck(1);
            } else {
                if (i == size - 1) {
                    myTextView.setOnClickListener(mAddImpressOnClickListener);
                } else {
                    bean.setCheck(1);
                }
            }
            myTextView.setBean(bean);
            mImpressGroup.addView(myTextView);
        }
    }


    /**
     * 显示贡献榜
     */
    private void showContribute(String conJson) {
        List<UserHomeConBean> list = JSON.parseArray(conJson, UserHomeConBean.class);
        if (list.size() == 0) {
            return;
        }
        if (list.size() > 3) {
            list = list.subList(0, 3);
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            ImageView imageView = (ImageView) mInflater.inflate(R.layout.view_user_home_con, mConGroup, false);
            ImgLoader.display(list.get(i).getAvatar(), imageView);
            mConGroup.addView(imageView);
        }
    }

    /**
     * 显示守护榜
     */
    private void showGuardList(String guardJson) {
        List<UserBean> list = JSON.parseArray(guardJson, UserBean.class);
        if (list.size() == 0) {
            return;
        }
        if (list.size() > 3) {
            list = list.subList(0, 3);
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            ImageView imageView = (ImageView) mInflater.inflate(R.layout.view_user_home_con, mGuardGroup, false);
            ImgLoader.display(list.get(i).getAvatar(), imageView);
            mGuardGroup.addView(imageView);
        }
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        float totalScrollRange = appBarLayout.getTotalScrollRange();
        float rate = -1 * verticalOffset / totalScrollRange * 2;
        if (rate >= 1) {
            rate = 1;
        }
        if (mRate != rate) {
            mRate = rate;
            mTitleView.setAlpha(rate);
            int a = (int) (mWhiteColorArgb[0] * (1 - rate) + mBlackColorArgb[0] * rate);
            int r = (int) (mWhiteColorArgb[1] * (1 - rate) + mBlackColorArgb[1] * rate);
            int g = (int) (mWhiteColorArgb[2] * (1 - rate) + mBlackColorArgb[2] * rate);
            int b = (int) (mWhiteColorArgb[3] * (1 - rate) + mBlackColorArgb[3] * rate);
            int color = Color.argb(a, r, g, b);
            mBtnBack.setColorFilter(color);
            mBtnShare.setColorFilter(color);
        }
    }

    /**
     * 获取颜色的argb
     */
    private int[] getColorArgb(int color) {
        return new int[]{Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)};
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                back();
                break;
            case R.id.btn_share:
                share();
                break;
            case R.id.btn_fans:
                forwardFans();
                break;
            case R.id.btn_follow:
                forwardFollow();
                break;
            case R.id.btn_follow_2:
                follow();
                break;
            case R.id.con_group_wrap:
                forwardContribute();
                break;
            case R.id.guard_group_wrap:
                forwardGuardList();
                break;
            case R.id.btn_pri_msg:
                forwardMsg();
                break;
            case R.id.btn_black:
                setBlack();
                break;
            case R.id.btn_add_friend:

                if (isSend) {
                    ToastUtil.show(R.string.all_readay_send);
                    Log.e("zlg","issend===="+isSend);
                } else {
                    addFriend();
                    Log.e("zlg","issend add===="+isSend);
                }
                break;
        }
    }

    private void addFriend() {
        HttpUtil.addFriend(mToUid, "", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                Log.e("zlg","code===="+code+"====msg===="+msg);
                if (code == 0) {
                    AddFriendEvent addFriendEvent = new AddFriendEvent();
                    addFriendEvent.setTouid(mToUid);
                    EventBus.getDefault().post(addFriendEvent);
                    ContactManager.sendInvitationRequest(mToUid, "01f288ab431af874c0291e9e", "hello", new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            if (0 == responseCode) {
                                //好友请求请求发送成功
                                Log.e("zlg", "好友请求请求发送成功");
                                ToastUtil.show(R.string.friend_request_success);
                                isSend = true;
                            } else {
                                //好友请求发送失败
                                Log.e("zlg", "好友请求发送失败" + responseMessage);
                            }
                        }
                    });
                }else if(code == 1002){
                    ToastUtil.show(R.string.all_readay_friend);
                } else if (code == 1003) {
                    ToastUtil.show(R.string.all_readay_request);
                }
            }
        });
    }

    private void back() {
        if (mContext instanceof UserHomeActivity) {
            ((UserHomeActivity) mContext).onBackPressed();
        }
    }

    /**
     * 关注
     */
    private void follow() {
        HttpUtil.setAttention(Constants.FOLLOW_FROM_HOME, mToUid, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer isAttention) {
                onAttention(isAttention);
            }
        });
    }

    /**
     * 私信
     */
    private void forwardMsg() {
        if (mSearchUserBean != null) {
            Log.e("zlg", mSearchUserBean.getAttention() == 1 ? "true" : "false");
            ChatRoomActivity.forward(mContext, mSearchUserBean, mSearchUserBean.getAttention() == 1);
        }
    }

    private void onAttention(int isAttention) {
        if (mBtnFollow2 != null) {
            mBtnFollow2.setText(isAttention == 1 ? R.string.following : R.string.follow);
        }
        if (mBtnBlack != null) {
            if (isAttention == 1) {
                mBtnBlack.setText(R.string.black);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (e.getToUid().equals(mToUid)) {
            int isAttention = e.getIsAttention();
            if (mSearchUserBean != null) {
                mSearchUserBean.setAttention(isAttention);
            }
            onAttention(isAttention);
        }
    }

    /**
     * 添加印象
     */
    private void addImpress() {
        if (mContext instanceof UserHomeActivity) {
            ((UserHomeActivity) mContext).addImpress(mToUid);
        }
    }

    /**
     * 刷新印象
     */
    public void refreshImpress() {
        HttpUtil.getUserHome(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mImpressGroup != null) {
                        mImpressGroup.removeAllViews();
                    }
                    showImpress(obj.getString("label"));
                }
            }
        });
    }

    /**
     * 查看贡献榜
     */
    private void forwardContribute() {
        Intent intent = new Intent(mContext, LiveContributeActivity.class);
        intent.putExtra(Constants.TO_UID, mToUid);
        mContext.startActivity(intent);
    }

    /**
     * 查看守护榜
     */
    private void forwardGuardList() {
        LiveGuardListActivity.forward(mContext, mToUid);
    }

    /**
     * 前往TA的关注
     */
    private void forwardFollow() {
        Intent intent = new Intent(mContext, FollowActivity.class);
        intent.putExtra(Constants.TO_UID, mToUid);
        mContext.startActivity(intent);
    }

    /**
     * 前往TA的粉丝
     */
    private void forwardFans() {
        Intent intent = new Intent(mContext, FansActivity.class);
        intent.putExtra(Constants.TO_UID, mToUid);
        mContext.startActivity(intent);
    }

    /**
     * 拉黑，解除拉黑
     */
    private void setBlack() {
        HttpUtil.setBlack(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    boolean isblack = JSON.parseObject(info[0]).getIntValue("isblack") == 1;
                    mBtnBlack.setText(isblack ? R.string.black_ing : R.string.black);
                    if (isblack) {
                        mBtnFollow2.setText(R.string.follow);
                        EventBus.getDefault().post(new FollowEvent(Constants.FOLLOW_FROM_HOME, mToUid, 0));
                    }
                }
            }
        });
    }

    /**
     * 分享
     */
    private void share() {
        LiveShareDialogFragment fragment = new LiveShareDialogFragment();
        fragment.setActionListener(this);
        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveShareDialogFragment");
    }


    @Override
    public void onItemClick(String type) {
        if (Constants.LINK.equals(type)) {
            copyLink();
        } else {
            shareHomePage(type);
        }
    }

    /**
     * 复制页面链接
     */
    private void copyLink() {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.copyLink();
        }
    }


    /**
     * 分享页面链接
     */
    private void shareHomePage(String type) {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.shareHomePage(type);
        }
    }

    @Override
    public void release() {
        super.release();
        EventBus.getDefault().unregister(this);
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.release();
        }
        mUserHomeSharePresenter = null;
        if (mVideoHomeViewHolder != null) {
            mVideoHomeViewHolder.release();
        }
        mVideoHomeViewHolder = null;
    }


}
