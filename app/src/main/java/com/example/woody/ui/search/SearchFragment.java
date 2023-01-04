package com.example.woody.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.woody.R;
import com.example.woody.entity.Glossary;
import com.example.woody.entity.Wood;
import com.example.woody.ui.library.WoodAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private List<Glossary> glossaries;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://woody-5c79f-default-rtdb.asia-southeast1.firebasedatabase.app");
    private DatabaseReference myRef = database.getReference("Glossary");
    private ListView listView;
    private SearchView searchView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_search,container,false);
        searchView=view.findViewById(R.id.search_bar);
        listView =view.findViewById(R.id.list_item);
        getGlossaryFromFirebase();
        if(glossaries!=null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Glossary glossary= glossaries.get(i);
                    Bundle bundle = new Bundle();
                    Gson gson = new Gson();
                    String woodDetailJson = gson.toJson(glossary);
                    bundle.putString("glossary", woodDetailJson);
                    Navigation.findNavController(view).navigate(R.id.detailWord, bundle);
                }
            });
        }
        return view;
    }

    private void getGlossaryFromFirebase() {
        glossaries = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Glossary wood = data.getValue((Glossary.class));
                    glossaries.add(wood);
                    // here you can access to name property like university.name
                };
                ArrayAdapter arrayAdapter= new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,glossaries);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}