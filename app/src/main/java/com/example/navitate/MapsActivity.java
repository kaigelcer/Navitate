package com.example.navitate;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "Maps";
    private GoogleMap mMap;
    private LatLng myLocation;
    private RadioGroup annotationGroup;
    private Button circleButton;
    private Button lineButton;
    private Button pointButton;
    private Button drawButton;
    private PolylineOptions drawingOptions;
    private Polyline drawing;
    private LineEditor lineEditor;
    private UsernamePasswordEntry usernamePasswordEntry;
    private Button postButton;
    private String username;
    private String password;
    private String title;
    private boolean canEdit;
    LinkedList<Circle> circles;
    LinkedList<Polyline> lines;
    LinkedList<Polyline> drawings;
    LinkedList<Marker> markers;
    private RetrofitInterface retrofitInterface;
    private static final String BASE_URL = "http://192.168.0.17:3000";
    private static final int LINE = 1;
    private static final int CIRCLE = 2;
    private static final int DRAWING = 3;
    private Button annotateMapButton;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up important views
        lineEditor = (LineEditor) getSupportFragmentManager().findFragmentById(R.id.lineEditor);
        lineEditor.getView().setVisibility(View.GONE);
        usernamePasswordEntry = (UsernamePasswordEntry) getSupportFragmentManager().findFragmentById(R.id.usernamePasswordEntry);
        usernamePasswordEntry.getView().setVisibility(View.GONE);
        annotationGroup = findViewById(R.id.radio_group_annotate);
        annotationGroup.setVisibility(View.GONE);
        canEdit = true;

        // Instantiate retrofit objects
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        // Initialize Location Manager; request location update every second
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

    }

    // Executed when loading and existing navitation
    private void loadData() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            canEdit = false;
            postButton.setText(R.string.update);
            postButton.setVisibility(View.GONE);
            annotateMapButton.setText(R.string.edit);
            ArrayList<CircleOptions> circleData = extras.getParcelableArrayList("circles");
            ArrayList<PolylineOptions> lineData = extras.getParcelableArrayList("lines");
            ArrayList<PolylineOptions> drawingData = extras.getParcelableArrayList("drawings");
            ArrayList<MarkerOptions> markerData = extras.getParcelableArrayList("markers");
            username = extras.getString("username");
            password = extras.getString("password");
            title = extras.getString("title");
            for (CircleOptions options : circleData){
                circles.add(mMap.addCircle(options));
            }
            for (PolylineOptions options : lineData){
                lines.add(mMap.addPolyline(options));
            }
            for (PolylineOptions options : drawingData){
                drawings.add(mMap.addPolyline(options));
            }
            for(MarkerOptions options : markerData) {
                markers.add(mMap.addMarker(options));
            }

        }
    }

    private void buttonSetup() {
        RadioGroup group = findViewById(R.id.radio_group_list_selector);

        // Initialize home button
        Button returnHomeButton = findViewById(R.id.returnHomeButton);
        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(homeIntent);
            }
        });

        annotateMapButton = findViewById(R.id.annotate);
        annotateMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit) {
                    annotationGroup.setVisibility(View.VISIBLE);
                } else {
                    usernamePasswordEntry.getView().setVisibility(View.VISIBLE);
                    usernamePasswordEntry.lockUsernameAndTitle(username, password, title);
                    Toast.makeText(MapsActivity.this, "Enter password to edit", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Circle Button Setup
        circleButton = findViewById(R.id.circleButton);
        circleButtonSetup();


        //Line Button Setup
        lineButton = findViewById(R.id.lineButton);
        lineButtonSetup();

        //Draw Button Setup
        drawButton = findViewById(R.id.drawButton);
        drawButtonSetup();

        //Post Button
        postButton = findViewById(R.id.postNavitation);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //postAll();
                enterMetaData();
            }
        });

    }

    public void lineButtonSetup(){
        lineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineEditor.getView().setVisibility(View.VISIBLE);
                lineEditor.setType("line");
            }
        });
    }

    public void lineClickDraw(int strokeColor, float strokeWidth, List<PatternItem> pattern, String patternTag) {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                PolylineOptions lineOptions = new PolylineOptions()
                        .add(latLng)
                        .width(strokeWidth)
                        .color(strokeColor)
                        .pattern(pattern);
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        lineOptions.add(latLng);
                        Polyline line = mMap.addPolyline(lineOptions);
                        line.setTag(patternTag);
                        lines.add(line);
                        lineClickDraw(strokeColor, strokeWidth, pattern, patternTag);
                    }
                });
            }
        });
        lineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToDropMarker();
                lineButtonSetup();
            }
        });
    }

    public void circleClickDraw(int strokeColor, float strokeWidth, List<PatternItem> pattern, String patternTag, int fillColor){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Circle newCircle;
                CircleOptions options = new CircleOptions()
                        .center(latLng)
                        .radius(1)
                        .fillColor(fillColor)
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth)
                        .strokePattern(pattern);
                newCircle = mMap.addCircle(options);
                LatLng parentLatLng = new LatLng(latLng.latitude, latLng.longitude);
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        newCircle.setRadius(distance(latLng, parentLatLng));
                        newCircle.setTag(patternTag);
                        circles.add(newCircle);
                        System.out.println(distance(latLng, parentLatLng));
                        circleClickDraw(strokeColor, strokeWidth, pattern, patternTag, fillColor);
                    }
                });
            }
        });
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToDropMarker();
                circleButtonSetup();
            }
        });
    }

    private void circleButtonSetup() {
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineEditor.getView().setVisibility(View.VISIBLE);
                lineEditor.setType("circle");
            }
        });
    }


    private void drawButtonSetup() {
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineEditor.getView().setVisibility(View.VISIBLE);
                lineEditor.setType("draw");
            }
        });
    }

    public void clickDraw(int strokeColor, float strokeWidth, List<PatternItem> pattern, String patternTag){
        @SuppressLint("UseCompatLoadingForDrawables") Bitmap penMarker = ((BitmapDrawable) getResources().getDrawable(R.drawable.pen)).getBitmap();
        penMarker = Bitmap.createScaledBitmap(penMarker, 100, 100, false);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mMap.getCameraPosition().target.latitude,
                        mMap.getCameraPosition().target.longitude))
                .title("Drag to Draw")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(penMarker))
                .anchor(0,1));
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                drawingOptions = new PolylineOptions()
                        .add(marker.getPosition())
                        .color(strokeColor)
                        .width(strokeWidth)
                        .pattern(pattern);
                drawing = mMap.addPolyline(drawingOptions);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                drawingOptions.add(marker.getPosition());
                drawing.setPoints(drawingOptions.getPoints());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                drawingOptions.add(marker.getPosition());
                drawing.setPoints(drawingOptions.getPoints());
                drawing.setTag(patternTag);
                drawings.add(drawing);
                drawing.setClickable(true);
                marker.setDraggable(false);
                marker.remove();
            }
        });
    }

    // Calculate distance between two LatLng points
    private double distance(LatLng latLng, LatLng parentLatLng) {
        double p = Math.PI / 180;
        double lat1 = latLng.latitude;
        double long1 = latLng.longitude;
        double lat2 = parentLatLng.latitude;
        double long2 = parentLatLng.longitude;
        double a = 0.5 - Math.cos((lat2 - lat1)*p)/2 + Math.cos(lat1*p) * Math.cos(lat2*p) * (1-Math.cos((long2-long1)*p))/2;
        return 1000*12742 * Math.asin(Math.sqrt(a));
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        (new Handler()).postDelayed(this::displayCurrentLocation, 1500);
        LatLng sydney = new LatLng(-34, 151);

        circles = new LinkedList<>();
        lines = new LinkedList<>();
        markers = new LinkedList<>();
        drawings = new LinkedList<>();




        // Initialize Buttons
        buttonSetup();
        clickToDropMarker();

        // Get extras if loading a navitation
        loadData();


        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setAllGesturesEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
    }

    private void displayCurrentLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12.0f));
    }

    private void clickToDropMarker() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker").draggable(true));
                marker.setTag(BitmapDescriptorFactory.HUE_RED);
                markers.add(marker);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerDialog(marker);

                return false;
            }
        });
    }

    private void markerDialog(Marker selected) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Marker Settings");
        alertDialog.setMessage("this is my app");

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Change Marker Colour", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                openColorPicker(alertDialog, selected);

            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Edit Marker Name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nameChangeDialog(selected);
                alertDialog.cancel();
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Delete Marker", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected.remove();
                alertDialog.cancel();
            }
        });

        alertDialog.show();

        int[] buttons = {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL};

        for (int i : buttons){
            final Button button = alertDialog.getButton(i);
            LinearLayout.LayoutParams buttonLL = (LinearLayout.LayoutParams) button.getLayoutParams();
            buttonLL.gravity = Gravity.CENTER;
            button.setLayoutParams(buttonLL);
        }




    }

    private void nameChangeDialog(Marker selected){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected.setTitle(input.getText().toString());
                selected.hideInfoWindow();
                selected.showInfoWindow();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void openColorPicker(AlertDialog alertDialog, Marker selected){
        AmbilWarnaDialog colourPicker = new AmbilWarnaDialog(this, (int) BitmapDescriptorFactory.HUE_BLUE, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                alertDialog.cancel();
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selected.setIcon(getMarkerIcon(color));
                selected.setTag(color);
                alertDialog.cancel();
            }
        });
        colourPicker.show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());

    }

    public BitmapDescriptor getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private void postOne(HashMap<String, String> map){
        Call<Void> call = retrofitInterface.postNavitation(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(MapsActivity.this, "Success", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    // Prepare and post the information from this map to server
    public void postAll(){

        HashMap<String, String> metaData = new HashMap<>();
        metaData.put("type", "metadata");
        metaData.put("username", username);
        metaData.put("password", password);
        metaData.put("title", title);
        postOne(metaData);

        for (Circle circle : circles){
            HashMap<String, String> circleMap = new HashMap<>(metaData);
            circleMap.put("type", "circle");
            circleMap.put("latitude", String.valueOf(circle.getCenter().latitude));
            circleMap.put("longitude", String.valueOf(circle.getCenter().longitude));
            circleMap.put("strokeColour", String.valueOf(circle.getStrokeColor()));
            circleMap.put("strokeWidth", String.valueOf(circle.getStrokeWidth()));
            circleMap.put("strokePattern", String.valueOf(circle.getTag()));
            circleMap.put("fillColour", String.valueOf(circle.getFillColor()));
            circleMap.put("radius", String.valueOf(circle.getRadius()));
            postOne(circleMap);
        }

        for (Marker marker : markers){
            HashMap<String, String> markerMap = new HashMap<>(metaData);
            markerMap.put("type", "marker");
            markerMap.put("latitude", String.valueOf(marker.getPosition().latitude));
            markerMap.put("longitude", String.valueOf(marker.getPosition().longitude));
            markerMap.put("colour", String.valueOf(marker.getTag())); // Problem with default marker ?
            postOne(markerMap);
        }

        for (Polyline line : lines){
            HashMap<String, String> lineMap = new HashMap<>(metaData);
            lineMap.put("type", "line");
            String points = "";
            for (LatLng point : line.getPoints()){
                points = points.concat("("
                        + String.valueOf(point.latitude)
                        + ", "
                        + String.valueOf(point.longitude)
                        + ")");
            }
            points = points.concat("!");
            lineMap.put("points", points);
            lineMap.put("strokeColour", String.valueOf(line.getColor()));
            lineMap.put("strokePattern", String.valueOf(line.getTag()));
            lineMap.put("strokeWidth", String.valueOf(line.getWidth()));
            postOne(lineMap);
        }

        for (Polyline drawing : lines){
            HashMap<String, String> drawingMap = new HashMap<>(metaData);
            drawingMap.put("type", "drawing");
            String points = "";
            for (LatLng point : drawing.getPoints()){
                points = points.concat("(" + String.valueOf(point.latitude) + ", " + String.valueOf(point.longitude) + ")");
            }
            points = points.concat("!");
            drawingMap.put("points", points);
            drawingMap.put("strokeColour", String.valueOf(drawing.getColor()));
            drawingMap.put("strokePattern", String.valueOf(drawing.getTag()));
            drawingMap.put("strokeWidth", String.valueOf(drawing.getWidth()));
            postOne(drawingMap);
        }

    }

    private void enterMetaData(){
        usernamePasswordEntry.getView().setVisibility(View.VISIBLE);
    }

    public void setMetaData(String username, String password, String title){
        this.username = username;
        this.password = password;
        this.title = title;
    }

    public void setEditable(){
        canEdit = true;
        annotateMapButton.setText(R.string.annotate_map);
        postButton.setVisibility(View.VISIBLE);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAll();
            }
        });
    }



    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}