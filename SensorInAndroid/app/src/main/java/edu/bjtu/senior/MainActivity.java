package edu.bjtu.senior;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {
    // layout
    private TextView accelerometerView;
    private ListView devices;
    private ListView unDevices;
    private TextView xTextView;
    private TextView yTextView;
    private TextView zTextView;
    private TextView latTextView;
    private TextView lonTextView;
    private TextView altTextView;
    private TextView timeTextView;
    private TextView speedTextView;

    // sensor
    private SensorManager sensorManager;
    private MySensorEventListener sensorEventListener;

    // bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private List<String> bluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private List<String> unBluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> unArrayAdapter;

    //要连接的目标蓝牙设备。
    private String target_device_name = "HUAWEI MatePad Pro"; // PC name
    private final String TAG = "蓝牙调试";
    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectedThread mConnectedThread;

    private BluetoothDevice device;
    private UnpairedDevice unpairedDevice;

    ArrayList<BluetoothDevice> mpairedDevices = new ArrayList<BluetoothDevice>();
    ArrayList<BluetoothDevice> unpairedDevices = new ArrayList<BluetoothDevice>();


    // gps
//    private LocationManager locationManager;
//    String pNet = Manifest.permission.ACCESS_FINE_LOCATION;
//    String pGPS = Manifest.permission.ACCESS_COARSE_LOCATION;
    //Can only use lower 16 bits for requestCode
//    final int GET_LOCATION_REQUEST_CODE = 123;
//    AlertDialog.Builder mHintDialog;
    //    private Location location;
    private LocationUtil.MLocation mBaseLocation;

    // data
    private String xyz;
    private int counter = 0;
    private double latitude;
    private double longitude;
    private double height;
    private float speed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout
//        mHintDialog = new AlertDialog.Builder(this).setTitle("提示");
//        accelerometerView = (TextView) this.findViewById(R.id.accelerometerView);
        xTextView = (TextView) this.findViewById(R.id.x);
        yTextView = (TextView) this.findViewById(R.id.y);
        zTextView = (TextView) this.findViewById(R.id.z);

        latTextView = (TextView) this.findViewById(R.id.lat);
        lonTextView = (TextView) this.findViewById(R.id.lon);
        altTextView = (TextView) this.findViewById(R.id.alt);
        timeTextView = (TextView) this.findViewById(R.id.time);
        speedTextView = (TextView) this.findViewById(R.id.speed);

        devices = (ListView) findViewById(R.id.lv_devices);
        unDevices = (ListView) findViewById(R.id.unpaired_devices);

        // sensor
        sensorEventListener = new MySensorEventListener();
        // Get sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // gps
        LocationUtil locationUtil = LocationUtil.getInstance(MainActivity.this);
        locationUtil.startMonitor();
        mBaseLocation = locationUtil.getBaseLocation();
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (shouldShowRequest()) {
//                //被拒绝了一次
//                mHintDialog.setMessage("被拒绝过了,但是还是可以申请权限")//
//                        .setPositiveButton("确定", new RequestPermissionsClick())//
//                        .show();
//            } else {
//                //第一次申请权限
//                //被无限拒绝
//                //都会到这里来
//                mHintDialog.setMessage("我想要个权限申请权限")//
//                        .setPositiveButton("确定", new RequestPermissionsClick())//
//                        .show();
//            }
//        }
////        else {
////            mHintDialog.setMessage("你已经拥有权限了，不用瞎申请了")//
////                    .setPositiveButton("确定", null)//
////                    .show();
////        }
//
//        //创建一个criteria对象
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//        //设置不需要获取海拔方向数据
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        //设置允许产生资费
//        criteria.setCostAllowed(true);
//        //要求低耗电
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        String provider = locationManager.getBestProvider(criteria, true);
//        if (provider == null) {
//            List<String> providers = locationManager.getProviders(true);
//            if (providers.size() > 0) {
//                provider = providers.get(0);
//            }
//        }
//
//        location = locationManager.getLastKnownLocation(provider);
////        location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        locationManager.requestLocationUpdates(
//                LocationManager.PASSIVE_PROVIDER,      //GPS定位提供者
//                0,       //更新数据时间为1秒
//                0,      //位置间隔为1米
//                //位置监听器
//                new LocationListener() {  //GPS定位信息发生改变时触发，用于更新位置信息
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        //GPS信息发生改变时，更新位置
//
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                    }
//
//                    @Override
//                    //位置状态发生改变时触发
//                    public void onStatusChanged(String provider, int status, Bundle extras) {
//                    }
//
//                    @Override
//                    //定位提供者启动时触发
//                    public void onProviderEnabled(String provider) {
//                    }
//
//                    @Override
//                    //定位提供者关闭时触发
//                    public void onProviderDisabled(String provider) {
//                    }
//                });


        // bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();

        device = getPairedDevices();

//        if (device == null) {
//            // 注册广播接收器。
//            // 接收蓝牙发现讯息。
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filter);
        bluetoothAdapter.startDiscovery();
