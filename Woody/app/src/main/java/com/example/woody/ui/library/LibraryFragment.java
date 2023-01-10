package com.example.woody.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woody.R;
import com.example.woody.entity.Wood;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private List<Wood> woods;
    private WoodAdapter woodAdapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://woody-5c79f-default-rtdb.asia-southeast1.firebasedatabase.app");
    private DatabaseReference myRef = database.getReference("Wood");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.search_bar);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getContext(),2,GridLayoutManager.VERTICAL,false);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, false));
        recyclerView.setLayoutManager(gridLayoutManager);
        getDataFromFirebase();
        if(woods!=null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    woodAdapter.getFilter().filter(s.toLowerCase().trim());
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (s.isEmpty() && woodAdapter!=null) {
                        woodAdapter.getFilter().filter("");
                    }
                    return false;
                }
            });
        }
        return view;
    }

    private void getDataFromFirebase() {
        woods = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Wood wood = data.getValue((Wood.class));
                    woods.add(wood);
                    // here you can access to name property like university.name
                };
                woodAdapter = new WoodAdapter(getContext(), woods);
                recyclerView.setAdapter(woodAdapter);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}