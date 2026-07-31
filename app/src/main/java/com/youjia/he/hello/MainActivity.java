package com.youjia.he.hello;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.youjia.he.hello.adapters.ItemAdapter;
import com.youjia.he.hello.data.DataManager;
import com.youjia.he.hello.models.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DataManager dataManager;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TextView tvTotalCount;

    private String currentFilter = "all"; // all, weight, volume, count
    private List<Item> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance(this);
        allItems = dataManager.getAllItems();

        // 初始化视图
        tvTotalCount = findViewById(R.id.tv_total_count);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MaterialButtonToggleGroup filterGroup = findViewById(R.id.filter_toggle_group);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        //设置按钮跳转到设置界面
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        //搜索按钮跳转到搜索界面
        ImageButton btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        });

        //筛选监听
        filterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_filter_all) currentFilter = "all";
                else if (checkedId == R.id.btn_filter_weight) currentFilter = "weight";
                else if (checkedId == R.id.btn_filter_volume) currentFilter = "volume";
                else if (checkedId == R.id.btn_filter_count) currentFilter = "count";
                refreshData();
            }
        });

        //适配器
        adapter = new ItemAdapter(buildDisplayList(), item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除商品")
                    .setMessage("确定要删除 \"" + item.getName() + "\" 吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dataManager.deleteItem(item);
                        refreshData();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddDialog());

        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //从设置页返回时刷新数据（清除数据后同步，同时应用排序变化）
        refreshData();
    }

    private List<Object> buildDisplayList() {
        List<Item> filtered = new ArrayList<>();
        if ("all".equals(currentFilter)) {
            filtered.addAll(allItems);
        } else {
            for (Item item : allItems) {
                if (item.getGroup().equals(currentFilter)) {
                    filtered.add(item);
                }
            }
        }

        if (filtered.isEmpty()) return new ArrayList<>();

        //读取降序设置
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean descending = prefs.getBoolean("descending_sort_enabled", false);
        Comparator<Item> priceComparator = Comparator.comparingDouble(Item::getBasePrice);
        if (descending) {
            priceComparator = priceComparator.reversed();
        }

        List<Item> weightItems = new ArrayList<>();
        List<Item> volumeItems = new ArrayList<>();
        List<Item> countItems = new ArrayList<>();
        for (Item item : filtered) {
            String group = item.getGroup();
            if ("weight".equals(group)) weightItems.add(item);
            else if ("volume".equals(group)) volumeItems.add(item);
            else if ("count".equals(group)) countItems.add(item);
        }

        weightItems.sort(priceComparator);
        volumeItems.sort(priceComparator);
        countItems.sort(priceComparator);

        List<Object> display = new ArrayList<>();
        if (!weightItems.isEmpty()) {
            display.add(getString(R.string.group_header_weight, weightItems.size()));
            display.addAll(weightItems);
        }
        if (!volumeItems.isEmpty()) {
            display.add(getString(R.string.group_header_volume, volumeItems.size()));
            display.addAll(volumeItems);
        }
        if (!countItems.isEmpty()) {
            display.add(getString(R.string.group_header_count, countItems.size()));
            display.addAll(countItems);
        }
        return display;
    }

    private void refreshData() {
        allItems = dataManager.getAllItems();
        List<Object> displayList = buildDisplayList();
        adapter.updateData(displayList);
        tvTotalCount.setText(getString(R.string.total_count, allItems.size()));
        findViewById(R.id.empty_layout).setVisibility(allItems.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(allItems.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_name);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_price);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        MaterialAutoCompleteTextView autoCompleteUnit = dialogView.findViewById(R.id.autocomplete_unit);
        TextInputEditText etBoxSize = dialogView.findViewById(R.id.et_box_size);
        TextInputEditText etDensity = dialogView.findViewById(R.id.et_density);
        TextInputLayout boxSizeLayout = dialogView.findViewById(R.id.box_size_layout);
        TextInputLayout densityLayout = dialogView.findViewById(R.id.density_layout);

        //设置单位下拉选项
        String[] units = getResources().getStringArray(R.array.units_array);
        ArrayAdapter<String> adapterUnit = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, units);
        autoCompleteUnit.setAdapter(adapterUnit);

        //监听单位选择，动态显示/隐藏字段
        autoCompleteUnit.setOnItemClickListener((parent, view, position, id) -> {
            String unit = units[position];
            boxSizeLayout.setVisibility("盒".equals(unit) ? View.VISIBLE : View.GONE);
            densityLayout.setVisibility(("毫升".equals(unit) || "立方厘米".equals(unit) || "立方米".equals(unit))
                    ? View.VISIBLE : View.GONE);
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("添加商品")
                .setView(dialogView)
                .setPositiveButton("添加", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
                String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
                String unit = autoCompleteUnit.getText() != null ? autoCompleteUnit.getText().toString() : "";

                if (TextUtils.isEmpty(name)) {
                    etName.setError("请输入名称");
                    return;
                }
                if (TextUtils.isEmpty(priceStr)) {
                    etPrice.setError("请输入总价");
                    return;
                }
                if (TextUtils.isEmpty(unit)) {
                    autoCompleteUnit.setError("请选择单位");
                    return;
                }

                double price;
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    etPrice.setError("请输入有效正数");
                    return;
                }

                double quantity;
                try {
                    quantity = Double.parseDouble(quantityStr);
                    if (quantity <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    etQuantity.setError("请输入有效正数");
                    return;
                }

                Double boxSize = null;
                if ("盒".equals(unit)) {
                    String boxStr = etBoxSize.getText() != null ? etBoxSize.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(boxStr)) {
                        etBoxSize.setError("请输入每盒个数");
                        return;
                    }
                    try {
                        boxSize = Double.parseDouble(boxStr);
                        if (boxSize <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        etBoxSize.setError("请输入有效正数");
                        return;
                    }
                }

                Double density = null;
                if ("毫升".equals(unit) || "立方厘米".equals(unit) || "立方米".equals(unit)) {
                    String densityStr = etDensity.getText() != null ? etDensity.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(densityStr)) {
                        try {
                            density = Double.parseDouble(densityStr);
                            if (density <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            etDensity.setError("请输入有效正数（可选）");
                            return;
                        }
                    }
                }

                Item newItem = new Item(name, price, quantity, unit, boxSize, density);
                dataManager.addItem(newItem);
                refreshData();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "已添加", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}
//终于做完了