//
//            if (bluetoothAdapter.startDiscovery()) {
//                Log.d(TAG, "启动蓝牙扫描设备...");
//            }
//        }


        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, bluetoothDevices);
        devices.setAdapter(arrayAdapter);
        devices.setOnItemClickListener((AdapterView.OnItemClickListener) this);

        unArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, unBluetoothDevices);
        unDevices.setAdapter(unArrayAdapter);
        unpairedDevice = new UnpairedDevice();
        unDevices.setOnItemClickListener((AdapterView.OnItemClickListener) unpairedDevice);

        @SuppressLint("CutPasteId") Button enableButton = (Button) this.findViewById(R.id.firstButton);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.enable();
            }
        });

        @SuppressLint("CutPasteId") Button disableButton = (Button) this.findViewById(R.id.thirdButton);
        disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.disable();
            }
        });

        @SuppressLint("CutPasteId") Button searchButton = (Button) this.findViewById(R.id.secondButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                //开启搜索
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver, filter);

                bluetoothAdapter.startDiscovery();
            }
        });
    }

//    private boolean shouldShowRequest() {
//        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
//    }
//
//    private class RequestPermissionsClick implements DialogInterface.OnClickListener {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            //这里是不提倡的写法
//            String[] p = {pNet, pGPS};
//            ActivityCompat.requestPermissions(MainActivity.this, p, GET_LOCATION_REQUEST_CODE);
//        }
//    }

    private BluetoothDevice getPairedDevices() {
        // 获得和当前Android已经配对的蓝牙设备。
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            // 遍历
            for (BluetoothDevice device : pairedDevices) {
                mpairedDevices.add(device);
                bluetoothDevices.add(device.getName() + ":" + device.getAddress());
                // 把已经取得配对的蓝牙设备名字和地址打印出来。
                Log.d(TAG, device.getName() + " : " + device.getAddress());
                if (TextUtils.equals(target_device_name, device.getName())) {
                    Log.d(TAG, "已配对目标设备 -> " + target_device_name);
                    return device;
                }
            }
        }

        return null;
    }

    // 广播接收发现蓝牙设备。
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();
                if (name != null) {
                    Log.d(TAG, "发现设备:" + name);
                    if (!bluetoothDevices.contains(device.getName() + ":" + device.getAddress()) && !unBluetoothDevices.contains(device.getName() + ":" + device.getAddress())) {
                        unBluetoothDevices.add(device.getName() + ":" + device.getAddress());
                        unpairedDevices.add(device);
                        unArrayAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        device = mpairedDevices.get(position);
        String s = arrayAdapter.getItem(position);
        target_device_name = s.substring(0, s.indexOf(":"));
        Log.d(TAG, device.getAddress());
        new ClientThread(device).start();
    }

    private class ClientThread extends Thread {
        private BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));

                Log.d(TAG, "连接服务端...");
                socket.connect();
                Log.d(TAG, "连接建立.");

                // 开始往服务器端发送数据。
                sendDataToServer(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendDataToServer(BluetoothSocket socket) {
            try {
                OutputStream os = socket.getOutputStream();
                String tmp = "";
                while (true) {
                    if (!tmp.equals(xyz)) {
                        os.write(xyz.getBytes());
                        os.flush();
                        tmp = xyz;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class UnpairedDevice implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Toast toast = Toast.makeText(getApplicationContext(),
//                    "设备未连接！", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
            device = unpairedDevices.get(position);
            bluetoothDevices.add(device.getName() + ":" + device.getAddress());
            unBluetoothDevices.remove(position);
            arrayAdapter.notifyDataSetChanged();
            unArrayAdapter.notifyDataSetChanged();
//            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(device.getAddress());
            ConnectThread connect = new ConnectThread(device);
            connect.start();
        }
    }

    @Override
    protected void onResume() {
        // Get accelerometer sensor
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    private final class MySensorEventListener implements SensorEventListener {
        // Get the real time value from the sensor
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];

                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
                Date date = new Date(System.currentTimeMillis());

                counter++;

                latitude = mBaseLocation.latitude;
                longitude = mBaseLocation.longitude;
                height = mBaseLocation.altitude;
                speed = mBaseLocation.speed;

                xTextView.setText("   x：" + x + "   ");
                yTextView.setText("   y：" + y + "   ");
                zTextView.setText("   z：" + z + "   ");

                latTextView.setText(String.valueOf(latitude));
                lonTextView.setText(String.valueOf(longitude));
                altTextView.setText(String.valueOf(height));
                timeTextView.setText(simpleDateFormat.format(date));
                speedTextView.setText(String.valueOf(speed));

                xyz = "data:" + x + "," + y + "," + z + "," + latitude + "," + longitude + "," + height + "," + simpleDateFormat.format(date) + "," + speed + ";";//维度 经度
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

    // Stop the capture of the sensor
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

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

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            view_data.setText(incomingMessage);
                        }
                    });
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
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
}