package com.app.sambaaccesssmb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Editable;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.sambaaccesssmb.databinding.ActivityMainBinding;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        addClicks();
    }

    private void addClicks() {
        binding.btnSubmit.setOnClickListener((view) -> {
            stabilizeConnection(getText(binding.edtServerAddress),
                    getText(binding.edtUsername),
                    getText(binding.edtPassword));
        });
    }

    private void stabilizeConnection(String serverAddress, String userName, String password) {
        Thread thread = new Thread(() -> {
            Looper.prepare();
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, userName, password);
            try {
                SmbFile smbFile = new SmbFile(serverAddress, auth);
                smbFile.connect();

                if (smbFile.listFiles().length == 0) {
                    binding.txtStatus.setText("List of files is 0");
                } else {
                    binding.txtStatus.setText("List of files is : " + smbFile.listFiles().length);
                }

                // SmbFileOutputStream smbfos = new SmbFileOutputStream(smbFile +"");
                // smbfos.write("testing....and writing to a file".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("completed ...nice !");

            Looper.loop();
        });
        thread.setName("smb-connection");
        thread.start();


    }


    private String getText(EditText editText) {
        Editable text = editText.getText();
        return text != null ? text.toString() : "";
    }
}