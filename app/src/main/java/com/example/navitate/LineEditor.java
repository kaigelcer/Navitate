package com.example.navitate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class LineEditor extends Fragment {

    float defaultWidth;
    List<PatternItem> pattern;
    String patternTag;
    int defaultColour;
    String type = "line";
    private EditText widthEntry;
    private int fillColour;
    private TextView fillColourText;
    private Button fillColourButton;
    private static final int STROKE = 1;
    private static final int FILL = 2;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        defaultColour = Color.BLACK;
        fillColour = Color.WHITE;
        defaultWidth = 5;
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinkedList<PatternItem> straight = null;
        pattern = straight;
        patternTag = "straight";
        LinkedList<PatternItem> dashed = new LinkedList<>();
        dashed.add(new Dash(20));
        dashed.add(new Gap(20));
        LinkedList<PatternItem> dotted = new LinkedList<>();
        dotted.add(new Dot());
        dotted.add(new Gap(20));
        PolylineOptions lineOptions = new PolylineOptions()
                .width(5)
                .pattern(null)
                .color(Color.BLACK);

        // Set Up Width Entry
        widthEntry = view.findViewById(R.id.width_entry);
        widthEntry.setText("5");
        widthEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (widthEntry.getText().toString().equals("")){
                    widthEntry.setText("5");
                }
                dashed.clear();
                float width = Float.parseFloat(widthEntry.getText().toString());
                dashed.add(new Dash(width*4));
                dashed.add(new Gap(width*4));
                dotted.clear();
                dotted.add(new Dot());
                dotted.add(new Gap(width*3));
                return false;
            }
        });

        // Set Up Pattern Spinner
        Spinner strokeSpinner = view.findViewById(R.id.stroke_spinner);
        List<String> patternOptions = new ArrayList<String>();
        patternOptions.add("____");
        patternOptions.add("------");
        patternOptions.add("......");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, patternOptions);
        strokeSpinner.setAdapter(dataAdapter);
        strokeSpinner.setPrompt("____");
        strokeSpinner.setEnabled(true);
        strokeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (strokeSpinner.getSelectedItem().equals("____")){
                    pattern = straight;
                    patternTag = "straight";
                } else if (strokeSpinner.getSelectedItem().equals("------")){
                    pattern = dashed;
                    patternTag = "dashed";
                } else if (strokeSpinner.getSelectedItem().equals("......")){
                    patternTag = "dotted";
                    pattern = dotted;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set up Colour picker 1
        Button colourPickerButton = view.findViewById(R.id.openColorButton);
        colourPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColourPicker(STROKE);
            }
        });

        // Colour picker 2
        fillColourText = view.findViewById(R.id.fillColorView);
        fillColourButton = view.findViewById(R.id.openColorButton2);
        fillColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColourPicker(FILL);
            }
        });


        // Set Up Draw Button
        Button goButton = view.findViewById(R.id.go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("width", widthEntry.getText().toString());
                args.putString("pattern", strokeSpinner.getSelectedItem().toString());
                args.putString("colour", Integer.toString(defaultColour));
                setArguments(args);
                startDrawing();
            }
        });
    }

    private void openColourPicker(int strokeOrFill) {
        AmbilWarnaDialog colourPicker = new AmbilWarnaDialog(this.getContext(), defaultColour, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                if (strokeOrFill == STROKE) {
                    defaultColour = color;

                } else if (strokeOrFill == FILL){
                    fillColour = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));

                }
            }
        });
        colourPicker.show();
    }

    private void startDrawing(){
        getView().setVisibility(View.GONE);
        MapsActivity activity = (MapsActivity) this.getActivity();
        assert activity != null;
        if (type.equals("line")){
            activity.lineClickDraw(defaultColour, Float.parseFloat(widthEntry.getText().toString()), pattern, patternTag);
        } else if (type.equals("circle")){
            activity.circleClickDraw(defaultColour, Float.parseFloat(widthEntry.getText().toString()), pattern, patternTag, fillColour);
        } else {
            activity.clickDraw(defaultColour, Float.parseFloat(widthEntry.getText().toString()), pattern, patternTag);
        }

    }

    public void setType(String drawingType){
        type = drawingType;
        if (type.equals("draw") || type.equals("line")){
            fillColourButton.setVisibility(View.GONE);
            fillColourText.setVisibility(View.GONE);
        }
    }
}