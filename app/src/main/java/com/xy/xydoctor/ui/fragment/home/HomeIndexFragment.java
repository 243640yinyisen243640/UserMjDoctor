package com.xy.xydoctor.ui.fragment.home;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.gyf.immersionbar.ImmersionBar;
import com.gyf.immersionbar.components.SimpleImmersionOwner;
import com.gyf.immersionbar.components.SimpleImmersionProxy;
import com.horen.chart.barchart.BarChartHelper;
import com.horen.chart.barchart.IBarData;
import com.imuxuan.floatingview.FloatingMagnetView;
import com.imuxuan.floatingview.FloatingView;
import com.imuxuan.floatingview.MagnetViewListener;
import com.lyd.baselib.base.fragment.BaseEventBusFragment;
import com.lyd.baselib.util.eventbus.BindEventBus;
import com.lyd.baselib.util.eventbus.EventMessage;
import com.rxjava.rxlife.RxLife;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wei.android.lib.colorview.view.ColorTextView;
import com.xy.xydoctor.R;
import com.xy.xydoctor.adapter.ReachTheStandRateAdapter;
import com.xy.xydoctor.bean.IndexBean;
import com.xy.xydoctor.bean.ReachTheStandRateBean;
import com.xy.xydoctor.bean.TestBarData;
import com.xy.xydoctor.constant.ConstantParam;
import com.xy.xydoctor.net.ErrorInfo;
import com.xy.xydoctor.net.OnError;
import com.xy.xydoctor.net.XyUrl;
import com.xy.xydoctor.ui.activity.director.DoctorListActivity;
import com.xy.xydoctor.ui.activity.patient.PatientAddTodayListActivity;
import com.xy.xydoctor.ui.activity.patient.PatientCountMainActivity;
import com.xy.xydoctor.ui.activity.todo.ToDoListActivity;
import com.xy.xydoctor.utils.BadgeUtils;
import com.xy.xydoctor.utils.PickerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.functions.Consumer;
import rxhttp.wrapper.param.RxHttp;

/**
 * ??????: ?????????
 * ??????: LYD
 * ????????????: 2019/5/27 15:42
 */
@BindEventBus
public class HomeIndexFragment extends BaseEventBusFragment implements SimpleImmersionOwner {
    @BindView(R.id.srl)
    SmartRefreshLayout srl;
    //????????????
    @BindView(R.id.tv_user_total)
    TextView tvUserTotal;
    @BindView(R.id.tv_user_count_gxy)
    TextView tvUserCountGxy;
    @BindView(R.id.tv_user_count_tnb)
    TextView tvUserCountTnb;
    @BindView(R.id.tv_user_count_gxy_and_tnb)
    TextView tvUserCountGxyAndTnb;
    //???????????????
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;
    @BindView(R.id.rv_reach_the_stand_rate)
    RecyclerView rvReachTheStandRate;
    //??????????????????
    @BindView(R.id.tv_today_add_total)
    TextView tvTodayAddTotal;
    //??????????????????
    @BindView(R.id.bar_chart)
    BarChart barChart;

