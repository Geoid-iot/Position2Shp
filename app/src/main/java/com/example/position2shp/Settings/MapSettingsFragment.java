package com.example.position2shp.Settings;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.position2shp.MainActivity;
import com.example.position2shp.Map.MapFragment;
import com.example.position2shp.Map.Shapefile;
import com.example.position2shp.Map.TrackingColour;
import com.example.position2shp.R;
import com.example.position2shp.Settings.Spinner.ColorPolygonSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapSettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public Settings settings;

    public MapSettingsFragment(Settings settings) {
        super(R.layout.mapsettings);
        this.settings = settings;
    }

    public String getBackgroundShapefile()
    {
        return settings.backgroundShpFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createExistingShapefilesArrayAdapter(view);

        createPointColorSpinner(view);

        createLineColorSpinner(view);

        createPolygonColorSpinner(view);
    }

    private void createPointColorSpinner(View view){
        //create dropdown with available reference systems
        String[] colors = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            colors = MapFragment.getNames(TrackingColour.class);
        }

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerPointColor);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        colorSpinner.setSelection(settings.pointColor);
    }

    private void createLineColorSpinner(View view){
        //create dropdown with available reference systems
        String[] colors = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            colors = MapFragment.getNames(TrackingColour.class);
        }

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerLineColor);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        colorSpinner.setSelection(settings.lineColor);
    }

    private void createPolygonColorSpinner(View view){
        //create dropdown with available reference systems
        String[] colors = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            colors = MapFragment.getNames(TrackingColour.class);
        }

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerPolygonFillColor);
        colorSpinner.setAdapter(colorAdapter);

        ColorPolygonSpinner colorPolygonSpinner = new ColorPolygonSpinner();
        colorSpinner.setOnItemSelectedListener(colorPolygonSpinner);
        colorSpinner.setSelection(MainActivity.colorPolygon.ordinal());
    }

    private void createExistingShapefilesArrayAdapter(View view){
        //ToDo: Let user select destination folder https://developer.android.com/training/data-storage/shared/documents-files
        //create dropdown with existing shapefiles
        String filepath = settings.externalFilesDir + getResources().getString(R.string.UserShapefiles);
        List<String> fileList = new ArrayList<>();
        Shapefile.collectShapefilesInList(fileList, filepath);

        filepath = settings.externalFilesDir + getResources().getString(R.string.NewShapefiles);
        Shapefile.collectShapefilesInList(fileList, filepath);

        filepath = settings.externalFilesDir + "/Kreise_BW/";
        Shapefile.collectShapefilesInList(fileList, filepath);

        String[] existingShapefiles = new String[fileList.size()];
        existingShapefiles = fileList.toArray(existingShapefiles);

        Spinner existingShapefilesSpinner = requireView().findViewById(R.id.spinnerBackgroundShapefiles);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, existingShapefiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        existingShapefilesSpinner.setAdapter(spinnerArrayAdapter);

        existingShapefilesSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final int viewId = parent.getId();
        if (viewId == R.id.spinnerBackgroundShapefiles) {
            //Todo: in spinner nur shp file ordner anzeigen, hier dann das .shp dazu suchen
            //ToDo: Fehlermeldung (Toast) wenn files nicht gefunden
            String selectedShapefile = parent.getItemAtPosition(position).toString();
            settings.backgroundShpFile = settings.externalFilesDir + File.separator + selectedShapefile;
        }
        else if (viewId == R.id.spinnerLineColor) {
            TrackingColour refSystem = TrackingColour.values()[position];
            switch (refSystem){
                case RED:
                    settings.lineColor = TrackingColour.RED.ordinal();
                    break;
                case YELLOW:
                    settings.lineColor = TrackingColour.YELLOW.ordinal();
                    break;
                case BLUE:
                    settings.lineColor = TrackingColour.BLUE.ordinal();
                    break;
                case GREEN:
                    settings.lineColor = TrackingColour.GREEN.ordinal();
                    break;
            }
        }
        else if (viewId == R.id.spinnerPointColor) {
            TrackingColour refSystem = TrackingColour.values()[position];
            switch (refSystem){
                case RED:
                    settings.pointColor = TrackingColour.RED.ordinal();
                    break;
                case YELLOW:
                    settings.pointColor = TrackingColour.YELLOW.ordinal();
                    break;
                case BLUE:
                    settings.pointColor = TrackingColour.BLUE.ordinal();
                    break;
                case GREEN:
                    settings.pointColor = TrackingColour.GREEN.ordinal();
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
