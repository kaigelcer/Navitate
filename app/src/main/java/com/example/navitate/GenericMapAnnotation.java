package com.example.navitate;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

// Class to store info from result of HTTP Get
public class GenericMapAnnotation {

    //Metadata
    String username;
    String title;
    String password;

    // All
    String type;

    // Circle only
    String radius;
    String fillColour;

    // Marker only
    String colour;

    //Circle or Marker
    String latitude;
    String longitude;

    //Line or Drawing
    String points;

    //Circle, line, or drawing
    String strokeColour;
    String strokeWidth;
    String strokePattern;

    public GenericMapAnnotation(){

    }

    public CircleOptions createCircle(){
        System.out.println(radius);
        System.out.println(fillColour);
        System.out.println(latitude);
        System.out.println(longitude);
        System.out.println(strokeColour);
        System.out.println(strokeWidth);
        System.out.println(strokePattern);
        CircleOptions circleOptions = new CircleOptions().radius(Double.parseDouble(radius))
                .fillColor(Integer.parseInt(fillColour))
                .center(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                .strokeColor(Integer.parseInt(strokeColour))
                .strokeWidth(Float.parseFloat(strokeWidth))
                .strokePattern(parsePattern(strokePattern));
        return circleOptions;
    }

    public PolylineOptions createLine(){
        PolylineOptions lineOptions = new PolylineOptions().color(Integer.parseInt(strokeColour))
                .width(Float.parseFloat(strokeWidth))
                .pattern(parsePattern(strokePattern))
                .addAll(parsePoints(points));
        System.out.println(points);
        System.out.println(strokeColour);
        System.out.println(strokeWidth);
        System.out.println(strokePattern);
        return lineOptions;
    }



    public PolylineOptions createDrawing(){
        PolylineOptions drawingOptions = new PolylineOptions().color(Integer.parseInt(strokeColour))
                .width(Float.parseFloat(strokeWidth))
                .pattern(parsePattern(strokePattern))
                .addAll(parsePoints(points));
        System.out.println(points);
        System.out.println(strokeColour);
        System.out.println(strokeWidth);
        System.out.println(strokePattern);
        return drawingOptions;
    }

    public MarkerOptions createMarker(){
        System.out.println(latitude);
        System.out.println(longitude);
        System.out.println(colour);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                .icon(BitmapDescriptorFactory.defaultMarker(Float.parseFloat(colour)));

        return markerOptions;

    }

    //Get pattern from string describing pattern
    public List<PatternItem> parsePattern(String patternTag){
        List<PatternItem> pattern = new LinkedList<>();
        switch (patternTag) {
            case "straight":
                pattern = null;
                break;
            case "dashed":
                pattern.add(new Dot());
                pattern.add(new Gap(Float.parseFloat(strokeWidth) * 3));
                break;
            case "dotted":
                pattern.add(new Dash(Float.parseFloat(strokeWidth) * 4));
                pattern.add(new Gap(Float.parseFloat(strokeWidth) * 4));
                break;
        }

        return pattern;
    }

    private Iterable<LatLng> parsePoints(String points) {
        List<LatLng> pointsList = new LinkedList<>();
        boolean onLat = true; // Are we parsing latitude currently?
        double latitude = 0;
        double longitude = 0;
        String latitudeString = "";
        String longitudeString = "";
        for (char character : points.toCharArray()){
            if (character == '!'){
                break;
            } else if (character == '('){
                latitudeString = "";
                onLat = true;
            } else if (character == ','){
                latitude = Double.parseDouble(latitudeString);
                longitudeString = "";
                onLat = false;
            } else if (character == ')'){
                longitude = Double.parseDouble(longitudeString);
                pointsList.add(new LatLng(latitude, longitude));
            } else {
                if (onLat){
                    latitudeString += character;
                } else {
                    longitudeString += character;
                }
            }
        }

        return pointsList;
    }

    public String getUsername(){
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getPassword() {
        return password;
    }



}
