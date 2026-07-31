package com.youjia.he.hello.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.youjia.he.hello.models.Item;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private final Context context;
    private final Gson gson;
    private final File dataFile;
    private List<Item> items = new ArrayList<>();

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        File dir = context.getExternalFilesDir(null);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        dataFile = new File(dir, "data.json");
        loadItems();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    private void loadItems() {
        if (!dataFile.exists()) {
            items = new ArrayList<>();
            return;
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<Item>>() {}.getType();
            List<Item> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                items = loaded;
            } else {
                items = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            items = new ArrayList<>();
        }
    }

    private void saveItems() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //CRUD
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    public void addItem(Item item) {
        items.add(item);
        saveItems();
    }

    public void deleteItem(Item item) {
        items.remove(item);
        saveItems();
    }

    public void updateItem(Item item) {
        int index = items.indexOf(item);
        if (index != -1) {
            items.set(index, item);
            saveItems();
        }
    }

    public void clearAll() {
        items.clear();
        saveItems();
    }
}