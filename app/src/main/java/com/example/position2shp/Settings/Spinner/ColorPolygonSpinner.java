package com.example.position2shp.Settings.Spinner;

import android.view.View;
import android.widget.AdapterView;

import com.example.position2shp.MainActivity;
import com.example.position2shp.Map.TrackingColour;

public class ColorPolygonSpinner implements AdapterView.OnItemSelectedListener{

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        TrackingColour refSystem = TrackingColour.values()[pos];
        switch (refSystem){
            case RED:
                MainActivity.colorPolygon = TrackingColour.RED;
                break;
            case YELLOW:
                MainActivity.colorPolygon = TrackingColour.YELLOW;
                break;
            case BLUE:
                MainActivity.colorPolygon = TrackingColour.BLUE;
                break;
            case GREEN:
                MainActivity.colorPolygon = TrackingColour.GREEN;
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
