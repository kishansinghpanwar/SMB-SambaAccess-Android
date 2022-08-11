package com.app.sambaaccesssmb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.sambaaccesssmb.R;
import com.app.sambaaccesssmb.interfaces.ReceiveCallback;
import com.app.sambaaccesssmb.connection.SMBConnection;
import com.app.sambaaccesssmb.databinding.ActivityLoginBinding;

import java.util.List;

import jcifs.smb.SmbFile;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (SMBConnection.getInstance().isConnectionStabled()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            addClicks();
        }
    }

    private void addClicks() {
        binding.btnSubmit.setOnClickListener((view) -> stabilizeConnection(getText(binding.edtServerAddress),
                getText(binding.edtUsername),
                getText(binding.edtPassword)));
    }

    private void stabilizeConnection(String serverAddress, String userName, String password) {
        SMBConnection smbConnection = SMBConnection.getInstance();
        smbConnection.setReceiveCallback(new ReceiveCallback() {
            @Override
            public void onReceiveCallback(Bundle data) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (data.getBoolean("is_success")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, data.getString("reason"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onReceiveFilesData(List<SmbFile> fileList) {
                //no use of this, because we are not fetching any files on login screen.
            }
        });
        smbConnection.initiateConnection(serverAddress, userName, password);
    }


    private String getText(EditText editText) {
        Editable text = editText.getText();
        return text != null ? text.toString() : "";
    }
}