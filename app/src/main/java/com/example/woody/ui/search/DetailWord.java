package com.example.woody.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.woody.R;
import com.example.woody.entity.Glossary;
import com.google.gson.Gson;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class DetailWord extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_detail, container, false);

        Bundle bundle = this.getArguments();
        TextView vietnamese = view.findViewById(R.id.vietnamese);
        TextView english = view.findViewById(R.id.english);
        TextView description = view.findViewById(R.id.description);
        ImageCarousel carousel = view.findViewById(R.id.imageView);
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.navigation_search);


            }
        });
        if (bundle != null) {
            Gson gson = new Gson();
            Glossary glossaryDetail = gson.fromJson(bundle.getString("glossary"), Glossary.class);

            vietnamese.setText(glossaryDetail.getVietnamese());
            english.setText(glossaryDetail.getEnglish());
            description.setText(glossaryDetail.getDefinition());
            List<CarouselItem> list = new ArrayList<>();

            String[] glossaryImage = glossaryDetail.getImage().split(",");

            if (glossaryImage.length > 0 && !glossaryImage[0].isEmpty()) {
                carousel.setVisibility(View.VISIBLE);
                for (int i = 0; i < glossaryImage.length; i++) {
                    list.add(new CarouselItem(glossaryImage[i].trim()));
                }
            }

            carousel.setAutoWidthFixing(true);
            carousel.setShowTopShadow(false);
            carousel.setShowBottomShadow(false);
            carousel.setData(list);
        }
        return view;
    }
}