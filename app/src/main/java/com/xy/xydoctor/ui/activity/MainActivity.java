package com.xy.xydoctor.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.imuxuan.floatingview.FloatingView;
import com.lyd.baselib.util.TurnsUtils;
import com.lyd.baselib.util.eventbus.BindEventBus;
import com.lyd.baselib.util.eventbus.EventBusUtils;
import com.lyd.baselib.util.eventbus.EventMessage;
import com.lyd.baselib.util.notification.NotificationUtils;
import com.lyd.baselib.util.sp.SPUtils;
import com.lyd.librongim.myrongim.GroupUserBean;
import com.lyd.librongim.rongim.RongImInterface;
import com.lyd.librongim.rongim.RongImUtils;
import com.rxjava.rxlife.RxLife;
import com.xy.xydoctor.BuildConfig;
import com.xy.xydoctor.R;
import com.xy.xydoctor.base.activity.BaseEventBusActivity;
import com.xy.xydoctor.bean.ImTokenBean;
import com.xy.xydoctor.bean.IsFamilyBean;
import com.xy.xydoctor.bean.UpdateBean;
import com.xy.xydoctor.constant.ConstantParam;
import com.xy.xydoctor.net.ErrorInfo;
import com.xy.xydoctor.net.OnError;
import com.xy.xydoctor.net.XyUrl;
import com.xy.xydoctor.ui.activity.mydevice.HomeScanActivity;
import com.xy.xydoctor.utils.DialogUtils;
import com.xy.xydoctor.utils.UpdateUtils;
import com.xy.xydoctor.view.NumberProgressBar;
import com.xy.xydoctor.view.popup.UpdatePopup;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;
import rxhttp.wrapper.param.RxHttp;

/**
 * 描述: 主页面
 * 作者: LYD
 * 创建日期: 2019/6/11 11:50
 */
