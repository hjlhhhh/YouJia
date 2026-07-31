package com.youjia.he.hello;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.youjia.he.hello.data.DataManager;

public class SettingsActivity extends AppCompatActivity {

    private DataManager dataManager;
    private SharedPreferences sharedPreferences;
    private MaterialSwitch switchFollowSystem;
    private MaterialSwitch switchNightMode;
    private MaterialSwitch switchDescending;

    // 偏好键
    private static final String KEY_FOLLOW_SYSTEM = "follow_system_enabled";
    private static final String KEY_MANUAL_NIGHT = "manual_night_enabled";
    private static final String KEY_DESCENDING_SORT = "descending_sort_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 super.onCreate() 之前应用已保存的主题
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dataManager = DataManager.getInstance(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }

        // 初始化开关
        switchFollowSystem = findViewById(R.id.switch_follow_system);
        switchNightMode = findViewById(R.id.switch_theme);
        switchDescending = findViewById(R.id.switch_descending);

        // 读取存储状态
        boolean followSystem = sharedPreferences.getBoolean(KEY_FOLLOW_SYSTEM, true);
        boolean manualNight = sharedPreferences.getBoolean(KEY_MANUAL_NIGHT, false);
        boolean descending = sharedPreferences.getBoolean(KEY_DESCENDING_SORT, false);

        // 设置开关初始状态
        switchFollowSystem.setChecked(followSystem);
        switchNightMode.setChecked(manualNight);
        switchDescending.setChecked(descending);

        // 根据跟随状态决定夜间模式开关是否可用
        updateNightModeSwitchEnable(followSystem);

        // 监听跟随系统开关
        switchFollowSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_FOLLOW_SYSTEM, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                switchNightMode.setEnabled(false);
            } else {
                switchNightMode.setEnabled(true);
                applyNightMode(switchNightMode.isChecked());
            }
            recreate();
        });

        // 监听夜间模式开关（仅在跟随关闭时有效）
        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchFollowSystem.isChecked()) {
                return;
            }
            sharedPreferences.edit().putBoolean(KEY_MANUAL_NIGHT, isChecked).apply();
            applyNightMode(isChecked);
            recreate();
        });

        // 监听降序排序开关
        switchDescending.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DESCENDING_SORT, isChecked).apply();
            // 不需要 recreate，主界面返回时会刷新
        });

        // 清除数据按钮
        findViewById(R.id.btn_clear_data).setOnClickListener(v -> showClearDataConfirm());
    }

    /**
     * 在 onCreate 之前应用存储的主题模式
     */
    private void applySavedTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean followSystem = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true);
        if (followSystem) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            boolean manualNight = prefs.getBoolean(KEY_MANUAL_NIGHT, false);
            if (manualNight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    /**
     * 根据是否跟随系统，启用/禁用夜间模式开关
     */
    private void updateNightModeSwitchEnable(boolean followSystem) {
        switchNightMode.setEnabled(!followSystem);
    }

    /**
     * 应用手动夜间模式（亮/暗）
     */
    private void applyNightMode(boolean night) {
        if (night) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void showClearDataConfirm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_data_confirm_title)
                .setMessage(R.string.clear_data_confirm_message)
                .setPositiveButton(R.string.clear_data, (dialog, which) -> {
                    dataManager.clearAll();
                    Toast.makeText(this, "所有数据已清除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}