import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * 蓝牙的服务器端。部署在Windows操作系统（PC电脑上）。 等待手机客户端或者其他蓝牙设备的连接。 该代码文件是一个Java
 * SE代码文件。运行于Windows PC上。
 * <p>
 * Java在Windows PC上实现蓝牙，必须依赖两个库：
 * <p>
 * 1，bluecove-2.1.1-SNAPSHOT.jar
 * 下载：http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.63/
 * <p>
 * 2，commons-io-2.6.jar
 * 下载：http://commons.apache.org/proper/commons-io/download_io.cgi
 *
 * @author tq
 */
public class RealBluetoothJavaServer {
    private StreamConnectionNotifier mStreamConnectionNotifier = null;
    private StreamConnection mStreamConnection = null;
    private int threadNum = 0;
    private String[] filenameMod = {"head", "tail"};


    public static void main(String[] args) {
        new RealBluetoothJavaServer();
    }

    public RealBluetoothJavaServer() {
        try {
            // 服务器端的UUID必须和手机端的UUID相一致。手机端的UUID需要去掉中间的-分割符。
            mStreamConnectionNotifier = (StreamConnectionNotifier) Connector
                    .open("btspp://localhost:0000110100001000800000805F9B34FB");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 开启线程读写蓝牙上接收和发送的数据。
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("服务器端开始监听...");
                    while (true) {
                        mStreamConnection = mStreamConnectionNotifier.acceptAndOpen();
                        System.out.println("接受连接");
                        threadNum++;
                        RealtimeChart chart = new RealtimeChart("Real-time Chart (" + threadNum + ")", "X", "Y", "Z", 5000);

                        InputStream is = mStreamConnection.openInputStream();

                        byte[] buffer = new byte[1024];

                        System.out.println("开始读数据...");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
                                    sdf.applyPattern("yyyy-MM-dd-HH-mm-ss");
                                    Date filenameDate = new Date();// 获取当前时间
                                    String filename = "data_" + filenameMod[threadNum % 2] + "_" + sdf.format(filenameDate) + ".csv";
                                    CSVUtils.exportCsv(new File(filename), "acc_x,acc_y,acc_z,latitude,longitude,altitude,date,speed");
                                    // 读数据。
                                    while (is.read(buffer) != -1) {
                                        String s = new String(buffer);
                                        String dataStr = s.substring(s.indexOf(":") + 1, s.indexOf(";"));
                                        CSVUtils.exportCsv(new File(filename), dataStr);
                                        StringTokenizer data = new StringTokenizer(dataStr, ",");
                                        Float[] acc = new Float[3];
                                        Double[] gps = new Double[3];
                                        String date;
                                        float speed;
                                        int index = 0;
                                        while (data.hasMoreTokens()) {
                                            if (index < 3) acc[index] = Float.parseFloat(data.nextToken());
                                            else if (index < 6) gps[index - 3] = Double.parseDouble(data.nextToken());
                                            else if (index == 6) date = data.nextToken();
                                            else {
                                                speed = Float.parseFloat(data.nextToken());
                                                index = 0;
                                            }
                                            index++;
                                        }

                                        chart.plot(acc[0], acc[1], acc[2]);
                                    }

                                    is.close();
                                    mStreamConnection.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }
}