@BindEventBus
public class MainActivity extends BaseEventBusActivity implements IUnReadMessageObserver, View.OnClickListener, OnDownloadListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.bnv_main)
    BottomNavigationView bnvMain;

    //更新进度
    private UpdatePopup updatePopup;
    //更新的apk新版本号
    private AppCompatTextView tvUpdateName;
    //更新的新apk大小
    private AppCompatTextView tvUpdateSize;
    //更新内容
    private AppCompatTextView tvUpdateContent;
    //进度条
    private NumberProgressBar pbUpdateProgress;
    //升级按钮
    private AppCompatTextView tvUpdateUpdate;
    //关闭按钮和线(显示隐藏)
    private LinearLayout llUpdateCancel;
    //关闭按钮(点击取消)
    private AppCompatImageView ivUpdateClose;
    //更新进度
    //更新网址
    private String updateUrl;
    private File updateApk;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        //获取医生下所有成员
        getAllUsers();
        //初始化底部导航栏
        initBottomBar();
        //弹窗
        initPopup();
        //获取融云token
        getImKitToken();
        //获取融云未读消息
        getImUnReadMsgCount();
        //设置友盟推送别名
        setUPushAlias();
        //更新软件
        toUpdate();
        //判断是否家签
        //getIsFamily();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNotifySettings();
    }

    /**
     * 检查通知权限
     */
    private void checkNotifySettings() {
        boolean notificationEnabled = NotificationUtils.isNotificationEnabled(getPageContext());
        if (notificationEnabled) {
        } else {
            DialogUtils.getInstance().showDialog(getPageContext(), "", "请点击确定跳转至设置页面,并允许通知.", true, new DialogUtils.DialogCallBack() {
                @Override
                public void execEvent() {
                    NotificationUtils.goToSettings(getPageContext());
                }
            });
        }
    }

    /**
     * 家签
     */
    private void getIsFamily() {
        HashMap<String, Object> map = new HashMap<>();
        RxHttp.postForm(XyUrl.IS_FAMILY)
                .addAll(map)
                .asResponse(IsFamilyBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<IsFamilyBean>() {
                    @Override
                    public void accept(IsFamilyBean isFamilyBean) throws Exception {
                        //MenuItem homeSignItem = bnvMain.getMenu().findItem(R.id.navigation_home_sign);
                        //int isfamily = isFamilyBean.getIsfamily();
                        //if (1 == isfamily) {
                        //    homeSignItem.setVisible(true);
                        //} else {
                        //    homeSignItem.setVisible(false);
                        //}
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }


    /**
     * 初始化更新弹窗
     */
    private void initPopup() {
        updatePopup = new UpdatePopup(getPageContext());
        tvUpdateName = updatePopup.findViewById(R.id.tv_update_name);
        tvUpdateSize = updatePopup.findViewById(R.id.tv_update_size);
        tvUpdateContent = updatePopup.findViewById(R.id.tv_update_content);
        pbUpdateProgress = updatePopup.findViewById(R.id.pb_update_progress);
        tvUpdateUpdate = updatePopup.findViewById(R.id.tv_update_update);
        llUpdateCancel = updatePopup.findViewById(R.id.ll_update_cancel);
        ivUpdateClose = updatePopup.findViewById(R.id.iv_update_close);
        tvUpdateUpdate.setOnClickListener(this);
        ivUpdateClose.setOnClickListener(this);
    }


    /**
     * 初始化底部导航
     */
    private void initBottomBar() {
        MenuItem patientItem = bnvMain.getMenu().findItem(R.id.navigation_patient);
        MenuItem doctorItem = bnvMain.getMenu().findItem(R.id.navigation_doctor);
        int type = SPStaticUtils.getInt("docType");
        //医生
        if (3 == type) {
            patientItem.setVisible(false);
            doctorItem.setVisible(true);
        } else {
            patientItem.setVisible(true);
            doctorItem.setVisible(false);
        }
        showTitleBar();
        hideBack();
        showLine();
        setTitle("工作台");
        getTvMore().setVisibility(View.VISIBLE);
        getTvMore().setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getPageContext(), R.drawable.home_index_top_scan), null, null, null);
        getTvMore().setCompoundDrawablePadding(5);
        getTvMore().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils
                        .permission(PermissionConstants.CAMERA)
                        .callback(new PermissionUtils.SimpleCallback() {
                            @Override
                            public void onGranted() {
                                Intent intent = new Intent(getPageContext(), HomeScanActivity.class);
                                intent.putExtra("type", "main");
                                startActivity(intent);
                            }

                            @Override
                            public void onDenied() {
                                ToastUtils.showShort("请允许使用相机权限");
                            }
                        }).request();
            }
        });
        //取消导航栏图标着色
        bnvMain.setItemIconTintList(null);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bnvMain, navController);
        bnvMain.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.navigation_home:
                        navController.navigate(R.id.navigation_home);
                        showTitleBar();
                        hideBack();
                        showLine();
                        setTitle("工作台");
                        getTvMore().setVisibility(View.VISIBLE);
                        break;
                    case R.id.navigation_patient:
                        navController.navigate(R.id.navigation_patient);
                        showTitleBar();
                        hideBack();
                        showLine();
                        setTitle("我的患者");
                        getTvMore().setVisibility(View.GONE);
                        break;
                    case R.id.navigation_doctor:
                        navController.navigate(R.id.navigation_doctor);
                        showTitleBar();
                        hideBack();
                        showLine();
                        setTitle("我的医生");
                        getTvMore().setVisibility(View.GONE);
                        break;
                    // case R.id.navigation_home_sign:
                    //     navController.navigate(R.id.navigation_home_sign);
                    //     showTitleBar();
                    //     hideBack();
                    //     setTitle("家签");
                    //     break;
                    case R.id.navigation_user:
                        navController.navigate(R.id.navigation_user);
                        hideTitleBar();
                        hideLine();
                        getTvMore().setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 设置别名
     */
    private void setUPushAlias() {
        String docId = SPStaticUtils.getString("docId");
//        String aliasBefore = BuildConfig.ENVIRONMENT ? "p_" : "t_";
        String aliasBefore = BuildConfig.ENVIRONMENT ? "wt_" : "t_";
        String bindAccount = aliasBefore + docId;
        Log.e("aliPush", "账号==" + bindAccount);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.bindAccount(bindAccount, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e("aliPush", "账号绑定成功" + s);
            }

            @Override
            public void onFailed(String s, String s1) {
                Log.e("aliPush", "账号绑定失败");
            }
        });
    }


    /**
     * 获取融云未读消息
     */
    private void getImUnReadMsgCount() {
        final Conversation.ConversationType[] conversationTypes = {Conversation.ConversationType.PRIVATE};
        RongIM.getInstance().addUnReadMessageCountChangedObserver(this, conversationTypes);
    }

    /**
     * 更新调取接口
     */
    private void toUpdate() {
        HashMap map = new HashMap<>();
        RxHttp.postForm(XyUrl.GET_UPDATE)
                .addAll(map)
                .asResponse(UpdateBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<UpdateBean>() {
                    @Override
                    public void accept(UpdateBean updateBean) throws Exception {
                        toDoUpdate(updateBean);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }

    /***
     * 更新操作
     * @param data
     */
    private void toDoUpdate(UpdateBean data) {
        int netVersion = data.getVersion();
        int localVersion = AppUtils.getAppVersionCode();
        if (localVersion < netVersion) {
            toShowUpdateDialog(data);
        }
    }

    /**
     * 显示更新
     *
     * @param data
     */
    private void toShowUpdateDialog(UpdateBean data) {
        updateUrl = data.getUpdateurl();
        String versionName = data.getVersionname();
        String apkSize = data.getAppsize();
        String updateContent = data.getUpcontent();
        tvUpdateName.setText(versionName);
        tvUpdateSize.setText(apkSize);
        tvUpdateContent.setText(updateContent);
        tvUpdateContent.setVisibility(updateContent == null ? View.GONE : View.VISIBLE);
        updatePopup.showPopupWindow();
    }

    /**
     * 获取融云token
     */
    private void getImKitToken() {
        HashMap map = new HashMap<>();
        RxHttp.postForm(XyUrl.GET_IM_TOKEN)
                .addAll(map)
                .asResponse(ImTokenBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<ImTokenBean>() {
                    @Override
                    public void accept(ImTokenBean imTokenBean) throws Exception {
                        String imToken = imTokenBean.getToken();
                        SPStaticUtils.put("loginImToken", imToken);
                        RongImUtils.logout();
                        //连接融云服务器
                        RongImUtils.connect(imToken, new RongImInterface.ConnectCallback() {
                            @Override
                            public void onSuccess() {
                                //获取会话列表
                                RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
                                    @Override
                                    public void onSuccess(List<Conversation> conversations) {
                                        if (conversations != null && conversations.size() > 0) {
                                            for (int i = 0; i < conversations.size(); i++) {
                                                Conversation conversation = conversations.get(i);
                                                long sentTime = conversation.getSentTime();
                                                String targetId = conversation.getTargetId();
                                                int unreadMessageCount = conversation.getUnreadMessageCount();
                                                Log.e(TAG, "时间==" + sentTime);
                                                Log.e(TAG, "发送者Id==" + targetId);
                                                Log.e(TAG, "未读消息数量==" + unreadMessageCount);
                                                MessageContent messageContent = conversation.getLatestMessage();
                                                if (messageContent instanceof TextMessage) {
                                                    //文本消息
                                                    TextMessage textMessage = (TextMessage) messageContent;
                                                    String str = textMessage.getContent();
                                                    Log.e(TAG, "最后一条消息内容==" + str);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                    }
                                }, Conversation.ConversationType.PRIVATE);
                            }
                        });
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }


    /**
     * 退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 再按一次退出程序
     */
    public void exit() {
        //if ((System.currentTimeMillis() - exitTime) > 2000) {
        //    ToastUtils.showShort("再按一次退出程序");
        //    exitTime = System.currentTimeMillis();
        //} else {
        //    ActivityUtils.finishAllActivities();
        //    System.exit(0);
        //}
        ClickUtils.back2HomeFriendly("再按一次退出程序");
    }

    /**
     * 注销已注册的未读消息数变化监听器。
     * <p>
     * 接收未读消息消息的监听器。
     */
    @Override
    protected void onDestroy() {
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(this);
        super.onDestroy();
    }


    /**
     * 未读消息数量监听
     *
     * @param count
     */
    @Override
    public void onCountChanged(int count) {
        Log.e(TAG, "count: " + count);
        SPStaticUtils.put("imUnReadMsgCount", count);
        EventBusUtils.post(new EventMessage(ConstantParam.EventCode.IM_UNREAD_MSG_COUNT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //更新升级按钮
            case R.id.tv_update_update:
                //判断字体获取状态
                String text = (String) tvUpdateUpdate.getText();
                switch (text) {
                    case "升级":
                    case "下载失败":
                        //下载并安装
                        toDownLoadAndInstallApk();
                        break;
                    case "下载中":
                        //没有动作
                        break;
                    case "立即安装":
                        //安装apk
                        AppUtils.installApp(updateApk);
                        break;
                }
                break;
            //关闭按钮
            case R.id.iv_update_close:
                updatePopup.dismiss();
                break;
        }
    }

    private void toDownLoadAndInstallApk() {
        UpdateUtils.downloadAndInstall(updateUrl, this);
    }

    @Override
    public void start() {
    }

    @Override
    public void downloading(int max, int progress) {
        //百分比
        int curr = (int) (progress / (double) max * 100.0);
        //下载中
        tvUpdateUpdate.setText("下载中");
        pbUpdateProgress.setVisibility(View.VISIBLE);
        pbUpdateProgress.setProgress(curr);
    }

    @Override
    public void done(File apk) {
        updateApk = apk;
        //立即安装
        tvUpdateUpdate.setText("立即安装");
        pbUpdateProgress.setVisibility(View.GONE);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
        tvUpdateUpdate.setText("下载失败");
    }


    /**
     *
     */
    private void getAllUsers() {
        int docType = SPStaticUtils.getInt("docType");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gid", 0 + "");
        if (3 == docType) {
            int imDocid = SPStaticUtils.getInt("imDocid", 0);
            map.put("userid", imDocid);
        }
        RxHttp.postForm(XyUrl.GET_GROUP_USER)
                .addAll(map)
                .asResponseList(GroupUserBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<List<GroupUserBean>>() {
                    @Override
                    public void accept(List<GroupUserBean> list) throws Exception {
                        Log.e(TAG, "allUsers大小==" + list.size());
                        GroupUserBean doctorUserBean = new GroupUserBean();
                        if (3 == docType) {
                            int docId = SPStaticUtils.getInt("imDocid");
                            String docName = SPStaticUtils.getString("imDocName");
                            String docHeadImg = SPStaticUtils.getString("imDocPic");
                            doctorUserBean.setUserid(docId);
                            doctorUserBean.setPicture(docHeadImg);
                            doctorUserBean.setNickname(docName);
                        } else {
                            String docId = SPStaticUtils.getString("docId");
                            String docName = SPStaticUtils.getString("docName");
                            String docHeadImg = SPStaticUtils.getString("docHeadImg");
                            int intDoctorId = TurnsUtils.getInt(docId, 0);
                            doctorUserBean.setUserid(intDoctorId);
                            doctorUserBean.setPicture(docHeadImg);
                            doctorUserBean.setNickname(docName);
                        }
                        list.add(doctorUserBean);
                        SPUtils.putBean("allUsers", list);
                        RongImUtils.setCurrentUserInfo(list, doctorUserBean);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {
                        ToastUtils.cancel();
                    }
                });
    }

    @Override
    protected void receiveEvent(EventMessage event) {
        super.receiveEvent(event);
        switch (event.getCode()) {
            case ConstantParam.EventCode.PATIENT_ADD:
            case ConstantParam.EventCode.DIRECTOR_REFRESH:
                getAllUsers();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FloatingView.get().detach(this);
    }
}
