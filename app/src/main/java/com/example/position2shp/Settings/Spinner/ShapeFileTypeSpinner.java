package com.example.position2shp.Settings.Spinner;

import android.view.View;
import android.widget.AdapterView;

import com.example.position2shp.MainActivity;
import com.example.position2shp.Map.ShapefileTypes;

public class ShapeFileTypeSpinner implements AdapterView.OnItemSelectedListener{

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        ShapefileTypes type = ShapefileTypes.values()[pos];
                switch(type) {
                    case Point:
                        MainActivity.actualShpType = ShapefileTypes.Point;
                        break;
                    case Line:
                        MainActivity.actualShpType = ShapefileTypes.Line;
                        break;
                    case Polygon:
                        MainActivity.actualShpType = ShapefileTypes.Polygon;
                        break;
                }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
