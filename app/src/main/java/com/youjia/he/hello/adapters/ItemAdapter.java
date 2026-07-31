package com.youjia.he.hello.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youjia.he.hello.R;
import com.youjia.he.hello.models.Item;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> dataList; // 包含 String (header) 和 Item
    private OnItemDeleteListener deleteListener;

    private final DecimalFormat priceFormat = new DecimalFormat("#0.0000");
    private final DecimalFormat totalFormat = new DecimalFormat("#0.00");

    public interface OnItemDeleteListener {
        void onDelete(Item item);
    }

    public ItemAdapter(List<Object> dataList, OnItemDeleteListener listener) {
        this.dataList = dataList;
        this.deleteListener = listener;
    }

    public void updateData(List<Object> newData) {
        this.dataList = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = dataList.get(position);
        if (obj instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_card, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object obj = dataList.get(position);
        if (holder instanceof HeaderViewHolder) {
            String header = (String) obj;
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ItemViewHolder) {
            Item item = (Item) obj;
            ((ItemViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    // ViewHolders
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
        }

        void bind(String header) {
            tvGroupName.setText(header);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTotal, tvQty, tvUnitPrice, tvDensityExtra;
        Button btnDelete;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvTotal = itemView.findViewById(R.id.tv_item_total);
            tvQty = itemView.findViewById(R.id.tv_item_qty);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvDensityExtra = itemView.findViewById(R.id.tv_density_extra);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(Item item) {
            tvName.setText(item.getName());
            tvTotal.setText(String.format(Locale.CHINA, "¥%s", totalFormat.format(item.getTotalPrice())));
            tvQty.setText(item.getDisplayQty());

            double base = item.getBasePrice();
            String priceStr = (base == Double.MAX_VALUE) ? "∞" : priceFormat.format(base);
            tvUnitPrice.setText(String.format(Locale.CHINA, "%s %s", priceStr, item.getPriceUnitLabel()));

            // 密度换算（仅体积类）
            Double densityPrice = item.getDensityPrice();
            if (densityPrice != null) {
                tvDensityExtra.setVisibility(View.VISIBLE);
                tvDensityExtra.setText(String.format(Locale.CHINA, "≈ %s 元/克 (密度%.2f)",
                        priceFormat.format(densityPrice), item.getDensity()));
            } else if ("volume".equals(item.getGroup())) {
                tvDensityExtra.setVisibility(View.VISIBLE);
                tvDensityExtra.setText("(无密度，无法换算)");
            } else {
                tvDensityExtra.setVisibility(View.GONE);
            }

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(item);
                }
            });
        }
    }
}