package com.xy.xydoctor.ui.fragment.patientinfo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.ft.analysis.obj.BloodfatData;
import com.ft.analysis.obj.BodyData;
import com.ft.analysis.obj.EcgData;
import com.ft.analysis.obj.GluData;
import com.ft.analysis.obj.NibpData;
import com.ft.analysis.obj.Spo2Data;
import com.ft.analysis.obj.TempData;
import com.ft.analysis.obj.UrineData;
import com.ft.analysis.obj.UserInfo;
import com.ft.analysis.scan.BLEController;
import com.ft.analysis.scan.ExchangeInterface;
import com.lyd.baselib.base.fragment.BaseFragment;
import com.lyd.baselib.util.TurnsUtils;
import com.lyd.baselib.util.ble.BleStatusBroadcastReceiver;
import com.lyd.baselib.util.ble.BleUtils;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.rxjava.rxlife.RxLife;
import com.wei.android.lib.colorview.view.ColorTextView;
import com.xy.xydoctor.R;
import com.xy.xydoctor.bean.GetOnlineBloodBean;
import com.xy.xydoctor.bean.OnlineTestGetBloodSugar;
import com.xy.xydoctor.bean.UserInfoBean;
import com.xy.xydoctor.constant.ConstantParam;
import com.xy.xydoctor.net.ErrorInfo;
import com.xy.xydoctor.net.OnError;
import com.xy.xydoctor.net.XyUrl;
import com.xy.xydoctor.utils.NumberUtils;
import com.xy.xydoctor.utils.StringToHexUtils;
import com.xy.xydoctor.utils.progress.BleDialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.functions.Consumer;
import rxhttp.wrapper.param.RxHttp;

/**
 * ??????: ????????????
 * ??????: LYD
 * ????????????: 2019/7/12 15:44
 */
