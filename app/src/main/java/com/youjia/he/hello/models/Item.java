package com.youjia.he.hello.models;

import androidx.annotation.NonNull;

public class Item {
    private String name;
    private double totalPrice;
    private double quantity;
    private String unit;           // "个","斤","千克","克","毫升","立方厘米","立方米","盒"
    private Double boxSize;        // 仅当unit为盒"时有效
    private Double density;        // 仅当unit为体积单位时有效


    public Item(String name, double totalPrice, double quantity, String unit,
                Double boxSize, Double density) {
        this.name = name;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.unit = unit;
        this.boxSize = boxSize;
        this.density = density;
    }

    //getters  setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getBoxSize() { return boxSize; }
    public void setBoxSize(Double boxSize) { this.boxSize = boxSize; }

    public Double getDensity() { return density; }
    public void setDensity(Double density) { this.density = density; }


    public double getEffectiveQty() {
        if ("盒".equals(unit) && boxSize != null && boxSize > 0) {
            return quantity * boxSize;
        }
        return quantity;
    }

    public String getGroup() {
        if (unit == null) return "other";
        switch (unit) {
            case "克":
            case "斤":
            case "千克":
                return "weight";
            case "毫升":
            case "立方厘米":
            case "立方米":
                return "volume";
            case "个":
            case "盒":
                return "count";
            default:
                return "other";
        }
    }

    public double getBasePrice() {
        String group = getGroup();
        if (group.equals("weight")) {
            double grams = quantity;
            if ("斤".equals(unit)) grams = quantity * 500;
            else if ("千克".equals(unit)) grams = quantity * 1000;
            if (grams == 0) return Double.MAX_VALUE;
            return totalPrice / grams;
        } else if (group.equals("volume")) {
            double ml = quantity;
            if ("立方厘米".equals(unit)) ml = quantity;
            else if ("立方米".equals(unit)) ml = quantity * 1_000_000;
            if (ml == 0) return Double.MAX_VALUE;
            return totalPrice / ml;
        } else if (group.equals("count")) {
            double count = getEffectiveQty();
            if (count == 0) return Double.MAX_VALUE;
            return totalPrice / count;
        }
        return Double.MAX_VALUE;
    }

    public String getPriceUnitLabel() {
        String group = getGroup();
        if (group.equals("weight")) return "元/克";
        if (group.equals("volume")) return "元/毫升";
        if (group.equals("count")) return "元/个";
        return "";
    }

    public String getDisplayQty() {
        if ("盒".equals(unit) && boxSize != null) {
            return quantity + " 盒 (共" + (int)(quantity * boxSize) + "个)";
        }
        return quantity + " " + unit;
    }

    public Double getDensityPrice() {
        if (density != null && density > 0 && "volume".equals(getGroup())) {
            double base = getBasePrice();
            if (base == Double.MAX_VALUE) return null;
            return base / density;
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", totalPrice=" + totalPrice +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", boxSize=" + boxSize +
                ", density=" + density +
                '}';
    }
}