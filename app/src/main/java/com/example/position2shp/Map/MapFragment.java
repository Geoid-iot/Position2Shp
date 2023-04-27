package com.example.position2shp.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.util.ListenableList;
import com.example.position2shp.MainActivity;
import com.example.position2shp.R;
import com.example.position2shp.Settings.Settings;
import com.example.position2shp.Util.PositionUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment {
    final DecimalFormat df = new DecimalFormat("#.#####");
    final DecimalFormat dfUtm = new DecimalFormat("#.##");
    private PointCollection polyLineCollection;
    private LocationDisplay mLocationDisplay;
    private TextView textViewGpsX, textViewGpsY, textViewGpsZ;
    private boolean trackingStarted;
    private MapView mMapView;
    private ArcGISMap map;
    private Button btnStart;
    double latitude = 47.5;
    double longitude = 9.01;
    private Location previousLocation;
    private MyLocationListener myLocationListener;
    ArrayList<PositionUtils.Position> positions;
    private GraphicsOverlay overlay;
    private int selectedPointColor = Color.RED;
    private int selectedLineColor = Color.RED;
    private int selectedPolygonColor = Color.RED;
    private Settings settings;

    public MapFragment() {
        super(R.layout.mapview);

        previousLocation = new Location("Init Position");
        previousLocation.setLongitude(9.0);
        previousLocation.setLatitude(49.0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(Objects.requireNonNull(e.getEnumConstants())).map(Enum::name).toArray(String[]::new);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLocationListener = new MyLocationListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settings = new Settings();
        if (getArguments() != null) {
            settings = MapFragmentArgs.fromBundle(getArguments()).getSettings();
        }

        btnStart = requireView().findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this::positionTracking);
        btnStart.setActivated(!MainActivity.mPositionTrackingShapefilePath.isEmpty());

        Button btnClear = requireView().findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            ListenableList<GraphicsOverlay> graphicOverlays = mMapView.getGraphicsOverlays();
            for (GraphicsOverlay graphicOverlay : graphicOverlays) {
                graphicOverlay.getGraphics().clear();
            }
            });

        Button btnEdit = requireView().findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            com.example.position2shp.Map.MapFragmentDirections.ActionMapFragmentToMapEditFragment actionToEditFragment = MapFragmentDirections.actionMapFragmentToMapEditFragment();
            actionToEditFragment.setSettings(settings);
            navController.navigate(actionToEditFragment);
        });

        Button btnSettings = requireView().findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            com.example.position2shp.Map.MapFragmentDirections.ActionMapFragmentToSettingsContainerFragment actionToSettingsFragment = MapFragmentDirections.actionMapFragmentToSettingsContainerFragment();
            actionToSettingsFragment.setSettings(settings);
            navController.navigate(actionToSettingsFragment);
        });

        Button btnCenter = requireView().findViewById(R.id.btnCenter);
        btnCenter.setOnClickListener(v -> centerMapView());

        textViewGpsX = requireView().findViewById(R.id.tvCoordX);
        textViewGpsY = requireView().findViewById(R.id.tvCoordY);
        textViewGpsZ = requireView().findViewById(R.id.tvCoordZ);

        // Initialize Position Info that shows actual position coordinates
        initializePositionInfo();

        // initialize map that shows tracking and shapefiles
        initializeMap();

        initializeBackgroundShpFile();

        myLocationListener.start();
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
    private void initializeBackgroundShpFile() {
        if (settings == null)
            return;

        File f = new File(settings.backgroundShpFile);
        if (f.exists() && f.canRead()) {
            // create the Symbol
            String shpFile = settings.backgroundShpFile.replace(".shp", "");
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

			Shapefile backgroundLayer = new Shapefile(map, settings.backgroundShpFile, renderer);
            backgroundLayer.load();
        }

        File f2 = new File(MainActivity.mPositionTrackingShapefilePath);
        if (f2.exists() && f2.canRead()) {
            // create the Symbol
            String shpFile = MainActivity.mPositionTrackingShapefilePath.replace(".shp", "");
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

            Shapefile backgroundLayer = new Shapefile(map, MainActivity.mPositionTrackingShapefilePath, renderer);
            backgroundLayer.load();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeMap() {
        mMapView = requireView().findViewById(R.id.mapView);

        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, latitude, longitude, 16);
        mMapView.setMap(map);
        mLocationDisplay = mMapView.getLocationDisplay();
        centerMapView();

        mMapView.setOnTouchListener(
                new DefaultMapViewOnTouchListener(requireContext(), mMapView) {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
						/*android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
								Math.round(motionEvent.getY()));
						Point newPoint = mMapView.screenToLocation(screenPoint);*/
                        return true;
                    }
                });
    }

    private void initializePositionInfo() {
        // UI elements for displaying GPS coordinates in different coordinate systems
        if (settings != null) {
            TextView label = requireView().findViewById(R.id.tvReferenceSystem);
            if (settings.refSystem == ReferenceSystems.WGS84.ordinal())
                label.setText(R.string.wgs84);
            else if (settings.refSystem == ReferenceSystems.UTM.ordinal())
                label.setText(R.string.utm);
        }
    }

    public void positionTracking(View view) {
        if (view.getId() == R.id.btnStart) {
            if (!view.isActivated())
            {
                CharSequence text = "Please select or create a shapefile in the settings first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(getContext(), text, duration);
                toast.show();
                return;
            }
            if (!trackingStarted) {
                overlay = new GraphicsOverlay();
                mMapView.getGraphicsOverlays().add(overlay);
				//ToDo: was passiert mit dem Tracking wenn man das Fragment verlässt? static !?!?

                if (settings.refSystem == ReferenceSystems.UTM.ordinal())
                    polyLineCollection = new PointCollection(SpatialReference.create(25832));
                else if (settings.refSystem == ReferenceSystems.WGS84.ordinal())
                    polyLineCollection = new PointCollection(SpatialReference.create(4326));

                trackingStarted = true;
                btnStart.setBackground(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.stop_black, null));

                setGeometryColors();
            } else {
                trackingStarted = false;

                if ((polyLineCollection.size() >= 2 && MainActivity.actualShpType == ShapefileTypes.Line) ||
                        MainActivity.actualShpType == ShapefileTypes.Point) {
                    int i = 0;
                    positions = new ArrayList<>();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    for (Point p : polyLineCollection) {
                        positions.add(i, new PositionUtils.Position(p.getY(), p.getX(), formatter.format(date)));
                        i++;
                    }
                    PositionUtils.appendShapefile(positions);
                }
                else if ((polyLineCollection.size() >= 4 && MainActivity.actualShpType == ShapefileTypes.Polygon)) {
                    int i = 0;
                    positions = new ArrayList<>();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    for (Point p : polyLineCollection) {
                        positions.add(i, new PositionUtils.Position(p.getY(), p.getX(), formatter.format(date)));
                        i++;
                    }
                    Point p0 = polyLineCollection.get(0);
                    positions.add(i, new PositionUtils.Position(p0.getY(), p0.getX(), formatter.format(date)));
                    PositionUtils.appendShapefile(positions);
                }
                else if ((polyLineCollection.size() < 4 && MainActivity.actualShpType == ShapefileTypes.Polygon))
                {
                    CharSequence text = "Not enough points for polygon feature!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();
                }
                else if (polyLineCollection.size() < 2 && MainActivity.actualShpType == ShapefileTypes.Line)
                {
                    CharSequence text = "Not enough points for line feature!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();
                }
                else if (polyLineCollection.isEmpty())
                {
                    CharSequence text = "No points tracked yet!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();
                }

                btnStart.setBackground(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.start_selector, null));
            }
        }
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
        myLocationListener.remove();
    }

    class MyLocationListener implements LocationListener {

        LocationManager mLocationManager;

        @RequiresApi(api = Build.VERSION_CODES.Q)
        MyLocationListener(){
            mLocationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager != null) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            1);
                }
            }
        }

        @SuppressLint("MissingPermission")
        void start(){
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, this);
        }

        @SuppressLint("LongLogTag")
        public void remove(){
            Log.d("Position2ShpLocationListener", "Stop location listener");
            mLocationManager.removeUpdates(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onLocationChanged(@NonNull Location location) {

            if (settings == null)
                return;

            try {
                Location gps_location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                location.equals(gps_location);

                if (gps_location != null) {
                    final double latitudeNew = gps_location.getLatitude();
                    final double longitudeNew = gps_location.getLongitude();
                    float distance = 0.0f;
                    if (previousLocation != null)
                        distance = gps_location.distanceTo(previousLocation);

                    double[] coordiates = new double[3];
                    if (settings.refSystem == ReferenceSystems.UTM.ordinal()) {
                        WGS84ToUTM wgsToUtm = new WGS84ToUTM();
                        coordiates = wgsToUtm.calcNorthEast(latitudeNew, longitudeNew);
                    }
                    else if (settings.refSystem == ReferenceSystems.WGS84.ordinal())
                    {
                        coordiates[0] = longitudeNew ;
                        coordiates[1] = latitudeNew;
                    }

                    if (distance > settings.trackingSensitivity) {
                        latitude = latitudeNew;
                        longitude = longitudeNew;
                        previousLocation = gps_location;

                        if (settings.refSystem == ReferenceSystems.UTM.ordinal())
                        {
                            Point point = new Point(coordiates[0], coordiates[1], SpatialReference.create(25832));
                            Viewpoint viewpoint = new Viewpoint(point, 200000);
                            map.setInitialViewpoint(viewpoint);
                            //mMapView.setViewpoint(viewpoint);}
                        }
                        else if (settings.refSystem == ReferenceSystems.WGS84.ordinal())
                        {
                            Point point = new Point(coordiates[0], coordiates[1], SpatialReference.create(4326));
                            Viewpoint viewpoint = new Viewpoint(point, 200000);
                            map.setInitialViewpoint(viewpoint);
                            //mMapView.setViewpoint(viewpoint);}
                        }

                        if (trackingStarted) {
                            polyLineCollection.add(coordiates[0], coordiates[1]);

                            ListenableList<Graphic> test = overlay.getGraphics();
                            if (test.size() >0)
                                overlay.getGraphics().remove(0);

                            if (MainActivity.actualShpType == ShapefileTypes.Point)
                            {
                                SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, selectedPointColor, 10);
                                Polyline tracking = new Polyline(polyLineCollection);
                                overlay.getGraphics().add(new Graphic(tracking, markerSymbol));
                            }
                            else if (MainActivity.actualShpType == ShapefileTypes.Line)
                            {
                                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, selectedLineColor, 5);
                                Polyline tracking = new Polyline(polyLineCollection);
                                overlay.getGraphics().add(new Graphic(tracking, lineSymbol));
                            }
                            else if (MainActivity.actualShpType == ShapefileTypes.Polygon)
                            {
                                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, selectedPolygonColor, 5);
                                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, selectedPolygonColor, lineSymbol);
                                Polygon polygon = new Polygon (polyLineCollection);
                                overlay.getGraphics().add(new Graphic(polygon, fillSymbol));
                            }
                        }
                    }

                    if (!mLocationDisplay.isStarted())
                        mLocationDisplay.startAsync();

                    if (settings.refSystem == ReferenceSystems.WGS84.ordinal()) {
                        textViewGpsX.setText(df.format(coordiates[0]));
                        textViewGpsY.setText(df.format(coordiates[1]));
                        textViewGpsZ.setText(df.format(gps_location.getAltitude()));
                    } else {
                        textViewGpsX.setText(dfUtm.format(coordiates[0]));
                        textViewGpsY.setText(dfUtm.format(coordiates[1]));
                        textViewGpsZ.setText("");
                    }
                }
            } catch (SecurityException ex) {

                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        1);
            }
        }

        @Override
        public void onLocationChanged(@NonNull List<Location> locations) { }

        @Override
        public void onProviderEnabled(@NonNull String provider) { }

        @Override
        public void onProviderDisabled(@NonNull String provider) { }
    }
}
