package com.example.woody.ui.identify;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.woody.R;
import com.example.woody.entity.Wood;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DetailWoodFragment extends Fragment {
    private Chip preservationStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detailwood, container, false);
        Bundle bundle = this.getArguments();
        TextView displayName = view.findViewById(R.id.display_name);
        TextView otherName = view.findViewById(R.id.other_name);
        preservationStatus = view.findViewById(R.id.preservation_status);
        TextView scienceName = view.findViewById(R.id.science_name);
        TextView commercialName = view.findViewById(R.id.commercial_name);
        TextView family = view.findViewById(R.id.family);
        TextView woodType = view.findViewById(R.id.wood_type);
        TextView distribution = view.findViewById(R.id.distribution);
        TextView properties = view.findViewById(R.id.properties);
        TextView usage = view.findViewById(R.id.usage);
        TextView reference = view.findViewById(R.id.reference);
//        TextView percent= view.findViewById(R.id.percent);

        ImageCarousel carousel = view.findViewById(R.id.imageView);
        ImageView backBtn = view.findViewById(R.id.backBtn);
        if (bundle != null) {
            Gson gson = new Gson();
            Wood woodDetail = gson.fromJson(bundle.getString("wood"), Wood.class);

            displayName.setText(woodDetail.getDisplayName());
            otherName.setText(woodDetail.getGeneralName());
            setPreservationStatus(woodDetail.getPreservation());
            scienceName.setText(woodDetail.getScienceName());
            commercialName.setText(woodDetail.getCommercialName());
            family.setText(woodDetail.getFamily());
            woodType.setText(woodDetail.getType());
            distribution.setText(woodDetail.getDistribution());
            properties.setText(woodDetail.getProperties());
            usage.setText(woodDetail.getUsage());
            reference.setText(Html.fromHtml("<a href=\""+ woodDetail.getReference() + "\">" + woodDetail.getReference() + "</a>"));
            reference.setClickable(true);
            reference.setMovementMethod (LinkMovementMethod.getInstance());
            String[] arrayImage = woodDetail.getImage().split(",");
            if (arrayImage.length > 0) {
                List<CarouselItem> list = new ArrayList<>();
                for (int i = 0; i < arrayImage.length; i++) {
                    list.add(new CarouselItem(
                            arrayImage[i].trim()
                    ));
                }
                carousel.setData(list);
            }
//            percent.setText(bundle.getString("percent"));
//            Glide.with(getContext()).load(woodDetail.getImage()).into(imageView);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   if(bundle.getString("fragment")=="Identify"){
                    Navigation.findNavController(view).navigate(R.id.navigation_identify);

                   }
                   else{
                      Navigation.findNavController(view).navigate(R.id.navigation_library);
                   }


                }
            });

        }
        return view;
    }

    private void setPreservationStatus(int preservation) {
        switch (preservation) {
            case 1:

                preservationStatus.setText("EX-Tuyệt chủng");
                preservationStatus.setTextColor(Color.parseColor("#93000A"));
                preservationStatus.setChipBackgroundColorResource(R.color.black);
                break;
            case 2:

                preservationStatus.setText("EW-Tuyệt chủng trong tự nhiên");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.black);
                break;
            case 3:

                preservationStatus.setText("CR-Cực kì nguy cấp");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.danger);
                break;
            case 4:

                preservationStatus.setText("EN-Nguy cấp");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.warning);
                break;
            case 5:

                preservationStatus.setText("VU-Sắp nguy cấp");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.yellow);
                break;
            case 7:

                preservationStatus.setText("NT-Sắp bị đe dọa");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.normal);
                break;
            case 6:

                preservationStatus.setText("Cd-Phụ thuộc bảo tồn");
                preservationStatus.setTextColor(Color.parseColor("#FFFBFF"));
                preservationStatus.setChipBackgroundColorResource(R.color.green);
                break;
            case 8:

                preservationStatus.setText("LC-Ít quan tâm");
                break;
            default:
                preservationStatus.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
