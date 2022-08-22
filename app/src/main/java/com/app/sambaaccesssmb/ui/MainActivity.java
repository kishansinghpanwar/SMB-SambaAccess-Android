package com.app.sambaaccesssmb.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.sambaaccesssmb.R;
import com.app.sambaaccesssmb.adapter.FilesAdapter;
import com.app.sambaaccesssmb.connection.SMBConnection;
import com.app.sambaaccesssmb.databinding.ActivityMainBinding;
import com.app.sambaaccesssmb.interfaces.FilesClickListener;
import com.app.sambaaccesssmb.interfaces.ReceiveCallback;
import com.app.sambaaccesssmb.model.FilesModel;
import com.app.sambaaccesssmb.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity implements FilesClickListener, View.OnClickListener {
    final int PICK_FILE_REQUEST_CODE = 159;
    private final String TAG = this.getClass().getSimpleName();
    SMBConnection smbConnection = SMBConnection.getInstance();
    ActivityMainBinding binding;
    FilesAdapter filesAdapter;
    List<SmbFile> smbFileList = new ArrayList<>();
    List<SmbFile> backstackQueue = new ArrayList<>();
    List<FilesModel> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filesAdapter = new FilesAdapter(MainActivity.this, fileList, this);
        binding.rvData.setAdapter(filesAdapter);

        binding.btnUploadFile.setOnClickListener(this);
        try {
            setupAdapter();
        } catch (SmbException e) {
            e.printStackTrace();
        }


    }

    private void setupAdapter() throws SmbException {
        smbConnection.setReceiveCallback(new ReceiveCallback() {
            @Override
            public void onReceiveCallback(Bundle data) {
                //No use of that callback here.
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onReceiveFilesData(List<SmbFile> updatedFileList) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Collections.sort(updatedFileList, (t1, t2) -> {
                        try {
                            return Long.compare(t1.createTime(), t2.createTime());
                        } catch (SmbException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    });
                    synchronized (MainActivity.this) {
                        if (smbFileList != updatedFileList) {
                            smbFileList.clear();
                            fileList.clear();
                            smbFileList.addAll(updatedFileList);
                            for (SmbFile file :
                                    smbFileList) {
                                try {
                                    fileList.add(new FilesModel(file.getName(), file.isDirectory()));
                                } catch (SmbException e) {
                                    e.printStackTrace();
                                }
                            }
                            filesAdapter.notifyDataSetChanged();
                        }
                        binding.txtNoFilesFound.setVisibility(fileList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
        smbConnection.getAllFiles();
    }

    @Override
    public void onFileClick(int position) {
        if (fileList.get(position).isDirectory()) {
            binding.txtTitle.setText(fileList.get(position).getName());
            smbConnection.getAllFiles(smbFileList.get(position));
            backstackQueue.add(smbFileList.get(position));
        }
    }

    @Override
    public void onDeleteClick(int position) {
        smbConnection.deleteFile(smbFileList.get(position));
    }

    @Override
    public void onBackPressed() {
        if (smbConnection.getCurrentSMBFile() == smbConnection.getRootSMBFile()) {
            smbConnection.releaseThread();
            super.onBackPressed();
        } else {
            if (backstackQueue.isEmpty()) {
                binding.txtTitle.setText(getString(R.string.app_name));
                smbConnection.getAllFiles();
            } else {
                SmbFile smbFile = backstackQueue.get(backstackQueue.size() - 1);
                if (smbFile == smbConnection.getCurrentSMBFile()) {
                    backstackQueue.remove(backstackQueue.size() - 1);
                    onBackPressed();
                    return;
                }
                binding.txtTitle.setText(smbFile.getName());
                smbConnection.getAllFiles(smbFile);
                backstackQueue.remove(backstackQueue.size() - 1);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == binding.btnUploadFile) {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = null;
                try {
                    uri = data.getData();
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);

                    File file = null;
                    if (uri != null) {
                        file = new File(getCacheDir(), Utils.getFileName(uri));
                    }
                    try (OutputStream output = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                    }

                    if (file != null) {
                        Log.d(TAG, "onActivityResult: source: " + file + ", isExist: " + file.exists());
                        if (file.exists())
                            smbConnection.uploadFile(file);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}