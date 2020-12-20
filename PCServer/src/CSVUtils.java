import java.io.*;

/**
 * CSV操作(读取和写入)
 *
 * @author tq
 * @version 2020-11-17
 */
public class CSVUtils {

    /**
     * 读取
     *
     * @param file     csv文件(路径+文件名)，csv文件不存在会自动创建
     * @param data 数据
     * @return
     */
    public static boolean exportCsv(File file, String data) {
        boolean isSucess = false;

        FileOutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(file, true);
            osw = new OutputStreamWriter(out);
            bw = new BufferedWriter(osw);
            bw.append(data).append("\r");
            isSucess = true;
        } catch (Exception e) {
            isSucess = false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                    osw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSucess;
    }
}