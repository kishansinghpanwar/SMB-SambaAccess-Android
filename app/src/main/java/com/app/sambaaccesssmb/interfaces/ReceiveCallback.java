package com.app.sambaaccesssmb.interfaces;

import android.os.Bundle;

import java.util.List;

import jcifs.smb.SmbFile;

public interface ReceiveCallback {
    void onReceiveCallback(Bundle data);

    void onReceiveFilesData(List<SmbFile> fileList);
}
