package com.youjia.he.hello;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.youjia.he.hello.adapters.SearchAdapter;
import com.youjia.he.hello.data.DataManager;
import com.youjia.he.hello.models.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private DataManager dataManager;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private View emptyLayout;
    private TextInputEditText etSearch;

    private List<Item> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataManager = DataManager.getInstance(this);
        allItems = dataManager.getAllItems();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_title);
        }

        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_view_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyLayout = findViewById(R.id.empty_layout_search);

        adapter = new SearchAdapter(new ArrayList<>(), item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除商品")
                    .setMessage("确定要删除 \"" + item.getName() + "\" 吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dataManager.deleteItem(item);
                        allItems = dataManager.getAllItems();
                        performSearch(etSearch.getText().toString());
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });

        //初始显示所有商品（按价格排序）
        performSearch("");
    }

    private void performSearch(String query) {
        List<Item> results = new ArrayList<>();
        String lowerQuery = query.trim().toLowerCase();

        //筛选商品（空查询时全部加入）
        for (Item item : allItems) {
            if (lowerQuery.isEmpty()) {
                results.add(item);
            } else {
                String name = item.getName().toLowerCase();
                int match = longestCommonSubsequenceLength(name, lowerQuery);
                if (match > 0) {
                    results.add(item);
                }
            }
        }

        //排序：匹配度降序，价格按设置
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean descending = prefs.getBoolean("descending_sort_enabled", false);
        Comparator<Item> comparator = (o1, o2) -> {
            int match1 = 0, match2 = 0;
            if (!lowerQuery.isEmpty()) {
                match1 = longestCommonSubsequenceLength(o1.getName().toLowerCase(), lowerQuery);
                match2 = longestCommonSubsequenceLength(o2.getName().toLowerCase(), lowerQuery);
            }
            if (match1 != match2) {
                return Integer.compare(match2, match1); // 匹配度降序
            }
            //价格比较（处理无穷大）
            double price1 = o1.getBasePrice();
            double price2 = o2.getBasePrice();
            if (price1 == Double.MAX_VALUE) price1 = Double.MAX_VALUE;
            if (price2 == Double.MAX_VALUE) price2 = Double.MAX_VALUE;
            return descending ? Double.compare(price2, price1) : Double.compare(price1, price2);
        };
        results.sort(comparator);

        adapter.updateData(results);
        emptyLayout.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
    }

    //计算最长公共子序列长度（不要求连续）
    private int longestCommonSubsequenceLength(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}