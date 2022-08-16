package com.app.sambaaccesssmb.connection;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.app.sambaaccesssmb.interfaces.ReceiveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class SMBConnection {
    private static volatile SMBConnection instance = null;
    private ReceiveCallback receiveCallback;
    private SmbFile rootSMBFile;
    private SmbFile currentSMBFile;
    private boolean threadRunning = true;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (threadRunning) {
                    if (receiveCallback != null) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("is_success", true);
                        receiveCallback.onReceiveCallback(bundle);
                        receiveCallback.onReceiveFilesData(Arrays.asList(currentSMBFile.listFiles()));
                    }
                    Thread.sleep(500);
                }
            } catch (SmbException | InterruptedException e) {
                e.printStackTrace();
                Bundle bundle = new Bundle();
                bundle.putBoolean("is_success", false);
                bundle.putString("reason", e.getMessage());
                receiveCallback.onReceiveCallback(bundle);
            }
        }
    };
    private Thread thread;
    private boolean isConnectionStabled;

    public static SMBConnection getInstance() {
        if (instance == null) {
            synchronized (SMBConnection.class) {
                if (instance == null) {
                    instance = new SMBConnection();
                }
            }
        }
        return instance;
    }

    public boolean isConnectionStabled() {
        return isConnectionStabled;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void setReceiveCallback(ReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
    }

    public void initiateConnection(String serverAddress, String userName, String password) {
        Thread thread = new Thread(() -> {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, userName, password);
            Bundle bundle = new Bundle();
            try {
                rootSMBFile = new SmbFile(serverAddress, auth);
                rootSMBFile.connect();
                bundle.putBoolean("is_success", true);
            } catch (Exception e) {
                bundle.putBoolean("is_success", false);
                bundle.putString("reason", e.getMessage());
            }
            isConnectionStabled = true;
            if (receiveCallback != null) {
                receiveCallback.onReceiveCallback(bundle);
            }
        });
        thread.setName("smb-connection");
        thread.start();
    }

    public void getAllFiles() {
        getAllFiles(null);
    }

    public void getAllFiles(SmbFile currentFile) {
        currentSMBFile = currentFile != null ? currentFile : rootSMBFile;
        if (thread == null || !thread.isAlive()) {
            threadRunning = true;
            thread = new Thread(runnable);
            thread.setName("smb-get-files");
            thread.start();
        }
    }

    public void uploadFile(File file) throws IOException {
        Thread thread = new Thread(() -> {
            try {
                // local source file and target smb file
                SmbFile smbFileTarget = new SmbFile(currentSMBFile, file.getName());

                // input and output stream
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                SmbFileOutputStream smbfos = new SmbFileOutputStream(smbFileTarget);

                // writing data
                try {
                    // 16 kb
                    final byte[] b = new byte[16 * 1024];
                    int read;
                    if (fis != null) {
                        while ((read = fis.read(b, 0, b.length)) > 0) {
                            smbfos.write(b, 0, read);
                        }
                    }
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                    smbfos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setName("upload-file");
        thread.start();
    }

    public void releaseThread() {
        threadRunning = false;
    }

    public SmbFile getCurrentSMBFile() {
        return currentSMBFile;
    }

    public SmbFile getRootSMBFile() {
        return rootSMBFile;
    }
}
