package com.example.position2shp.Map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.position2shp.MainActivity;
import com.example.position2shp.R;
import com.example.position2shp.Settings.Settings;
import com.example.position2shp.Util.PositionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapEditFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private LocationDisplay mLocationDisplay;
    private MapView mMapView;
    private ArcGISMap map;
    double latitude = 47.5;
    double longitude = 9.01;
    private int selectedPointColor = Color.RED;
    private int selectedLineColor = Color.RED;
    private int selectedPolygonColor = Color.RED;

    Settings settings;

    public MapEditFragment() {
        super(R.layout.mapeditview);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(Objects.requireNonNull(e.getEnumConstants())).map(Enum::name).toArray(String[]::new);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.mapeditview, container, false);
        return rootView;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settings = new Settings();
        if (getArguments() != null) {
            settings = com.example.position2shp.Map.MapEditFragmentArgs.fromBundle(getArguments()).getSettings();
        }

        Button btnSaveEdit = requireView().findViewById(R.id.btnSaveEdit);
        //btnSaveEdit.setOnClickListener(this::positionTracking);
        btnSaveEdit.setEnabled(!MainActivity.mPositionTrackingShapefilePath.isEmpty());

        Button btnCancel = requireView().findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            com.example.position2shp.Map.MapEditFragmentDirections.ActionMapEditFragmentToMapFragment actionToEditFragment = MapEditFragmentDirections.actionMapEditFragmentToMapFragment();
            actionToEditFragment.setSettings(settings);
            navController.navigate(actionToEditFragment);
            });

        Button btnCenter = requireView().findViewById(R.id.btnCenter2);
        btnCenter.setOnClickListener(v -> centerMapView());

        createExistingShapefilesArrayAdapter(view);

        // initialize map that shows tracking and shapefiles
        initializeMap();
    }

    private void setGeometryColors()
    {
        if (settings.pointColor == TrackingColour.RED.ordinal())
            selectedPointColor = Color.RED;
        else if (settings.pointColor == TrackingColour.YELLOW.ordinal())
            selectedPointColor = Color.YELLOW;
        else if (settings.pointColor == TrackingColour.GREEN.ordinal())
            selectedPointColor = Color.GREEN;
        else if (settings.pointColor == TrackingColour.BLUE.ordinal())
            selectedPointColor = Color.BLUE;

        if (settings.lineColor == TrackingColour.RED.ordinal())
            selectedLineColor = Color.RED;
        else if (settings.lineColor == TrackingColour.YELLOW.ordinal())
            selectedLineColor = Color.YELLOW;
        else if (settings.lineColor == TrackingColour.GREEN.ordinal())
            selectedLineColor = Color.GREEN;
        else if (settings.lineColor == TrackingColour.BLUE.ordinal())
            selectedLineColor = Color.BLUE;

        if (MainActivity.colorPolygon == TrackingColour.RED)
            selectedPolygonColor = Color.RED;
        else if (MainActivity.colorPolygon == TrackingColour.YELLOW)
            selectedPolygonColor = Color.YELLOW;
        else if (MainActivity.colorPolygon == TrackingColour.GREEN)
            selectedPolygonColor = Color.GREEN;
        else if (MainActivity.colorPolygon == TrackingColour.BLUE)
            selectedPolygonColor = Color.BLUE;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeBackgroundShpFile(String fileName) {

        File f = new File(fileName);
        if (f.exists() && f.canRead()) {
            // create the Symbol
            String shpFile = fileName.replace(".shp", "");
            int shapeFileType = PositionUtils.getShapeFileType(shpFile);

            // create the Renderer
            SimpleRenderer renderer = new SimpleRenderer();
            setGeometryColors();

            if (shapeFileType == 3)
            {
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, selectedLineColor, 1.0f);
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.TRANSPARENT, lineSymbol);

                renderer.setSymbol(fillSymbol);
            }
            else if (shapeFileType == 5)
            {
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, selectedPolygonColor, 1.0f);
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.TRANSPARENT, lineSymbol);

                renderer.setSymbol(fillSymbol);
            }
            else if (shapeFileType == 1)
            {
                SimpleMarkerSymbol pointSymbol =
                    new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, selectedPointColor, 10f);
                renderer.setSymbol(pointSymbol);
            }

			Shapefile backgroundLayer = new Shapefile(map, fileName, renderer);
            backgroundLayer.load();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeMap() {
        mMapView = requireView().findViewById(R.id.mapView2);

        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, latitude, longitude, 16);
        mMapView.setMap(map);
        mLocationDisplay = mMapView.getLocationDisplay();
        centerMapView();
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

        Spinner existingShapefilesSpinner = requireView().findViewById(R.id.spinnerSelectShapefile);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, existingShapefiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        existingShapefilesSpinner.setAdapter(spinnerArrayAdapter);

        existingShapefilesSpinner.setOnItemSelectedListener(this);
    }

    public void centerMapView() {
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        if (!mLocationDisplay.isStarted())
            mLocationDisplay.startAsync();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        final int viewId = adapterView.getId();
        if (viewId == R.id.spinnerSelectShapefile) {
            String existingShapefileName = adapterView.getSelectedItem().toString();

            String externalFilesDir = settings.externalFilesDir + File.separator + existingShapefileName;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                initializeBackgroundShpFile(externalFilesDir);
            }
            String shpFile = externalFilesDir.replace(".shp", "");

            String[] fields = PositionUtils.getShpFieldValues(shpFile);

            TableRow row = requireView().findViewById(R.id.trHeader);
            row.removeAllViews();
            int screenWidth = this.getResources().getDisplayMetrics().widthPixels - 20;
            double textViewWidth = (1.0 / fields.length) * screenWidth;

            for(String s : fields)
                {
                    // Inflate your row "template" and fill out the fields.
                    TextView tv1 = new TextView(requireActivity());
                    tv1.setText(s);
                    tv1.setTextColor(Color.BLACK);
                    tv1.setTextSize(15);
                    tv1.setPadding(0, 25, 0, 25);
                    tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    tv1.setWidth((int)textViewWidth);
                    tv1.setGravity(Gravity.CENTER);
                    //tv1.setLayoutParams(new TableRow.LayoutParams(colCount));
                    row.addView(tv1);
                }

            int recordCount = PositionUtils.getShpRecordCount(shpFile);
            int[] values = PositionUtils.getShpFieldTypes(shpFile);

            TableLayout table = requireView().findViewById(R.id.tlAttributes);
            table.removeAllViews();
            for (int rowId = 0; rowId < recordCount; rowId++)
            {
                TableRow row2 = new TableRow(requireActivity());
                table.addView(row2);
            }

            for (int j = 0; j < values.length; j++)
            {
                if (values[j] == 0) //String
                {
                    String[] attributes = PositionUtils.getStringAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++)
                    {
                        String s = attributes[k];
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int)textViewWidth);
                        tv1.setGravity(Gravity.CENTER);
                        //tv1.setLayoutParams(new TableRow.LayoutParams(j));
                        row2.addView(tv1);
                    }
                }

                else if (values[j] == 1) //int
                {
                    int[] attributes = PositionUtils.getIntAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++)
                    {
                        String s = String.valueOf(attributes[k]);
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int)textViewWidth);
                        tv1.setGravity(Gravity.CENTER);
                        //tv1.setLayoutParams(new TableRow.LayoutParams(j));
                        row2.addView(tv1);
                    }
                }

                else if (values[j] == 2) //double
                {
                    double[] attributes = PositionUtils.getDoubleAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++)
                    {
                        String s = String.valueOf(attributes[k]);
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int)textViewWidth);
                        tv1.setGravity(Gravity.CENTER);
                        //tv1.setLayoutParams(new TableRow.LayoutParams(j));
                        row2.addView(tv1);
                    }
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
