package com.example.position2shp.Map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.example.position2shp.Map.FileParser.ShapefileReader;
import com.example.position2shp.R;
import com.example.position2shp.Settings.MapSettingsFragment;
import com.example.position2shp.Settings.Settings;
import com.example.position2shp.Util.PositionUtils;

import org.cts.crs.CoordinateReferenceSystem;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapEditFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    double latitude = 47.5;
    double longitude = 9.01;
    List<Map.Entry<Integer, List<Integer>>> mapping;
    Settings settings;
    private MapView mMapView;
    private IMapController mapController;
    private MyLocationNewOverlay mLocationOverlay;

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
        btnSaveEdit.setEnabled(!settings.positionTrackingShapefilePath.isEmpty());

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeBackgroundShpFile(String fileName) throws Exception {

        if (settings == null) return;

        List<Overlay> graphicOverlays = mMapView.getOverlays();

        for (int i = graphicOverlays.size() - 1; i > 0; i--) {
            mMapView.getOverlays().remove(graphicOverlays.get(i));
        }

        File f = new File(fileName);
        if (f.exists() && f.canRead()) {
            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            mMapView.setUseDataConnection(true);

            //ShapeConverter.convert();
            ShapefileReader shpReader = new ShapefileReader(requireContext());
            mapping = shpReader.attributeToOverlayMapping;
            List<PointData[]> points = shpReader.convert(f);
            ShapeType mShapeType = shpReader.mShapeType;

            String csNameSrc = "EPSG:25832";
            CoordinateReferenceSystem crs = shpReader.getCRS(f);
            String name = crs.getName();

            List<Overlay> folder = new ArrayList<>();
            if (name.contains("ETRS89") && name.contains("UTM") && name.contains("32N")) {
                folder = shpReader.transformToWGS84(mMapView, mShapeType, points, csNameSrc, settings);
            } else if (name.contains("GCS_WGS_1984")) {
                folder = shpReader.getOverlay(mMapView, points, mShapeType, settings);
            }

            mMapView.getOverlayManager().addAll(folder);
            mMapView.invalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeMap() {
        mMapView = requireView().findViewById(R.id.mapView2);

        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mMapView.setMultiTouchControls(true);

        mapController = mMapView.getController();
        mapController.setZoom(16.0);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();

        mMapView.getOverlays().add(mLocationOverlay);
        mapController.setCenter(new GeoPoint(latitude, longitude));
    }

    private void createExistingShapefilesArrayAdapter(View view) {
        //create dropdown with existing shapefiles
        List<String> fileList = new ArrayList<>();

        String filepath = settings.externalFilesDir + getResources().getString(R.string.new_shp);
        MapSettingsFragment.collectShapefilesInList(fileList, filepath);

        filepath = settings.externalFilesDir + getResources().getString(R.string.background_shp);
        MapSettingsFragment.collectShapefilesInList(fileList, filepath);

        String[] existingShapefiles = new String[fileList.size()];
        existingShapefiles = fileList.toArray(existingShapefiles);

        Spinner existingShapefilesSpinner = requireView().findViewById(R.id.spinnerSelectShapefile);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, existingShapefiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        existingShapefilesSpinner.setAdapter(spinnerArrayAdapter);

        existingShapefilesSpinner.setOnItemSelectedListener(this);
    }

    public void centerMapView() {
        mapController = mMapView.getController();
        mapController.setZoom(18.0);

        mapController.setCenter(mLocationOverlay.getMyLocation());
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        final int viewId = adapterView.getId();
        if (viewId == R.id.spinnerSelectShapefile) {
            String existingShapefileName = adapterView.getSelectedItem().toString();

            String externalFilesDir = settings.externalFilesDir + File.separator + existingShapefileName;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    initializeBackgroundShpFile(externalFilesDir);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            String shpFile = externalFilesDir.replace(".shp", "");

            String[] fields = PositionUtils.getShpFieldValues(shpFile);

            TableRow row = requireView().findViewById(R.id.trHeader);
            row.removeAllViews();
            int screenWidth = this.getResources().getDisplayMetrics().widthPixels - 20;
            double textViewWidth = (1.0 / fields.length) * screenWidth;

            for (String s : fields) {
                // Inflate your row "template" and fill out the fields.
                TextView tv1 = new TextView(requireActivity());
                tv1.setText(s);
                tv1.setTextColor(Color.BLACK);
                tv1.setTextSize(15);
                tv1.setPadding(0, 25, 0, 25);
                tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                tv1.setWidth((int) textViewWidth);
                tv1.setGravity(Gravity.CENTER);
                //tv1.setLayoutParams(new TableRow.LayoutParams(colCount));
                row.addView(tv1);
            }

            int recordCount = PositionUtils.getShpRecordCount(shpFile);
            int[] values = PositionUtils.getShpFieldTypes(shpFile);

            TableLayout table = requireView().findViewById(R.id.tlAttributes);
            table.removeAllViews();
            for (int rowId = 0; rowId < recordCount; rowId++) {
                TableRow row2 = new TableRow(requireActivity());
                row2.setTag(rowId);
                row2.setOnClickListener(v -> {
                    TableRow tr = (TableRow) v;

                    int id = (int) v.getTag();

                    int amountOverlays = mMapView.getOverlays().size();
                    List<Integer> lastItem = mapping.get(mapping.size()-1).getValue();
                    int amountGeometries = lastItem.get(lastItem.size() -1) + 1;

                    if (amountGeometries < amountOverlays - 1) //subtract location overlay
                    {
                        int overlySize = mMapView.getOverlays().size()-1;
                        while (overlySize > amountGeometries) {
                            mMapView.getOverlayManager().remove(mMapView.getOverlays().size() - 1);
                            mMapView.invalidate();
                            overlySize = mMapView.getOverlays().size() - 1;
                        }
                    }

                    List<Integer> testMap = mapping.get(id).getValue();
                    for (int index : testMap) {

                        Overlay overlay = mMapView.getOverlayManager().get(index + 1);
                        if (overlay.getClass() == Polygon.class) {
                            Polygon p = new Polygon();
                            p.setPoints(((Polygon) overlay).getActualPoints());
                            p.getOutlinePaint().setColor(Color.MAGENTA);
                            mMapView.getOverlayManager().add(p);
                            mMapView.invalidate();
                        }
                        else if (overlay.getClass() == Polyline.class) {
                            Polyline p = new Polyline();
                            p.setPoints(((Polyline) overlay).getActualPoints());
                            p.getOutlinePaint().setColor(Color.MAGENTA);
                            mMapView.getOverlayManager().add(p);
                            mMapView.invalidate();
                        }
                        else if (overlay.getClass() == Marker.class) {
                            Marker m = new Marker(mMapView);
                            m.setPosition(((Marker) overlay).getPosition());
                            m.setIcon(requireContext().getResources().getDrawable(R.drawable.baseline_circle_24));
                            m.getIcon().setColorFilter(Color.MAGENTA, PorterDuff.Mode.MULTIPLY);
                            mMapView.getOverlayManager().add(m);
                            mMapView.invalidate();
                        }
                    }

                });
                table.addView(row2);
            }

            for (int j = 0; j < values.length; j++) {
                if (values[j] == 0) //String
                {
                    String[] attributes = PositionUtils.getStringAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++) {
                        String s = attributes[k];
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int) textViewWidth);
                        tv1.setGravity(Gravity.CENTER);
                        //tv1.setLayoutParams(new TableRow.LayoutParams(j));
                        row2.addView(tv1);
                    }
                } else if (values[j] == 1) //int
                {
                    int[] attributes = PositionUtils.getIntAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++) {
                        String s = String.valueOf(attributes[k]);
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int) textViewWidth);
                        tv1.setGravity(Gravity.CENTER);
                        //tv1.setLayoutParams(new TableRow.LayoutParams(j));
                        row2.addView(tv1);
                    }
                } else if (values[j] == 2) //double
                {
                    double[] attributes = PositionUtils.getDoubleAttributeTable(shpFile, j);
                    for (int k = 0; k < attributes.length; k++) {
                        String s = String.valueOf(attributes[k]);
                        TableRow row2 = (TableRow) table.getChildAt(k);
                        TextView tv1 = new TextView(requireActivity());
                        tv1.setText(s);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTextSize(15);
                        tv1.setPadding(0, 25, 0, 25);
                        //tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
                        tv1.setWidth((int) textViewWidth);
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
