package edu.bjtu.transbetweenandroid;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import org.achartengine.GraphicalView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import edu.bjtu.transbetweenandroid.jama.Matrix;
import edu.bjtu.transbetweenandroid.jkalman.JKalman;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "蓝牙调试";
    //    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private ConnectedThread mConnectedThread;

    private LinearLayout mLeftCurveLayout;//存放左图表的布局容器
    private LinearLayout mRightCurveLayout;//存放右图表的布局容器
    private GraphicalView mView, mView2;//左右图表
    private ChartService mService, mService2;
    private Float[][] acc = new Float[2][3];
    private Float[][] origin = new Float[2][3];
    private int threadNum = -1;
    private boolean twoThreads = false;

    private String[] filenameMod = {"head", "tail"};
    private int[] color = {0xAAFF0000, 0xAA00FF00};
    private LatLng[] latLngs = new LatLng[2];

    private MapView mapView;
    private BaiduMap map;

    private JKalman mFilter;
    private Matrix mPredictValue;
    private Matrix mCorrectedValue;
    private Matrix mMeasurementValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                acc[i][j] = (float) 0.0;
            }
        }

        for (int i = 0; i < 2; i++) {
            latLngs[i] = new LatLng(0.0, 0.0);

        }

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

//        initial();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();


        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mLeftCurveLayout = (LinearLayout) findViewById(R.id.left_temperature_curve);
        mRightCurveLayout = (LinearLayout) findViewById(R.id.right_temperature_curve);

        mService = new ChartService(this);
        mService.setXYMultipleSeriesDataset("Car Head");
        mService.setXYMultipleSeriesRenderer(100, 15, "Head Acc", "t", "a",
                Color.BLACK, Color.RED, Color.RED, Color.BLACK);
        mView = mService.getGraphicalView();

        mService2 = new ChartService(this);
        mService2.setXYMultipleSeriesDataset("Car Tail");
        mService2.setXYMultipleSeriesRenderer(100, 15, "Tail Acc", "t", "a",
                Color.BLACK, Color.RED, Color.RED, Color.BLACK);
        mView2 = mService2.getGraphicalView();

        //将左右图表添加到布局容器中
        mLeftCurveLayout.addView(mView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mRightCurveLayout.addView(mView2, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();

        mapView = (MapView) findViewById(R.id.mapView);
        map = mapView.getMap();
//        116.350954,39.957067
        LatLng cenpt = new LatLng(39.957423, 116.350878);  //设定中心点坐标

        MapStatus mMapStatus = new MapStatus.Builder()//定义地图状态
                .target(cenpt)
                .zoom(18)
                .build();  //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        map.setMapStatus(mMapStatusUpdate);//改变地图状态
    }

    private double t = 0;
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            mService.updateChart(t, acc[0][0], acc[0][1], acc[0][2]);
            if (twoThreads) mService2.updateChart(t, acc[1][0], acc[1][1], acc[1][2]);
            else if (acc[1][0] != 0 && acc[1][1] != 0 && acc[1][2] != 0) {
                twoThreads = true;
                mService2.updateChart(t, acc[1][0], acc[1][1], acc[1][2]);
            }
            t += 0.25;
        }
    };


    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        Log.d(TAG, "manageMyConnectedSocket: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            int num = threadNum + 1;
            threadNum += 1;

            SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
            sdf.applyPattern("yyyy-MM-dd-HH-mm-ss");
            Date filenameDate = new Date();// 获取当前时间
            String filename = "data_" + filenameMod[num % 2] + "_" + sdf.format(filenameDate) + ".csv";
            WriteData2CSVThread csvThread = new WriteData2CSVThread("ori_x,ori_y,ori_z,acc_x,acc_y,acc_z,latitude,longitude,altitude,date,speed", "oppo & mi car \uD83D\uDD4A pro.", filename);
            csvThread.run();

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
//                    Log.d(TAG, "InputStream: " + incomingMessage);

                    String dataStr = incomingMessage.substring(incomingMessage.indexOf(":") + 1, incomingMessage.indexOf(";"));
//                            CSVUtils.exportCsv(new File(filename), dataStr);
                    WriteData2CSVThread innerCsvThread = new WriteData2CSVThread(dataStr, "oppo & mi car \uD83D\uDD4A pro.", filename);
                    innerCsvThread.run();
                    StringTokenizer data = new StringTokenizer(dataStr, ",");

                    Double[] gps = new Double[3];
                    String date;
                    float speed;
                    int index = 0;
                    while (data.hasMoreTokens()) {
                        if (index < 3) {
                            acc[num][index] = Float.parseFloat(data.nextToken());
//                            origin[num][index] = Float.parseFloat(data.nextToken());
//                            acc[num][index] = (float) KalmanFilter(origin[num][index]);
                        } else if (index < 6)
                            gps[index - 3] = Double.parseDouble(data.nextToken());
                        else if (index == 6) date = data.nextToken();
                        else {
                            speed = Float.parseFloat(data.nextToken());
                            index = 0;
                        }
                        index++;
                    }
//                    WriteData2CSVThread csvThread = new WriteData2CSVThread("ori_x,ori_y,ori_z,acc_x,acc_y,acc_z,latitude,longitude,altitude,date,speed", "oppo & mi car \uD83D\uDD4A pro.", filename);
//                    csvThread.run();


                    //构建折线点坐标
                    LatLng ll = new LatLng(gps[0], gps[1]);
                    if (latLngs[num].latitude == 0.0 && latLngs[num].longitude == 0.0)
                        latLngs[num] = ll;
                    else {
                        List<LatLng> points = new ArrayList<LatLng>();
                        points.add(latLngs[num]);
                        points.add(ll);
                        //设置折线的属性
                        OverlayOptions mOverlayOptions = new PolylineOptions()
                                .width(7)
                                .color(color[num])
                                .points(points);
                        //在地图上绘制折线
                        //mPloyline 折线对象
                        Overlay mPolyline = map.addOverlay(mOverlayOptions);
                        latLngs[num] = ll;
                    }


                    handler.sendMessage(handler.obtainMessage());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("appname", MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    //mmServerSocket.close();
//                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    //初始化卡尔曼滤波参数
    private void initial() {
        //dynam_params:测量矢量维数,measure_params:状态矢量维数
        try {
            mFilter = new JKalman(1, 1);
            double x = 0;
            mPredictValue = new Matrix(1, 1);
            mCorrectedValue = new Matrix(1, 1);
            mMeasurementValue = new Matrix(1, 1);
            mMeasurementValue.set(0, 0, x);     //初始状态（随便设置，不会影响后面的结果）

            double[][] tr = {{1}};   //转移矩阵
            mFilter.setTransition_matrix(new Matrix(tr));

            double[][] Q = {{0.00002}};
            mFilter.setProcess_noise_cov(new Matrix(Q));  //预测噪声协方差矩阵

            double[][] P = {{0.0004}};
            mFilter.setMeasurement_noise_cov(new Matrix(P));  //测量噪声协方差矩阵
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //返回预测修正后的数据
    public double KalmanFilter(double oldValue) {
        mPredictValue = mFilter.Predict();
        mMeasurementValue.set(0, 0, oldValue);
        mCorrectedValue = mFilter.Correct(mMeasurementValue);
        return mCorrectedValue.get(0, 0);
    }


}