package com.example.position2shp.Map;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.position2shp.MainActivity;
import com.example.position2shp.Map.FileParser.ShapefileReader;
import com.example.position2shp.R;
import com.example.position2shp.Settings.Settings;
import com.example.position2shp.Util.PositionUtils;

import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.op.CoordinateOperationException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class MapFragment extends Fragment {
    final DecimalFormat df = new DecimalFormat("#.#####");
    final DecimalFormat dfUtm = new DecimalFormat("#.##");
    double latitude = 47.5;
    double longitude = 9.01;
    private List<GeoPoint> geoPoints;
    private MyLocationNewOverlay mLocationOverlay;
    private TextView textViewGpsX, textViewGpsY, textViewGpsZ;
    private boolean trackingStarted;
    private MapView mMapView;
    private IMapController mapController;
    private Button btnStart;
    private Location previousLocation;
    private Settings settings;
    private boolean deleteLastLayer = false;

    private final SimpleDateFormat formatter;
    List<Date> trackingTime;

    public MapFragment() {
        super(R.layout.mapview);

        previousLocation = new Location("Init Position");
        previousLocation.setLongitude(9.0);
        previousLocation.setLatitude(49.0);

        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        trackingTime = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(Objects.requireNonNull(e.getEnumConstants())).map(Enum::name).toArray(String[]::new);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ToDo Warum wird onViewCreateed zwei mal aufgerufen
        settings = new Settings();
        if (getArguments() != null) {
            settings = MapFragmentArgs.fromBundle(getArguments()).getSettings();
        }

        geoPoints = new ArrayList<>();

        btnStart = requireView().findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            try {
                positionTracking(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        if (settings != null)
            btnStart.setActivated(!settings.positionTrackingShapefilePath.isEmpty());

        Button btnClear = requireView().findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            List<Overlay> graphicOverlays = mMapView.getOverlays();

            for (int i = graphicOverlays.size() - 1; i > 0; i--) {
                mMapView.getOverlays().remove(graphicOverlays.get(i));
            }
        });

        Button btnEdit = requireView().findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            MapFragmentDirections.ActionMapFragmentToMapEditFragment actionToEditFragment = MapFragmentDirections.actionMapFragmentToMapEditFragment();
            actionToEditFragment.setSettings(settings);
            navController.navigate(actionToEditFragment);
        });

        Button btnSettings = requireView().findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            MapFragmentDirections.ActionMapFragmentToSettingsContainerFragment actionToSettingsFragment = MapFragmentDirections.actionMapFragmentToSettingsContainerFragment();
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

        try {
            initializeBackgroundShpFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

      //  myLocationListener.start();

        deleteLastLayer = false;
    }

    private void loadShapeFile(File f) throws Exception {
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setUseDataConnection(true);

        //ShapeConverter.convert();
        ShapefileReader shpReader = new ShapefileReader(requireContext());
        List<PointData[]> points = shpReader.convert(f);

        CoordinateReferenceSystem crs = shpReader.getCRS(f);
        String crsName = crs.getName();

        List<Overlay> folder = new ArrayList<>();
        ShapeType mShapeType = shpReader.mShapeType;
        if (crsName.contains("ETRS89") && crsName.contains("UTM") && crsName.contains("32N")) {
            String csNameSrc = "EPSG:25832";
            folder = shpReader.transformToWGS84(mMapView, mShapeType, points, csNameSrc, settings);
        } else if (crsName.contains("GCS_WGS_1984")) {
            folder = shpReader.getOverlay(mMapView, points, mShapeType, settings);
        }

        mMapView.getOverlayManager().addAll(folder);
        mMapView.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeBackgroundShpFile() throws Exception {
        if (settings == null)
            return;

        File f = new File(settings.backgroundShpFile);
        if (f.exists() && f.canRead()) {
            loadShapeFile(f);
        }

        if (!settings.createNewShapefile) {
            f = new File(settings.positionTrackingShapefilePath);
            if (f.exists() && f.canRead()) {
                loadShapeFile(f);
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initializeMap() {
        mMapView = requireView().findViewById(R.id.mapView);

        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mMapView.setMultiTouchControls(true);

        mapController = mMapView.getController();
        mapController.setZoom(16.0);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mMapView){
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onLocationChanged(Location gps_location, IMyLocationProvider source) {
                super.onLocationChanged(gps_location, source);
                if (settings == null)
                    return;

                try {
                    if (gps_location != null) {
                        final double latitudeNew = gps_location.getLatitude();
                        final double longitudeNew = gps_location.getLongitude();
                        float distance = 0.0f;
                        if (previousLocation != null)
                            distance = gps_location.distanceTo(previousLocation);

                        double[] coordinates = new double[3];
                        if (settings.refSystem == ReferenceSystems.UTM_32N.ordinal()) {
                            WGS84ToUTM wgsToUtm = new WGS84ToUTM();
                            coordinates = wgsToUtm.calcNorthEast(latitudeNew, longitudeNew);
                        } else if (settings.refSystem == ReferenceSystems.WGS84.ordinal()) {
                            coordinates[0] = latitudeNew;
                            coordinates[1] = longitudeNew;
                        }

                        if (distance > settings.trackingSensitivity) {
                            latitude = latitudeNew;
                            longitude = longitudeNew;
                            GeoPoint point = new GeoPoint(latitude, longitude);
                            if (previousLocation.getProvider().equals("Init Position"))
                                mapController.setCenter(point);

                            if (trackingStarted && settings.automaticTracking) {
                                updateTracking(point);
                            }
                            previousLocation = gps_location;
                        }

                        if (settings.refSystem == ReferenceSystems.WGS84.ordinal()) {
                            textViewGpsX.setText(df.format(coordinates[0]));
                            textViewGpsY.setText(df.format(coordinates[1]));
                            textViewGpsZ.setText(df.format(gps_location.getAltitude()));
                        } else {
                            textViewGpsX.setText(dfUtm.format(coordinates[0]));
                            textViewGpsY.setText(dfUtm.format(coordinates[1]));
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
        };
        mLocationOverlay.enableMyLocation();
        if (settings != null && settings.automaticTracking && trackingStarted) {
            mLocationOverlay.enableFollowLocation();
            mapController.setCenter(new GeoPoint(latitude, longitude));
        }

        mMapView.getOverlays().add(mLocationOverlay);


        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                //Toast.makeText(requireContext(), p.getLatitude() + " - " + p.getLongitude(), Toast.LENGTH_LONG).show();

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                //Toast.makeText(requireContext(), p.getLatitude() + " - " + p.getLongitude(), Toast.LENGTH_SHORT).show();
                if (trackingStarted && !settings.automaticTracking) {
                    updateTracking(p);
                }
                return false;
            }
        };


        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        mMapView.getOverlays().add(OverlayEvents);
    }

    private void initializePositionInfo() {
        // UI elements for displaying GPS coordinates in different coordinate systems
        if (settings != null) {
            TextView label = requireView().findViewById(R.id.tvReferenceSystem);
            if (settings.refSystem == ReferenceSystems.WGS84.ordinal())
                label.setText(R.string.wgs84);
            else if (settings.refSystem == ReferenceSystems.UTM_32N.ordinal())
                label.setText(R.string.utm);
        }
    }

    public void positionTracking(View view) throws IllegalCoordinateException, CRSException, CoordinateOperationException {
        if (view.getId() == R.id.btnStart) {
            if (!view.isActivated()) {
                CharSequence text = "Please select or create a shapefile in the settings first!";
                Toast.makeText(getContext(), text, LENGTH_SHORT).show();
                return;
            }
            if (!trackingStarted) {
                //ToDo: was passiert mit dem Tracking wenn man das Fragment verlässt? static !?!?

                trackingStarted = true;
                btnStart.setBackground(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.stop_black, null));
            } else {
                trackingStarted = false;
                deleteLastLayer = false;
                ArrayList<PositionUtils.Position> positions;

                if ((geoPoints.size() >= 2 && MainActivity.actualShpType == ShapefileTypes.Line) ||
                        MainActivity.actualShpType == ShapefileTypes.Point) {
                    int i = 0;
                    positions = new ArrayList<>();

                    //Date date = new Date();
                    Iterator<Date> it = trackingTime.iterator();
                    if (settings.refSystem == ReferenceSystems.UTM_32N.ordinal()) {
                        ShapefileReader sr = new ShapefileReader(requireContext());
                        String csNameSrc = "EPSG:4326";
                        List<GeoPoint> geoPointsTransformed = sr.transformToUTM32(geoPoints, csNameSrc);

                        for (GeoPoint p : geoPointsTransformed) {
                            positions.add(i, new PositionUtils.Position(p.getLongitude(), p.getLatitude(), formatter.format(it.next())));
                            i++;
                        }
                    } else if (settings.refSystem == ReferenceSystems.WGS84.ordinal()) {
                        for (GeoPoint p : geoPoints) {
                            positions.add(i, new PositionUtils.Position(p.getLatitude(), p.getLongitude(), formatter.format(it.next())));
                            i++;
                        }
                    }
                    PositionUtils.appendShapefile(positions);
                } else if ((geoPoints.size() >= 4 && MainActivity.actualShpType == ShapefileTypes.Polygon)) {
                    int i = 0;
                    positions = new ArrayList<>();
                    Iterator<Date> it = trackingTime.iterator();
                    if (settings.refSystem == ReferenceSystems.UTM_32N.ordinal()) {
                        ShapefileReader shpReader = new ShapefileReader(requireContext());
                        String csNameSrc = "EPSG:4326";
                        List<GeoPoint> geoPointsTransformed = shpReader.transformToUTM32(geoPoints, csNameSrc);
                        for (GeoPoint p : geoPointsTransformed) {
                            positions.add(i, new PositionUtils.Position(p.getLongitude(), p.getLatitude(), formatter.format(it.next())));
                            i++;
                        }
                        GeoPoint p0 = geoPointsTransformed.get(0);
                        positions.add(i, new PositionUtils.Position(p0.getLongitude(), p0.getLatitude(), formatter.format(trackingTime.get(0))));
                    } else if (settings.refSystem == ReferenceSystems.WGS84.ordinal()) {
                        for (GeoPoint p : geoPoints) {
                            positions.add(i, new PositionUtils.Position(p.getLatitude(), p.getLongitude(), formatter.format(it.next())));
                            i++;
                        }
                        GeoPoint p0 = geoPoints.get(0);
                        positions.add(i, new PositionUtils.Position(p0.getLatitude(), p0.getLongitude(), formatter.format(trackingTime.get(0))));
                    }

                    PositionUtils.appendShapefile(positions);
                } else if ((geoPoints.size() < 4 && MainActivity.actualShpType == ShapefileTypes.Polygon)) {
                    CharSequence text = "Not enough points for polygon feature!";
                    Toast.makeText(getContext(), text, LENGTH_SHORT).show();
                } else if (geoPoints.size() < 2 && MainActivity.actualShpType == ShapefileTypes.Line) {
                    CharSequence text = "Not enough points for line feature!";
                    Toast.makeText(getContext(), text, LENGTH_SHORT).show();
                } else if (geoPoints.isEmpty()) {
                    CharSequence text = "No points tracked yet!";
                    Toast.makeText(getContext(), text, LENGTH_SHORT).show();
                }

                btnStart.setBackground(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.start_selector, null));
            }
        }
    }

    public void centerMapView() {
        mapController = mMapView.getController();
        mapController.setZoom(18.0);

        mapController.setCenter(mLocationOverlay.getMyLocation());
        //ToDo: pause resume destroy mapController mapview mLocationOverlay
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
        //  mMapView.on();
    }

    public void updateTracking(GeoPoint point) {
        geoPoints.add(point);
        trackingTime.add(new Date());

        List<Overlay> overlays = mMapView.getOverlays();
        if (overlays.size() > 1 && deleteLastLayer) {
            mMapView.getOverlays().remove(overlays.size() - 1);
        }

        deleteLastLayer = true;
        ShapefileReader sr = new ShapefileReader(requireContext());
        if (MainActivity.actualShpType == ShapefileTypes.Point) {
            // add overlay
            mMapView.getOverlays().addAll(sr.createMarkers(mMapView, geoPoints, settings));

        } else if (MainActivity.actualShpType == ShapefileTypes.Line) {
            List<Overlay> folder = new ArrayList<>();
            folder.add(sr.createPolyLine(geoPoints, settings));
            mMapView.getOverlayManager().addAll(folder);

        } else if (MainActivity.actualShpType == ShapefileTypes.Polygon) {
            List<Overlay> folder = new ArrayList<>();
            folder.add(sr.createPolygon(geoPoints, settings));
            mMapView.getOverlayManager().addAll(folder);

            geoPoints.remove(geoPoints.size() - 1);
        }
        mMapView.invalidate();
    }
}
