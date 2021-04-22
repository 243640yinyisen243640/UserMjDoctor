package com.xy.xydoctor.ui.activity.healthrecord;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;
import com.rxjava.rxlife.RxLife;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xy.xydoctor.R;
import com.xy.xydoctor.adapter.CheckAdapter;
import com.xy.xydoctor.bean.CheckBean;
import com.xy.xydoctor.net.ErrorInfo;
import com.xy.xydoctor.net.OnError;
import com.xy.xydoctor.net.XyUrl;
import com.xy.xydoctor.utils.PickerUtils;
import com.xy.xydoctor.view.popup.SlideFromTopPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.functions.Consumer;
import razerdp.basepopup.BasePopupWindow;
import rxhttp.wrapper.param.RxHttp;

/**
 * 描述: 检查记录
 * 作者: LYD
 * 创建日期: 2019/3/4 17:31
 */
public class HealthRecordCheckListActivity extends BaseHideLineActivity {
    @BindView(R.id.img_top_back)
    ImageView imgTopBack;
    @BindView(R.id.tv_top_title)
    TextView tvTopTitle;
    @BindView(R.id.ll_up_down)
    LinearLayout llUpDown;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    //popu开始
    SlideFromTopPopup popu;
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;
    //popu结束
    @BindView(R.id.lv_check)
    ListView lvCheck;
    @BindView(R.id.srl_check)
    SmartRefreshLayout srlCheck;
    private RotateAnimation showArrowAnima;
    private RotateAnimation dismissArrowAnima;
    private int pageIndex = 1;//当前获取的是第几页的数据
    private List<CheckBean> list = new ArrayList<>();//ListView显示的数据
    private List<CheckBean> tempList;//用于临时保存ListView显示的数据
    private CheckAdapter adapter;

    private String beginTime = "";
    private String endTime = "";
    /**
     * popu取消监听
     */
    private BasePopupWindow.OnDismissListener onDismissListener = new BasePopupWindow.OnDismissListener() {

        @Override
        public boolean onBeforeDismiss() {
            //startDismissArrowAnima();
            return super.onBeforeDismiss();
        }

        @Override
        public void onDismiss() {

        }
    };


    private void initValue(String beginTime, String endTime) {
        String userId = getIntent().getStringExtra("userid");
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", userId + "");
        map.put("begintime", beginTime);
        map.put("endtime", endTime);
        map.put("page", pageIndex);
        RxHttp.postForm(XyUrl.GET_CHECK_LIST)
                .addAll(map)
                .asResponseList(CheckBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<List<CheckBean>>() {
                    @Override
                    public void accept(List<CheckBean> myTreatPlanBeans) throws Exception {
                        tempList = myTreatPlanBeans;
                        //少于10条,将不会再次触发加载更多事件
                        if (tempList.size() < 10) {
                            srlCheck.finishLoadMoreWithNoMoreData();
                        } else {
                            srlCheck.finishLoadMore();
                        }
                        //设置数据
                        if (pageIndex == 1) {
                            if (list == null) {
                                list = new ArrayList<>();
                            } else {
                                list.clear();
                            }
                            list.addAll(tempList);
                            adapter = new CheckAdapter(Utils.getApp(), R.layout.item_health_record_check, list);
                            lvCheck.setAdapter(adapter);
                        } else {
                            list.addAll(tempList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }

    private void initReFresh() {
        srlCheck.setEnableAutoLoadMore(true);//开启自动加载功能(非必须）
        //下拉刷新
        srlCheck.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                initValue(beginTime, endTime);
                srlCheck.finishRefresh();
                refreshLayout.setNoMoreData(false);//恢复没有更多数据的原始状态
            }
        });
        //上拉加载
        srlCheck.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                ++pageIndex;
                initValue(beginTime, endTime);
            }
        });
    }

    @OnClick({R.id.img_top_back, R.id.tv_start_time, R.id.tv_end_time, R.id.ll_up_down})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_top_back://popu
                finish();
                break;
            case R.id.ll_up_down://popu
                dismissPopu();
                break;
            case R.id.tv_start_time:
                PickerUtils.showTime(getPageContext(), new PickerUtils.TimePickerCallBack() {
                    @Override
                    public void execEvent(String time) {
                        tvStartTime.setText(time);
                        beginTime = time;
                        if (!TextUtils.isEmpty(endTime)) {
                            pageIndex = 1;
                            if (tempList != null) {
                                tempList.clear();
                            }
                            if (list != null) {
                                list.clear();
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            initValue(beginTime, endTime);
                        }
                    }
                });
                break;
            case R.id.tv_end_time:
                PickerUtils.showTime(getPageContext(), new PickerUtils.TimePickerCallBack() {
                    @Override
                    public void execEvent(String time) {
                        tvEndTime.setText(time);
                        endTime = time;
                        if (!TextUtils.isEmpty(beginTime)) {
                            pageIndex = 1;
                            if (tempList != null) {
                                tempList.clear();
                            }
                            if (list != null) {
                                list.clear();
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            initValue(beginTime, endTime);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 初始化popu
     */
    private void initPopu() {
        popu = new SlideFromTopPopup(getPageContext(), getIntent().getStringExtra("userid"));
        popu.setOnDismissListener(onDismissListener);
        //        buildShowArrowAnima();
        //        buildDismissArrowAnima();
    }

    /**
     * 隐藏popu
     */
    private void dismissPopu() {
        //        if (!popu.isShowing())
        //            startShowArrowAnima();
        popu.showPopupWindow(rlTitle);
    }

    /**
     * 箭头展开动画
     */
    private void buildShowArrowAnima() {
        if (showArrowAnima != null)
            return;
        showArrowAnima = new RotateAnimation(0, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        showArrowAnima.setDuration(450);
        showArrowAnima.setInterpolator(new AccelerateDecelerateInterpolator());
        showArrowAnima.setFillAfter(true);
    }

    /**
     * 展示箭头开始动画
     */
    //    private void startShowArrowAnima() {
    //        if (imgUpDown == null)
    //            return;
    //        imgUpDown.clearAnimation();
    //        imgUpDown.startAnimation(showArrowAnima);
    //    }

    /**
     * 展示箭头取消动画
     */
    //    private void startDismissArrowAnima() {
    //        if (imgUpDown == null)
    //            return;
    //        imgUpDown.clearAnimation();
    //        imgUpDown.startAnimation(dismissArrowAnima);
    //    }

    /**
     * 箭头取消动画
     */
    private void buildDismissArrowAnima() {
        if (dismissArrowAnima != null)
            return;
        dismissArrowAnima = new RotateAnimation(180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        dismissArrowAnima.setDuration(450);
        dismissArrowAnima.setInterpolator(new AccelerateDecelerateInterpolator());
        dismissArrowAnima.setFillAfter(true);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_health_record_check_list;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        tvTopTitle.setText("检查记录");
        hideTitleBar();
        initReFresh();
        initValue(beginTime, endTime);
        initPopu();
    }
}