package com.example.woody.ui.library;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.woody.R;
import com.example.woody.entity.Wood;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WoodAdapter extends RecyclerView.Adapter<WoodAdapter.WoodViewHolder> implements Filterable {
    private List<Wood> woodList;
    private List<Wood> oldList;
    private Context context;

    public WoodAdapter(Context context, List<Wood> list) {
        this.context = context;
        this.woodList = list;
        this.oldList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gross_list_item, parent, false);
        return new WoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WoodViewHolder holder, int position) {
        Wood wood = woodList.get(position);
        if (wood == null) {
            return;
        }
        holder.displayName.setText(wood.getDisplayName());
        holder.scienceName.setText(wood.getScienceName());
        String[] arrayImage = wood.getImage().split(",");
        if (arrayImage.length > 0) {
            Glide.with(context).load(arrayImage[1].trim()).into(holder.imageView);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String woodDetailJson = gson.toJson(wood);
                bundle.putString("wood", woodDetailJson);
                Navigation.findNavController(view).navigate(R.id.detailWoodFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (woodList != null) {
            return woodList.size();
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String keyword = charSequence.toString();

                if (keyword.isEmpty()) {
                    woodList = oldList;
                } else {
                    List<Wood> list = new ArrayList<>();
                    for (Wood item : oldList) {
                        if(item.getDisplayName().toLowerCase().trim().contains(keyword.toLowerCase().trim())){
                            list.add(item);
                        }
                    }
                    woodList=list;
                }
                FilterResults results= new FilterResults();
                results.values=woodList;
                return  results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                woodList= (List<Wood>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class WoodViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView imageView;
        private TextView scienceName, displayName;

        public WoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            cardView = itemView.findViewById(R.id.card);
            scienceName = itemView.findViewById(R.id.science_name);
            displayName = itemView.findViewById(R.id.display_name);
        }
    }
}