    private SimpleImmersionProxy mSimpleImmersionProxy = new SimpleImmersionProxy(this);
    private int unReadCount;
    //private NewDialogUtils bleDialogUtils = new NewDialogUtils();
    private String beginTime = "";
    private String endTime = "";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_index;
    }

    @Override
    protected void init(View rootView) {
        setTime();
        getIndex(beginTime, endTime);
        setRefresh();
        setDragView();
    }

    private void setTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String nowString = TimeUtils.millis2String(System.currentTimeMillis(), simpleDateFormat);
        tvStartTime.setText(nowString);
        tvEndTime.setText(nowString);
        beginTime = nowString;
        endTime = nowString;
    }


    private void setDragView() {
        FloatingView.get().icon(R.drawable.home_to_to);
        FloatingView.get().customView(R.layout.dragview_home_to_do);
        FloatingView.get().add();
        FloatingView.get().listener(new MagnetViewListener() {


            @Override
            public void onRemove(FloatingMagnetView magnetView) {

            }

            @Override
            public void onClick(FloatingMagnetView magnetView) {
                toJudgeToDo();
            }
        });

        int allDp = 48 + 60;
        int allDpPx = ConvertUtils.dp2px(allDp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.END;
        //????????????
        params.setMargins(0, allDpPx, 0, 0);
        FloatingView.get().layoutParams(params);
    }


    private void setRefresh() {
        srl.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                beginTime = "";
                endTime = "";
                getIndex(beginTime, endTime);
                srl.finishRefresh();
            }
        });
        srl.setEnableLoadMore(false);
    }

    /**
     * ??????????????????
     */
    private void getIndex(String beginTime, String endTime) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("starttime", beginTime);
        map.put("endtime", endTime);
        RxHttp.postForm(XyUrl.GET_INDEX)
                .addAll(map)
                .asResponse(IndexBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<IndexBean>() {
                    @Override
                    public void accept(IndexBean data) throws Exception {
                        //bleDialogUtils.dismissProgress();
                        //????????????
                        SpanUtils.with(tvUserTotal)
                                .append("????????????").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                                .appendSpace(15, Color.TRANSPARENT)
                                .append(data.getYhtj().getTotal() + "").setForegroundColor(ColorUtils.getColor(R.color.home_index_user_count_add))
                                .appendSpace(15, Color.TRANSPARENT)
                                .append("???").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                                .create();
                        //?????????????????? ????????????
                        setUserCount(data.getYhtj());
                        //???????????????
                        setReachTheStandRate(data.getSugars());
                        //?????????????????????
                        int todayTotal = data.getXztj().getTotal();
                        setTodayCount(todayTotal);
                        //????????????????????????
                        setBarChart(data.getHztj());
                        //????????????
                        unReadCount = data.getMsgnum();
                        setRedPoint(unReadCount);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {
                        //bleDialogUtils.dismissProgress();
                    }
                });
    }

    private void setReachTheStandRate(IndexBean.SugarBean sugars) {
        int total = sugars.getTotal();
        int empstomach = sugars.getEmpstomach();
        int unempstomach = sugars.getUnempstomach();
        List<ReachTheStandRateBean> list = new ArrayList<>();
        ReachTheStandRateBean bean0 = new ReachTheStandRateBean(total);
        ReachTheStandRateBean bean1 = new ReachTheStandRateBean(empstomach);
        ReachTheStandRateBean bean2 = new ReachTheStandRateBean(unempstomach);
        list.add(bean0);
        list.add(bean1);
        list.add(bean2);
        rvReachTheStandRate.setLayoutManager(new LinearLayoutManager(getPageContext()));
        rvReachTheStandRate.setAdapter(new ReachTheStandRateAdapter(list));
    }

    /**
     * ????????????
     *
     * @param yhtj
     */
    private void setUserCount(IndexBean.YhtjBean yhtj) {
        int gxy = yhtj.getGxy();
        int tnb = yhtj.getTnb();
        int blsg = yhtj.getBlsg();

        SpanUtils.with(tvUserCountGxy)
                .append("?????????").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .appendSpace(15, Color.TRANSPARENT)
                .append(gxy + "").setForegroundColor(ColorUtils.getColor(R.color.home_index_user_count))
                .appendSpace(15, Color.TRANSPARENT)
                .append("???").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .create();


        SpanUtils.with(tvUserCountTnb)
                .append("?????????").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .appendSpace(15, Color.TRANSPARENT)
                .append(tnb + "").setForegroundColor(ColorUtils.getColor(R.color.home_index_user_count))
                .appendSpace(15, Color.TRANSPARENT)
                .append("???").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .create();

        SpanUtils.with(tvUserCountGxyAndTnb)
                .append("?????????????????????????????????").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .appendSpace(15, Color.TRANSPARENT)
                .append(blsg + "").setForegroundColor(ColorUtils.getColor(R.color.home_index_user_count))
                .appendSpace(15, Color.TRANSPARENT)
                .append("???").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .create();
    }

    /**
     * ????????????
     *
     * @param msgnum
     */
    private void setRedPoint(int msgnum) {
        int imUnReadMsgCount = SPStaticUtils.getInt("imUnReadMsgCount");
        int totalUnReadCount = msgnum + imUnReadMsgCount;
        FloatingMagnetView view = FloatingView.get().getView();
        ColorTextView tvRedPoint = view.findViewById(R.id.tv_red_point);
        if (totalUnReadCount > 0) {
            BadgeUtils.setHuaWei(totalUnReadCount);
            BadgeUtils.setVivo(totalUnReadCount);
            tvRedPoint.setVisibility(View.VISIBLE);
            if (totalUnReadCount > 999) {
                tvRedPoint.setText("...");
            } else {
                tvRedPoint.setText(totalUnReadCount + "");
            }
        } else {
            tvRedPoint.setVisibility(View.GONE);
        }
    }


    /**
     * ????????????????????????
     */
    private void setBarChart(IndexBean.HztjBean today) {
        int xtg = today.getXtg();
        int xtd = today.getXtd();
        int xtzc = today.getXtzc();
        int xtwcl = today.getXtwcl();
        int xyg = today.getXyg();
        int xyd = today.getXyd();
        int xyzc = today.getXyzc();
        int xywcl = today.getXywcl();
        //??????????????????
        List<String> names = new ArrayList<>();
        names.add("??????");
        names.add("??????");
        //???????????????????????????
        List<List<IBarData>> data = new ArrayList<>();
        // ?????????????????????
        ArrayList<IBarData> enxd = new ArrayList<>();
        enxd.add(new TestBarData((xtg), "??????"));
        enxd.add(new TestBarData((xtd), "??????"));
        enxd.add(new TestBarData((xtzc), "??????"));
        enxd.add(new TestBarData((xtwcl), "??????"));
        data.add(enxd);

        ArrayList<IBarData> enxy = new ArrayList<>();
        enxy.add(new TestBarData((xyg), "??????"));
        enxy.add(new TestBarData((xyd), "??????"));
        enxy.add(new TestBarData((xyzc), "??????"));
        enxy.add(new TestBarData((xywcl), "??????"));
        data.add(enxy);
        //????????????
        int[] colors = getResources().getIntArray(R.array.chart_colors_local);
        List<Integer> chartColors = new ArrayList<>();
        for (int color : colors) {
            chartColors.add(color);
        }
        //??????y?????????
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setLabelCount(4, false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(xtwcl + 10);

        //??????????????????
        barChart.setTouchEnabled(true);
        new BarChartHelper.Builder()
                .setContext(getActivity())
                // ?????????
                .setBarChart(barChart)
                // ????????????
                .setBarSetData(data)
                // ???????????? ???????????????
                .setLabels(names)
                //??????X???????????????
                .setDisplayCount(4)
                // ??????????????????
                .setLegendEnable(false)
                // ??????????????????
                .setLegendTextSize(16)
                // ??????????????????Y???
                .setyAxisRightEnable(false)
                //X,Y????????????????????????
                .setDrawGridLines(false)
                .setGroupSpace(0.4f)
                .setBarColors(chartColors)
                //X????????????????????????????????????IBarData???????????????
                .setXValueEnable(true)
                .build();
    }

    /**
     * ??????????????????
     *
     * @param todayTotal
     */
    private void setTodayCount(int todayTotal) {
        SpanUtils.with(tvTodayAddTotal)
                .append("????????????").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .appendSpace(15, Color.TRANSPARENT)
                .append(todayTotal + "").setForegroundColor(ColorUtils.getColor(R.color.home_index_user_count_add))
                .appendSpace(15, Color.TRANSPARENT)
                .append("???").setForegroundColor(ColorUtils.getColor(R.color.black_text))
                .create();
    }


    @OnClick({R.id.bar_chart, R.id.img_bar_chart, R.id.tv_start_time, R.id.tv_end_time, R.id.ll_patient_add_today})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.bar_chart:
            case R.id.img_bar_chart:
                //??????????????????
                intent = new Intent(getPageContext(), PatientCountMainActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_start_time:
                PickerUtils.showTime(getPageContext(), new PickerUtils.TimePickerCallBack() {
                    @Override
                    public void execEvent(String content) {
                        toJudgeBeginTime(content);
                    }

                });
                break;
            case R.id.tv_end_time:
                PickerUtils.showTime(getPageContext(), new PickerUtils.TimePickerCallBack() {
                    @Override
                    public void execEvent(String content) {
                        toJudgeEndTime(content);
                    }
                });
                break;
            case R.id.ll_patient_add_today:
                intent = new Intent(getPageContext(), PatientAddTodayListActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * ??????????????????
     *
     * @param content
     */
    private void toJudgeBeginTime(String content) {
        endTime = tvEndTime.getText().toString().trim();
        if (endTime.compareTo(content) < 0) {
            ToastUtils.showShort("????????????" + "??????" + "????????????");
            return;
        }
        tvStartTime.setText(content);
        beginTime = tvStartTime.getText().toString().trim();
        getIndex(beginTime, endTime);
    }

    /**
     * ??????????????????
     *
     * @param content
     */
    private void toJudgeEndTime(String content) {
        beginTime = tvStartTime.getText().toString().trim();
        if (content.compareTo(beginTime) < 0) {
            ToastUtils.showShort("????????????" + "??????" + "????????????");
            return;
        }
        tvEndTime.setText(content);
        endTime = tvEndTime.getText().toString().trim();
        getIndex(beginTime, endTime);
    }





    /**
     * ????????????
     */
    private void toJudgeToDo() {
        int type = SPStaticUtils.getInt("docType");
        if (3 == type) {
            startActivity(new Intent(getActivity(), DoctorListActivity.class));
        } else {
            startActivity(new Intent(getActivity(), ToDoListActivity.class));
            //startActivity(new Intent(getActivity(), TestActivity.class));
        }
    }

    @Override
    protected void receiveEvent(EventMessage event) {
        super.receiveEvent(event);
        switch (event.getCode()) {
            case ConstantParam.EventCode.NEW_PATIENT_OPERATE:
            case ConstantParam.EventCode.APPLY_TO_HOSPITAL:
            case ConstantParam.EventCode.IM_MESSAGE_REFRESH:
                beginTime = "";
                endTime = "";
                getIndex(beginTime, endTime);
                break;
            case ConstantParam.EventCode.IM_UNREAD_MSG_COUNT:
                setRedPoint(unReadCount);
                break;
        }
    }

    /**
     * Fragment ???????????????
     */
    @Override
    public void initImmersionBar() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarDarkFont(true).statusBarColor(R.color.white).init();
    }

    @Override
    public boolean immersionBarEnabled() {
        return true;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mSimpleImmersionProxy.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSimpleImmersionProxy.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSimpleImmersionProxy.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mSimpleImmersionProxy.onHiddenChanged(hidden);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mSimpleImmersionProxy.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment ???????????????
     */
}
