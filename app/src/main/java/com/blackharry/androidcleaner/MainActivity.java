package com.blackharry.androidcleaner;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;
    private List<File> recordings;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "recordings":
                    loadAndDisplayRecordings();
                    return true;
                case "categories":
                    // Handle categories action
                    return true;
                case "settings":
                    // Handle settings action
                    return true;
            }
            return false;
        });

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordings = new ArrayList<>();
        adapter = new RecordingAdapter(recordings, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAndDisplayRecordings() {
        // 清空当前列表
        recordings.clear();
        
        // 获取录音文件列表
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "Recordings");
        if (recordingsDir.exists() && recordingsDir.isDirectory()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isAudioFile(file)) {
                        recordings.add(file);
                    }
                }
            }
        }
        
        // 通知适配器数据已更新
        adapter.notifyDataSetChanged();
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || 
               name.endsWith(".wav") || 
               name.endsWith(".m4a") || 
               name.endsWith(".aac");
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