public class PatientOnlineTestFragment extends BaseFragment implements ExchangeInterface, BleStatusBroadcastReceiver.BleStatusChangeListener {
    private static final String TAG = "xxx";
    private static final int OPEN_BLE = 10086;
    @BindView(R.id.ll_online_test_empty)
    LinearLayout llOnlineTestEmpty;
    @BindView(R.id.tv_get_data)
    ColorTextView tvGetData;
    @BindView(R.id.img_head)
    QMUIRadiusImageView imgHead;
    @BindView(R.id.tv_person_name)
    TextView tvPersonName;
    @BindView(R.id.img_sex)
    ImageView imgSex;
    @BindView(R.id.tv_person_age)
    TextView tvPersonAge;
    @BindView(R.id.tv_height)
    TextView tvHeight;
    @BindView(R.id.tv_weight)
    TextView tvWeight;
    @BindView(R.id.tv_check_time)
    TextView tvCheckTime;
    //????????????
    @BindView(R.id.ll_online_test_blood_sugar)
    LinearLayout llBloodSugar;
    @BindView(R.id.tv_blood_sugar)
    TextView tvBloodSugar;
    @BindView(R.id.img_blood_sugar)
    ImageView imgBloodSugar;
    //????????????
    //????????????
    @BindView(R.id.ll_online_test_blood_oxygen)
    LinearLayout llBloodOxygen;
    @BindView(R.id.tv_blood_oxygen)
    TextView tvBloodOxygen;
    @BindView(R.id.img_blood_oxygen)
    ImageView imgBloodOxygen;
    @BindView(R.id.tv_blood_pulse)
    TextView tvBloodPulse;
    @BindView(R.id.img_blood_pulse)
    ImageView imgBloodPulse;
    //????????????
    //????????????
    @BindView(R.id.ll_online_test_body_fat)
    LinearLayout llBodyFat;
    @BindView(R.id.tv_online_test_weight)
    TextView tvOnlineTestWeight;
    @BindView(R.id.tv_bmi)
    TextView tvBmi;
    @BindView(R.id.img_bmi)
    ImageView imgBmi;
    //????????????
    //????????????
    @BindView(R.id.ll_online_test_blood_pressure)
    LinearLayout llBloodPressure;
    @BindView(R.id.tv_blood_pressure_systolic_pressure)
    TextView tvSystolicPressure;
    @BindView(R.id.img_blood_pressure_systolic_pressure)
    ImageView imgSystolicPressure;
    @BindView(R.id.tv_blood_pressure_diastolic_pressure)
    TextView tvDiastolicPressure;
    @BindView(R.id.img_blood_pressure_diastolic_pressure)
    ImageView imgDiastolicPressure;
    //????????????????????????????????????
    String bCommand1 = "04B0A054";
    //????????????????????????????????????
    String bCommand2 = "04B0A155";
    //????????????
    private Timer timer = new Timer();
    //???????????????????????????
    private BleStatusBroadcastReceiver receiver;
    //??????????????????
    private String height;
    private BleDialogUtils bleDialogUtils;
    private boolean isConnectSuccess;
    //????????????????????????
    private int scanTime = 10 * 1000;
    //????????????????????????????????????(????????????????????????+???????????????????????????+40???)
    private int overTime = ConstantParam.DEFAULT_TIMEOUT + scanTime + 40 * 1000;
    //???????????????
    //????????????
    private String bDeviceName = "ClinkBlood";
    //????????????
    private String bs1 = "0000fc00-0000-1000-8000-00805f9b34fb";
    //?????? ?????????
    private String bs2 = "0000fca1-0000-1000-8000-00805f9b34fb";
    //????????? ?????????
    private String bs3 = "0000fca0-0000-1000-8000-00805f9b34fb";
    //???????????????
    //???????????????
    //????????????
    private String eDeviceName = "SH-eScale";
    //????????????
    private String es1 = "53480001-534d-4152-542d-455343414c45";
    //?????? ?????????
    private String es2 = "53480003-534d-4152-542d-455343414c45";
    //????????? ?????????
    private String es3 = "53480002-534d-4152-542d-455343414c45";
    //????????????(???????????????????????????60$????????????????????????)
    //private String eCommand1 = "313024";
    //??????????????????(???????????????????????????11$???????????????????????????????????????????????????????????????)
    private String eCommand2 = "313124";
    //???????????????

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_patient_online_test;
    }

    @Override
    protected void init(View rootView) {
        setScanRule();
        registerBleStatusReceiver();
        setTopInfo();
        initDialog();
    }


    /**
     * ??????????????????
     */
    private void setScanRule() {
        BleScanRuleConfig scanRuleConfig =
                new BleScanRuleConfig.Builder()
                        //??????????????????????????????????????????
                        .setDeviceName(true, eDeviceName)
                        //????????????autoConnect????????????????????????false
                        .setAutoConnect(false)
                        //????????????????????????????????????10???
                        .setScanTimeOut(scanTime)
                        .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    /**
     * ??????????????????
     */
    private void setTopInfo() {
        String userId = getArguments().getString("userid");
        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", userId);
        RxHttp.postForm(XyUrl.GET_USER_INFO)
                .addAll(map)
                .asResponseList(UserInfoBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<List<UserInfoBean>>() {
                    @Override
                    public void accept(List<UserInfoBean> myTreatPlanBeans) throws Exception {
                        List<UserInfoBean> data = myTreatPlanBeans;
                        UserInfoBean user = data.get(0);
                        if (1 == user.getSex()) {
                            Glide.with(Utils.getApp()).load(user.getPicture()).error(R.drawable.head_man).placeholder(R.drawable.head_man).into(imgHead);
                            imgSex.setImageResource(R.drawable.male);
                        } else {
                            Glide.with(Utils.getApp()).load(user.getPicture()).error(R.drawable.head_woman).placeholder(R.drawable.head_woman).into(imgHead);
                            imgSex.setImageResource(R.drawable.female);
                        }
                        if (TextUtils.isEmpty(user.getNickname())) {
                            tvPersonName.setText(user.getUsername());
                        } else {
                            tvPersonName.setText(user.getNickname());
                        }
                        tvPersonAge.setText(user.getAge() + "???");
                        height = user.getHeight();
                        tvHeight.setText(height + "cm");
                        tvWeight.setText(user.getWeight() + "kg");
                        String checkTime = SPStaticUtils.getString("checkTime");
                        if (TextUtils.isEmpty(checkTime)) {
                            tvCheckTime.setText("??????????????????:" + "-- -- --");
                        } else {
                            tvCheckTime.setText("??????????????????:" + checkTime);
                        }
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }


    /**
     * ?????????Dialog
     */
    private void initDialog() {
        bleDialogUtils = new BleDialogUtils();
    }


    @OnClick(R.id.tv_get_data)
    public void onViewClicked() {
        //PatientOnlineTestFragmentPermissionsDispatcher.obtainAccessFineLocationWithPermissionCheck(this);
        requestLocation();
    }

    private void requestLocation() {
        PermissionUtils
                .permission(PermissionConstants.LOCATION)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        isConnectSuccess = false;
                        getSugarData();
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.showShort(R.string.please_open_gps);
                        Log.e(TAG, "accessFineLocationDenied");
                    }
                }).request();
    }

    /**
     * ????????????????????????
     */
    private void getSugarData() {
        HashMap<String, Object> map = new HashMap<>();
        RxHttp.postForm(XyUrl.GET_ONLINE_TEST_BLOOD_SUGAR)
                .addAll(map)
                .asResponse(OnlineTestGetBloodSugar.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<OnlineTestGetBloodSugar>() {
                    @Override
                    public void accept(OnlineTestGetBloodSugar data) throws Exception {
                        setBloodSugar(data);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {
                        getBloodData();
                    }
                });
    }


    /**
     * ????????????
     */
    private void getBloodData() {
        HashMap<String, Object> map = new HashMap<>();
        RxHttp.postForm(XyUrl.GET_ONLINE_TEST_BLOOD_PRESSURE)
                .addAll(map)
                .asResponse(GetOnlineBloodBean.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<GetOnlineBloodBean>() {
                    @Override
                    public void accept(GetOnlineBloodBean data) throws Exception {
                        setBloodPressure(data);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {
                        BleStart();
                    }
                });

    }

    /**
     * ??????????????????
     */
    private void BleStart() {
        boolean bleState = BleUtils.getBleState();
        if (bleState) {
            dialogStartConnect();
            startScanBpAndAio();
            dialogPostDelay();
        } else {
            BleUtils.openBle(this, OPEN_BLE);
        }
    }

    /**
     * ????????????
     */
    private void dialogStartConnect() {
        bleDialogUtils.showProgress(getActivity(), getString(R.string.ble_connecting));
    }

    /**
     * ????????????
     */
    private void dialogConnectSuccess() {
        bleDialogUtils.dismissProgress();
        bleDialogUtils.mProgressDialog = null;
        bleDialogUtils.showProgress(getActivity(), getString(R.string.ble_connected_and_wait_data));
    }

    /**
     * ????????????
     */
    private void dialogDismiss() {
        bleDialogUtils.dismissProgress();
    }

    /**
     * ????????????
     */
    private void dialogConnectFailed() {
        bleDialogUtils.dismissProgress();
        ToastUtils.showLong(R.string.ble_connect_error);
    }

    /**
     * ????????????
     */
    private void dialogPostDelay() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //????????????????????????????????????
                //Log.e(TAG, "isConnectSuccess: " + isConnectSuccess);
                if (isConnectSuccess == false) {
                    dialogConnectFailed();
                }
            }
        }, overTime);
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void startScanBpAndAio() {
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Log.e(TAG, "onScanStarted");
            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                Log.e(TAG, "onScanFinished");
                //?????????????????????????????????????????????????????????????????????BLE??????????????????????????????????????????????????????
                if (scanResult == null) {
                    Log.e(TAG, "onScanFinished:" + "????????????");
                    startScanYC();
                } else {
                    Log.e(TAG, "onScanFinished:" + "?????????");
                }
            }

            @Override
            public void onStartConnect() {
                Log.e(TAG, "onStartConnect");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                //????????????
                Log.e(TAG, "onConnectFail");
                startScanYC();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                String name = bleDevice.getName();
                Log.e(TAG, "???????????????????????????==" + name);
                openBleNotify(bleDevice, name);
                dialogConnectSuccess();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.e(TAG, "onDisConnected");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.e(TAG, "onScanning");
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param bleDevice
     * @param name
     */
    private void openBleNotify(BleDevice bleDevice, String name) {
        //????????????
        String uuidService = "";
        //???????????? ????????????
        String uuidNotify = "";
        if (bDeviceName.equals(name)) {
            uuidService = bs1;
            uuidNotify = bs2;
        } else if (eDeviceName.equals(name)) {
            uuidService = es1;
            uuidNotify = es2;
        }
        BleManager.getInstance().notify(
                bleDevice, uuidService, uuidNotify,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "??????????????????:" + "onNotifySuccess");
                                startTestStepOne(bleDevice, name);
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "data==" + data);
                                StringBuilder stringBuilder = new StringBuilder();
                                String convertString = ConvertUtils.bytes2HexString(data);
                                stringBuilder.append(convertString);
                                String allString = stringBuilder.toString();
                                Log.e(TAG, "???????????????:" + allString);
                                if (bDeviceName.equals(name)) {
                                    //??????
                                    if (allString.contains("070BBD")) {
                                        isConnectSuccess = true;
                                        String bloodPressureString = allString.substring(allString.indexOf("080BB8") + 8, allString.indexOf("080BB8") + 12);
                                        //16?????? ????????? ?????????
                                        //Log.e(TAG, "bloodPressureString==" + bloodPressureString);
                                        //setBloodPressure(bloodPressureString);
                                        showdownBle(bleDevice);
                                    }
                                } else if (eDeviceName.equals(name)) {
                                    //?????????????????? 313124
                                    if (!"313124".equals(allString)) {
                                        BodyData bodyData = new BodyData();
                                        setBodyFat(bodyData, allString);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    /**
     * ??????????????????????????????
     *
     * @param bleDevice
     * @param name
     */
    private void startTestStepOne(BleDevice bleDevice, String name) {
        //????????????
        String uuidService = "";
        //???????????? ????????????
        String uuidWrite = "";
        //??????
        String command = "";
        if (bDeviceName.equals(name)) {
            uuidService = bs1;
            uuidWrite = bs3;
            command = bCommand1;
        } else if (eDeviceName.equals(name)) {
            uuidService = es1;
            uuidWrite = es3;
            command = eCommand2;
        }
        BleManager.getInstance().write(
                bleDevice, uuidService, uuidWrite,
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bDeviceName.equals(name)) {
                                    Log.e(TAG, "?????????????????????" + "onWriteSuccess");
                                    startTestStepTwo(bleDevice, name);
                                }
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "?????????????????????" + "onWriteFailure");
                            }
                        });
                    }
                });
    }

    /**
     * ??????????????????????????????
     *
     * @param bleDevice
     * @param name
     */
    private void startTestStepTwo(BleDevice bleDevice, String name) {
        //????????????
        String uuidService = "";
        //???????????? ????????????
        String uuidWrite = "";
        //??????
        String command = "";
        if (bDeviceName.equals(name)) {
            uuidService = bs1;
            uuidWrite = bs3;
            command = bCommand2;
        }
        BleManager.getInstance().write(
                bleDevice, uuidService, uuidWrite,
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "?????????????????????:" + "onWriteSuccess");
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "?????????????????????:" + "onWriteFailure");
                            }
                        });
                    }
                });
    }

    /**
     * ??????
     *
     * @param bleDevice
     */
    private void showdownBle(BleDevice bleDevice) {
        //??????????????????????????????
        String hex2 = "04B0A65A";
        //????????????
        String s0 = "0000fc00-0000-1000-8000-00805f9b34fb";
        //???????????? ?????????
        String s1 = "0000fca0-0000-1000-8000-00805f9b34fb";
        BleManager.getInstance().write(
                bleDevice, s0, s1,
                HexUtil.hexStringToBytes(hex2),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "??????" + "onWriteSuccess");
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "??????" + "onWriteFailure");
                            }
                        });
                    }
                });
    }

    /**
     * ?????????????????????????????????
     */
    private void startScanYC() {
        BLEController bleController = new BLEController(getPageContext(), this, ExchangeInterface.USING_TYPE_FULL);
        bleController.startScan();
    }


    //??????????????????

    /**
     * ????????????
     *
     * @param bean
     */
    private void setBloodSugar(OnlineTestGetBloodSugar bean) {
        dialogDismiss();
        //????????????
        llOnlineTestEmpty.setVisibility(View.GONE);
        llBloodSugar.setVisibility(View.VISIBLE);
        llBloodOxygen.setVisibility(View.GONE);
        llBodyFat.setVisibility(View.GONE);
        llBloodPressure.setVisibility(View.GONE);
        //????????????
        //OnlineTestGetBloodSugar data = (OnlineTestGetBloodSugar) baseBean.getData();
        //????????????
        long datetime = bean.getDatetime();
        String checkTime = TimeUtils.millis2String(datetime * 1000L);
        tvCheckTime.setText("??????????????????:" + checkTime);
        double glucosevalue = bean.getGlucosevalue();
        tvBloodSugar.setText(glucosevalue + "mol/L");
        //??????4.4~10.0
        if (glucosevalue > 10.0) {
            imgBloodSugar.setImageResource(R.drawable.online_test_blood_sugar_high);
        } else if (glucosevalue > 4.4) {
            imgBloodSugar.setImageResource(R.drawable.online_test_blood_sugar_common);
        } else {
            imgBloodSugar.setImageResource(R.drawable.online_test_blood_sugar_low);
        }
        uploadBloodSugar(bean);
    }

    /**
     * ??????????????????
     *
     * @param bean
     */
    private void setBloodPressure(GetOnlineBloodBean bean) {

        dialogDismiss();
        //????????????
        llOnlineTestEmpty.setVisibility(View.GONE);
        llBloodSugar.setVisibility(View.GONE);
        llBloodOxygen.setVisibility(View.GONE);
        llBodyFat.setVisibility(View.GONE);
        llBloodPressure.setVisibility(View.VISIBLE);
        String systolic = bean.getSystolic();
        String diastole = bean.getDiastole();
        String checkTime = bean.getDatetime();
        //????????????
        if (!TextUtils.isEmpty(systolic) && !TextUtils.isEmpty(diastole)) {
            tvSystolicPressure.setText(systolic + "mmHg");
            tvDiastolicPressure.setText(diastole + "mmHg");
            double high = TurnsUtils.getDouble(systolic, 0);
            double low = TurnsUtils.getDouble(diastole, 0);
            //?????????90~140
            if (high > 140) {
                imgSystolicPressure.setImageResource(R.drawable.online_test_blood_pressure_high);
            } else if (high > 90) {
                imgSystolicPressure.setImageResource(R.drawable.online_test_blood_pressure_common);
            } else {
                imgSystolicPressure.setImageResource(R.drawable.online_test_blood_pressure_low);
            }
            //?????????60~90
            if (low > 90) {
                imgDiastolicPressure.setImageResource(R.drawable.online_test_blood_pressure_high);
            } else if (low > 60) {
                imgDiastolicPressure.setImageResource(R.drawable.online_test_blood_pressure_common);
            } else {
                imgDiastolicPressure.setImageResource(R.drawable.online_test_blood_pressure_low);
            }
            tvCheckTime.setText("??????????????????:" + checkTime);
            //????????????
            uploadBloodPressure(high, low, checkTime);
        }
    }

    /**
     * ????????????
     *
     * @param spo2Data
     */
    private void setBloodOxygen(Spo2Data spo2Data) {
        dialogDismiss();
        //isConnectSuccess = true;
        int spo2 = spo2Data.getSpo2();//???????????????
        int pr = spo2Data.getPr();//?????????
        long systemTime = spo2Data.getSystemTime();
        llOnlineTestEmpty.setVisibility(View.GONE);
        llBloodSugar.setVisibility(View.GONE);
        llBloodOxygen.setVisibility(View.VISIBLE);
        llBodyFat.setVisibility(View.GONE);
        llBloodPressure.setVisibility(View.GONE);
        //????????????
        String checkTime = TimeUtils.millis2String(systemTime);
        tvCheckTime.setText("??????????????????:" + checkTime);
        //????????????????????????95~99????????????95???????????????
        tvBloodOxygen.setText(spo2 + "%");
        if (spo2 < 95) {
            imgBloodOxygen.setImageResource(R.drawable.online_test_blood_oxygen_abnormal);
        } else {
            imgBloodOxygen.setImageResource(R.drawable.online_test_blood_oxygen_common);
        }
        //??????60~100??????????????????60??????????????????100?????????.
        tvBloodPulse.setText(pr + "bmp");
        if (pr > 100) {
            imgBloodPulse.setImageResource(R.drawable.online_test_blood_pulse_high);
        } else if (pr > 60) {
            imgBloodPulse.setImageResource(R.drawable.online_test_blood_pulse_common);
        } else {
            imgBloodPulse.setImageResource(R.drawable.online_test_blood_pulse_low);
        }
        uploadBloodOxygen(spo2Data);
    }

    /**
     * ??????????????????
     *
     * @param bodyData
     */
    private void setBodyFat(BodyData bodyData, String resultHex) {
        dialogDismiss();
        llOnlineTestEmpty.setVisibility(View.GONE);
        llBloodSugar.setVisibility(View.GONE);
        llBloodOxygen.setVisibility(View.GONE);
        llBodyFat.setVisibility(View.VISIBLE);
        llBloodPressure.setVisibility(View.GONE);
        String weight = "";
        if (TextUtils.isEmpty(resultHex)) {
            //?????????????????????
            weight = bodyData.getWeight() + "";
        } else {
            //???????????????
            //resultHex = "573A3037342E3620483A3135382E350D0A";
            resultHex = StringToHexUtils.hexStr2Str(resultHex);
            String[] resultArray = resultHex.split("\\s+");
            if (2 == resultArray.length) {
                String heightTotal = resultArray[1];
                String weightTotal = resultArray[0];
                height = heightTotal.substring(heightTotal.indexOf(":") + 1);
                weight = weightTotal.substring(weightTotal.indexOf(":") + 1);
                if (weight.startsWith("0")) {
                    weight = weight.substring(weight.indexOf("0") + 1);
                }
            }
        }
        //????????????
        tvOnlineTestWeight.setText(weight + "kg");
        tvWeight.setText(weight + "kg");
        double weightDouble = TurnsUtils.getDouble(weight, 0);
        double doubleHeightM = TurnsUtils.getDouble(height, 0) / 100;
        double bmi = weightDouble / (doubleHeightM * doubleHeightM);
        double bmiOneBit = NumberUtils.saveOneBitOneRound(bmi);
        tvBmi.setText(bmiOneBit + "");
        //bmi18.5~23.9
        if (bmi > 23.9) {
            imgBmi.setImageResource(R.drawable.body_fat_high);
        } else if (bmi > 18.5) {
            imgBmi.setImageResource(R.drawable.body_fat_common);
        } else {
            imgBmi.setImageResource(R.drawable.body_fat_low);
        }
        //????????????
        String checkTime = TimeUtils.getNowString();
        tvCheckTime.setText("??????????????????:" + checkTime);
        //????????????
        uploadBodyFat(weight, checkTime);
    }

    //??????????????????
    //??????????????????

    /**
     * ????????????
     *
     * @param bean
     */
    private void uploadBloodSugar(OnlineTestGetBloodSugar bean) {
        String userid = getArguments().getString("userid");
        String glucosevalue = bean.getGlucosevalue() + "";
        String category = bean.getCategory() + "";
        long datetime = bean.getDatetime();
        String checkTime = TimeUtils.millis2String(datetime * 1000L);
        SPStaticUtils.put("checkTime", checkTime);
        String type = bean.getType() + "";
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", userid);
        map.put("docsugar", "1");
        map.put("glucosevalue", glucosevalue);
        map.put("category", category);
        map.put("datetime", checkTime);
        map.put("type", type);
        RxHttp.postForm(XyUrl.ADD_BLOOD_SUGAR)
                .addAll(map)
                .asResponse(String.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        ToastUtils.showShort(R.string.ble_data_upload_success);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });

    }

    /**
     * ????????????
     *
     * @param high
     * @param low
     * @param checkTime
     */
    private void uploadBloodPressure(double high, double low, String checkTime) {
        String userid = getArguments().getString("userid");
        SPStaticUtils.put("checkTime", checkTime);
        HashMap map = new HashMap<>();
        map.put("uid", userid);
        map.put("systolic", high);//??????
        map.put("diastole", low);//??????
        map.put("datetime", checkTime);
        map.put("type", "1");
        RxHttp.postForm(XyUrl.ADD_BLOOD)
                .addAll(map)
                .asResponse(String.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        ToastUtils.showShort(R.string.ble_data_upload_success);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }

    /**
     * ????????????
     *
     * @param spo2Data
     */
    private void uploadBloodOxygen(Spo2Data spo2Data) {
        String userid = getArguments().getString("userid");
        int spo2 = spo2Data.getSpo2();//???????????????
        int pr = spo2Data.getPr();//?????????
        long systemTime = spo2Data.getSystemTime();
        String checkTime = TimeUtils.millis2String(systemTime);
        SPStaticUtils.put("checkTime", checkTime);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", userid);
        map.put("oxygen", spo2 + "");
        map.put("bpmval", pr + "");
        map.put("type", 1);
        map.put("datetime", checkTime);
        RxHttp.postForm(XyUrl.ADD_BLOOD_OXYGEN)
                .addAll(map)
                .asResponse(String.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        ToastUtils.showShort(R.string.ble_data_upload_success);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }

    /**
     * ??????????????????
     *
     * @param
     */
    private void uploadBodyFat(String weight, String checkTime) {
        String userid = getArguments().getString("userid");
        SPStaticUtils.put("checkTime", checkTime);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", userid);
        map.put("height", height);
        map.put("weight", weight);
        map.put("datetime", checkTime);
        map.put("type", 1);
        RxHttp.postForm(XyUrl.ADD_BMI)
                .addAll(map)
                .asResponse(String.class)
                .to(RxLife.toMain(this))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        ToastUtils.showShort(R.string.ble_data_upload_success);
                    }
                }, new OnError() {
                    @Override
                    public void onError(ErrorInfo error) throws Exception {

                    }
                });
    }
    //??????????????????

    //YC BLE????????????
    @Override
    public void onStateResponed(int i) {
        Log.e(TAG, "onStateResponed" + i);
        //        switch (i) {
        //            case 6://????????????
        //                break;
        //            case 3://??????????????????
        //                break;
        //            case 4://??????????????????
        //                break;
        //            case 9://????????????
        //                break;
        //            case 16://??????????????????
        //                break;
        //        }
        if (6 == i || 3 == i) {

        } else if (4 == i) {
            dialogConnectSuccess();
        } else if (16 == i) {
            isConnectSuccess = true;
            dialogDismiss();
        }
    }


    /**
     * ???????????????
     *
     * @param spo2Data
     */

    @Override
    public void onSpo2Changed(Spo2Data spo2Data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "??????????????????");
                setBloodOxygen(spo2Data);
            }
        });
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param bodyData
     */
    @Override
    public void onBodyChanged(BodyData bodyData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "??????????????????");
                setBodyFat(bodyData, "");
            }
        });
    }


    /*** ??????????????????????????????????????????????????????????????????????????????????????????????????????30????????????????????????????????????
     * @param arrayList ??????????????????????????????????????????
     * @param workingmode ????????????30????????????????????????
     */
    @Override
    public void onEcgWaveChanged(ArrayList<Integer> arrayList, int workingmode, int hr, int state) {

    }

    /***???????????????????????????????????????
     * @param ecgData ????????????
     */
    @Override
    public void onEcgChanged(EcgData ecgData) {

    }

    /**
     * ??????
     *
     * @param gluData
     */
    @Override
    public void onGluChanged(GluData gluData) {

    }

    @Override
    public UserInfo getUserInfo() {
        return null;
    }

    /**
     * ??????
     *
     * @param nibpData
     */
    @Override
    public void onNibpChanged(NibpData nibpData) {

    }

    /**
     * ???11???????????????????????????????????????
     *
     * @param urineData ??????????????????
     */
    @Override
    public void onUrineChanged(UrineData urineData) {

    }


    /**
     * ??????
     *
     * @param bloodfatData
     */
    @Override
    public void onBFChanged(BloodfatData bloodfatData) {

    }

    /**
     * ??????
     *
     * @param tempData
     */
    @Override
    public void onTempChanged(TempData tempData) {

    }

    /**
     * ??????Ble-Yc??????
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //if (bleController != null) {
        //    bleController.onDestroy();
        //}
        unregisterBleStatusReceiver();
    }

    //YC BLE????????????


    //????????????


    //??????????????????

    /**
     * ???????????????????????????
     */
    private void registerBleStatusReceiver() {
        if (receiver == null) {
            receiver = new BleStatusBroadcastReceiver(this);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * ??????????????????
     */
    private void unregisterBleStatusReceiver() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public void onBleClose() {
        //??????????????????
        cancelTask();
        //???????????????
        dialogDismiss();
        ToastUtils.showShort("?????????????????????,???????????????");
    }

    //??????????????????

    /**
     * ??????????????????
     */
    private void cancelTask() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * ?????????????????????
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_BLE) {
            switch (resultCode) {
                case Activity.RESULT_OK://????????????
                    //Log.e(TAG, "????????????");
                    dialogStartConnect();
                    startScanBpAndAio();
                    //????????????????????????
                    dialogPostDelay();
                    break;
                case Activity.RESULT_CANCELED://????????????
                    //Log.e(TAG, "????????????");
                    ToastUtils.showShort("???????????????");
                    break;
            }
        }
    }
}
