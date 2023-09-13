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

import com.example.position2shp.Map.MapFragment;
import com.example.position2shp.Map.TrackingColour;
import com.example.position2shp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapSettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private final Settings settings;

    public MapSettingsFragment(Settings settings) {
        super(R.layout.mapsettings);
        this.settings = settings;
    }

    public Settings getSettings(){
        return settings;
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

        createPolygonFillColorSpinner(view);

        createPolygonLineColorSpinner(view);
    }

    private String[] createColorArray(){
        //create dropdown with available reference systems
        String[] colors = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            colors = MapFragment.getNames(TrackingColour.class);
        }
        return colors;
    }

    private ArrayAdapter<String> createColorArrayAdapter(View view, String[] colors){
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        return colorAdapter;
    }

    private void createPointColorSpinner(View view){
        String[] colors = createColorArray();
        ArrayAdapter<String> colorAdapter = createColorArrayAdapter(view, colors);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerPointColor);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        int idx = Arrays.asList(colors).indexOf(TrackingColour.fromValue(settings.pointColor).toString());
        colorSpinner.setSelection(idx);
    }

    private void createLineColorSpinner(View view){
        String[] colors = createColorArray();
        ArrayAdapter<String> colorAdapter = createColorArrayAdapter(view, colors);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerLineColor);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        int idx = Arrays.asList(colors).indexOf(TrackingColour.fromValue(settings.lineColor).toString());
        colorSpinner.setSelection(idx);
    }

    private void createPolygonFillColorSpinner(View view){
        String[] colors = createColorArray();
        ArrayAdapter<String> colorAdapter = createColorArrayAdapter(view, colors);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerPolygonFillColor);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        int idx = Arrays.asList(colors).indexOf(TrackingColour.fromValue(settings.polygonFillColour).toString());
        colorSpinner.setSelection(idx);
    }

    private void createPolygonLineColorSpinner(View view){
        String[] colors = createColorArray();
        ArrayAdapter<String> colorAdapter = createColorArrayAdapter(view, colors);

        Spinner colorSpinner = requireView().findViewById(R.id.spinnerPolygonLineColour);
        colorSpinner.setAdapter(colorAdapter);

        colorSpinner.setOnItemSelectedListener(this);
        int idx = Arrays.asList(colors).indexOf(TrackingColour.fromValue(settings.polygonLineColour).toString());
        colorSpinner.setSelection(idx);
    }

    // find all shapefiles in user folder
    public static void collectShapefilesInList(List<String> fileList, String filepath) {
        File f = new File(filepath);
        File[] files = f.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    File[] files2 = inFile.listFiles();
                    if (files2 != null) {
                        for (File inFile2 : files2) {
                            if (inFile2.isFile() && inFile2.getAbsolutePath().contains(".shp")) {
                                String searchString = "/files/";
                                String parentPathName = inFile2.getParent();
                                String filename = null;
                                if (parentPathName != null) {
                                    filename = parentPathName.substring(parentPathName.indexOf(searchString) + searchString.length());
                                }

                                fileList.add(filename + File.separator + inFile2.getName());
                            }
                        }
                    }

                }
                else if (inFile.isFile() && inFile.getAbsolutePath().contains(".shp")) {
                    String searchString = "/files/";
                    String parentPathName = inFile.getParent();
                    String filename = null;
                    if (parentPathName != null) {
                        filename = parentPathName.substring(parentPathName.indexOf(searchString) + searchString.length());
                    }

                    fileList.add(filename + File.separator + inFile.getName());
                }
            }
        }
    }

    private void createExistingShapefilesArrayAdapter(View view){
        //create dropdown with existing shapefiles
        List<String> fileList = new ArrayList<>();
        fileList.add("---- None -----");

        String filepath = settings.externalFilesDir + getResources().getString(R.string.new_shp);
        collectShapefilesInList(fileList, filepath);

        filepath = settings.externalFilesDir + getResources().getString(R.string.background_shp);
        collectShapefilesInList(fileList, filepath);

        String[] existingShapefiles = new String[fileList.size()];
        existingShapefiles = fileList.toArray(existingShapefiles);

        Spinner existingShapefilesSpinner = requireView().findViewById(R.id.spinnerBackgroundShapefiles);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, existingShapefiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        existingShapefilesSpinner.setAdapter(spinnerArrayAdapter);

        int idx = 0;
        for (String file : fileList)
        {
            if (settings.backgroundShpFile.contains(file))
            {
                idx = fileList.indexOf(file);
            }
        }

        existingShapefilesSpinner.setSelection(idx);
        existingShapefilesSpinner.setOnItemSelectedListener(this);
    }

    private int getColor(int position){
        TrackingColour refSystem = TrackingColour.values()[position];
        int color = 0;
        switch (refSystem){
            case RED:
                color = TrackingColour.RED.getNumVal();
                break;
            case YELLOW:
                color = TrackingColour.YELLOW.getNumVal();
                break;
            case BLUE:
                color = TrackingColour.BLUE.getNumVal();
                break;
            case GREEN:
                color = TrackingColour.GREEN.getNumVal();
                break;
            case TRANSPERENT:
                color = TrackingColour.TRANSPERENT.getNumVal();
                break;
        }
        return color;
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
            settings.lineColor = getColor(position);
        }
        else if (viewId == R.id.spinnerPointColor) {
            settings.pointColor = getColor(position);
        }
        else if (viewId == R.id.spinnerPolygonLineColour) {
            settings.polygonLineColour = getColor(position);
        }
        else if (viewId == R.id.spinnerPolygonFillColor) {
            settings.polygonFillColour = getColor(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
