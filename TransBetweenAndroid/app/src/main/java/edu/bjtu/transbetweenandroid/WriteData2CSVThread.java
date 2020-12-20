package edu.bjtu.transbetweenandroid;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class WriteData2CSVThread extends Thread {
    private static final String FILE_FOLDER =
           "data/data/edu.bjtu.transbetweenandroid"
                    + File.separator;

    String data;
    String fileName;
    String folder;
    StringBuilder sb;

    public WriteData2CSVThread(String data, String folder, String fileName) {
        this.data = data;
        this.folder = FILE_FOLDER + folder;
        this.fileName = fileName;
    }

    private void createFolder() {
        File fileDir = new File(folder);
        boolean hasDir = fileDir.exists();
//        Log.d("File",folder);
        if (!hasDir) {
            fileDir.mkdirs();// 这里创建的是目录
        }
    }

    @Override
    public void run() {
        super.run();
        createFolder();
        File eFile = new File(folder + File.separator + fileName);
        if (!eFile.exists()) {
            try {
                boolean newFile = eFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(eFile, true);
            sb = new StringBuilder();
            sb.append(data);
            sb.append("\n");
            os.write(sb.toString().getBytes());
            os.flush();
            os.close();
//            Log.d("File",folder+File.separator+fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}