package com.blackharry.androidcleaner;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void filterAndClassifyRecordings() {
        // 获取录音文件列表
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "Recordings");
        if (recordingsDir.exists() && recordingsDir.isDirectory()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 过滤逻辑
                    if (isDeletedCallRecording(file)) {
                        // 分类逻辑
                        classifyRecording(file);
                    }
                }
            }
        }
    }

    private boolean isDeletedCallRecording(File file) {
        // 判断文件是否为已删除通话记录的录音
        return file.getName().contains("deleted");
    }

    private void classifyRecording(File file) {
        // 根据通话频次和占用空间大小进行分类
    }
}