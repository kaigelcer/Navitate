package com.example.navitate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrowseActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private RetrofitInterface retrofitInterface;
    private static final String BASE_URL = "http://192.168.0.17:3000";
    private MyRecyclerViewAdapter recyclerViewAdapter;
    private TextView mTextView;
    private List<GenericMapAnnotation> annotations;
    private List<String> navitationTitles;
    private RecyclerView recyclerView;
    LinkedList<CircleOptions> circleData;
    LinkedList<PolylineOptions> lineData;
    LinkedList<PolylineOptions> drawingData;
    LinkedList<MarkerOptions> markerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        MapsInitializer.initialize(getApplicationContext());

        // Instantiate retrofit objects
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        circleData = new LinkedList<>();
        lineData = new LinkedList<>();
        drawingData = new LinkedList<>();
        markerData = new LinkedList<>();

        retrieveNavitations();

        annotations = new LinkedList<>();


    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + recyclerViewAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();

        retrieveNavitation(recyclerViewAdapter.getItem(position));
    }

    private void retrieveNavitation(String title) {
        Call<List<GenericMapAnnotation>> call = retrofitInterface.getNavitation(title);
        call.enqueue(new Callback<List<GenericMapAnnotation>>() {
            @Override
            public void onResponse(Call<List<GenericMapAnnotation>> call, Response<List<GenericMapAnnotation>> response) {
                annotations = response.body();
                Intent mapIntent = new Intent(BrowseActivity.this, MapsActivity.class);
                for (GenericMapAnnotation result : annotations){
                    if (result.type != null) {
                        switch (result.type) {
                            case "circle":
                                circleData.add(result.createCircle());
                                break;
                            case "line":
                                lineData.add(result.createLine());
                                break;
                            case "marker":
                                markerData.add(result.createMarker());
                                break;
                            case "drawing":
                                drawingData.add(result.createDrawing());
                                break;
                        }
                    } else {
                        mapIntent.putExtra("username", result.getUsername());
                        mapIntent.putExtra("password", result.getPassword());
                        mapIntent.putExtra("title", result.getTitle());
                    }
                }

                mapIntent.putExtra("circles", circleData);
                mapIntent.putExtra("lines", lineData);
                mapIntent.putExtra("drawings", drawingData);
                mapIntent.putExtra("markers", markerData);
                startActivity(mapIntent);

            }

            @Override
            public void onFailure(Call<List<GenericMapAnnotation>> call, Throwable t) {

            }
        });
    }

    private void retrieveNavitations() {
        Call<List<String>> call = retrofitInterface.getNavitations();
        call.enqueue(new Callback<List<String>>() {

            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BrowseActivity.this, "Success", Toast.LENGTH_LONG).show();
                }


                navitationTitles = response.body();
                System.out.println(response.body());
                // set up the RecyclerView
                recyclerView = findViewById(R.id.navitationRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(BrowseActivity.this));
                recyclerViewAdapter = new MyRecyclerViewAdapter(BrowseActivity.this, navitationTitles);
                recyclerViewAdapter.setClickListener(BrowseActivity.this);
                recyclerView.setAdapter(recyclerViewAdapter);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        ((LinearLayoutManager) (recyclerView.getLayoutManager())).getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(BrowseActivity.this, "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
}