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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Item> items;
    private OnItemDeleteListener deleteListener;
    private final DecimalFormat priceFormat = new DecimalFormat("#0.0000");
    private final DecimalFormat totalFormat = new DecimalFormat("#0.00");

    public interface OnItemDeleteListener {
        void onDelete(Item item);
    }

    public SearchAdapter(List<Item> items, OnItemDeleteListener listener) {
        this.items = items;
        this.deleteListener = listener;
    }

    public void updateData(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTotal, tvQty, tvUnitPrice, tvDensityExtra;
        Button btnDelete;

        SearchViewHolder(@NonNull View itemView) {
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