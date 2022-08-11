package com.app.sambaaccesssmb.utils;

import androidx.annotation.NonNull;

import com.app.sambaaccesssmb.R;

public class Utils {
    public Utils() {
        throw new ExceptionInInitializerError("Cannot initialize utils class.");
    }

    @NonNull
    public static String getExtension(String file) {
        if (file.contains(".")) {
            String[] splitArray = file.split("\\.");
            if (splitArray.length > 0) {
                return splitArray[splitArray.length - 1];
            }
        }
        return "N/A";
    }

    public static int getIconFromExtension(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            case "txt":
                return R.drawable.txt;
            case "ppt":
                return R.drawable.ppt;
            case "psd":
                return R.drawable.psd;
            case "svg":
                return R.drawable.svg;
            case "xls":
                return R.drawable.xls;
            case "avi":
                return R.drawable.avi;
            case "html":
                return R.drawable.html;
            case "iso":
                return R.drawable.iso;
            case "doc":
                return R.drawable.doc;
            case "pdf":
                return R.drawable.pdf;
            case "png":
                return R.drawable.png;
            case "jpg":
            case "jpeg":
                return R.drawable.jpg;
            case "js":
                return R.drawable.javascript;
            case "json":
                return R.drawable.json;
            case "mp3":
                return R.drawable.mp3;
            case "mp4":
                return R.drawable.mp4;
            case "xml":
                return R.drawable.xml;
            case "zip":
                return R.drawable.zip;
            default:
                return R.drawable.file;

        }
    }
}
