package com.app.sambaaccesssmb.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.app.sambaaccesssmb.interfaces.FilesClickListener;
import com.app.sambaaccesssmb.R;
import com.app.sambaaccesssmb.interfaces.ReceiveCallback;
import com.app.sambaaccesssmb.connection.SMBConnection;
import com.app.sambaaccesssmb.adapter.FilesAdapter;
import com.app.sambaaccesssmb.databinding.ActivityMainBinding;
import com.app.sambaaccesssmb.model.FilesModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity implements FilesClickListener {
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
                if (smbFile == smbConnection.getCurrentSMBFile()){
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
}