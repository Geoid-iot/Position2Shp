package com.example.position2shp.Map.FileParser;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;

import com.example.position2shp.R;
import com.example.position2shp.Settings.Settings;

import net.iryndin.jdbf.core.DbfRecord;
import net.iryndin.jdbf.reader.DbfReader;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CompoundCRS;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.datum.GeodeticDatum;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationException;
import org.cts.op.CoordinateOperationFactory;
import org.cts.op.transformation.FrenchGeocentricNTF2RGF;
import org.cts.op.transformation.GridBasedTransformation;
import org.cts.op.transformation.NTv2GridShiftTransformation;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.RegistryManager;
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.shape.DefaultShapeMetaSetter;
import org.osmdroid.shape.ShapeMetaSetter;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * https://github.com/osmdroid/osmdroid/issues/906
 * A simple utility class to convert a shape file into osmdroid overlays
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 * @since 6.1.0
 */

public class ShapefileReader {

    Context context;
    public ShapeType mShapeType;
    public List<Map.Entry<Integer,List<Integer>>> attributeToOverlayMapping;
    public List<DbfRecord> metadataList;
    ShapeMetaSetter shapeMetaSetter;
    public ShapefileReader(Context c)
    {
        context = c;
        metadataList = new ArrayList<>();
        attributeToOverlayMapping= new ArrayList<>();
        shapeMetaSetter = new DefaultShapeMetaSetter();
    }

    public static ValidationPreferences getDefaultValidationPreferences() {
        final ValidationPreferences pref = new ValidationPreferences();
        pref.setMaxNumberOfPointsPerShape(200000);
        return pref;
    }

    public Polygon createPolygon(MapView map, List<GeoPoint> geoPoints, Settings settings, DbfRecord metadata) throws ParseException {
        geoPoints.add(geoPoints.get(0));    //forces the loop to close(connect last point to first point)
        Polygon polygon = new Polygon(map);
        polygon.getFillPaint().setColor(settings.polygonFillColour); //set fill color

        polygon.setPoints(geoPoints);
        polygon.setTitle("A sample polygon");
        polygon.getOutlinePaint().setColor(settings.polygonLineColour);
        polygon.getOutlinePaint().setStrokeWidth(5);
        shapeMetaSetter.set(metadata, polygon);

      /*  CustomInfoWindow infoWindow = new CustomInfoWindow(map, polygon);
        polygon.setInfoWindow(infoWindow);*/

      /*  polygon.setOnClickListener((polygon1, mapView, eventPos) -> {
            polygon1.getOutlinePaint().setColor(Color.MAGENTA);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.show_polygon_in_ar)
                    .setPositiveButton(R.string.show_in_ar, (dialog, id) -> {
                        Toast.makeText(builder.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                        polygon1.getOutlinePaint().setColor(settings.polygonLineColour);
                        mapView.invalidate();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        polygon1.getOutlinePaint().setColor(settings.polygonLineColour);
                        mapView.invalidate();
                    });
            // Create the AlertDialog object and return it
           // builder.create().show();
            mapView.invalidate();
            return false;
        });*/
        return polygon;
    }

    public Polygon createPolygon(List<GeoPoint> geoPoints, Settings settings) {
        geoPoints.add(geoPoints.get(0));    //forces the loop to close(connect last point to first point)
        Polygon polygon = new Polygon();
        polygon.getFillPaint().setColor(settings.polygonFillColour); //set fill color

        polygon.setPoints(geoPoints);
        polygon.setTitle("A sample polygon");
        polygon.getOutlinePaint().setColor(settings.polygonLineColour);
        polygon.getOutlinePaint().setStrokeWidth(5);

        return polygon;
    }

    public Polyline createPolyLine(List<GeoPoint> geoPoints, Settings settings) {
        Polyline polyline = new Polyline();

        polyline.setPoints(geoPoints);
        polyline.setTitle("A sample polyline");
        polyline.getOutlinePaint().setColor(settings.lineColor);
        polyline.getOutlinePaint().setStrokeWidth(5);

       // CustomInfoWindow infoWindow = new CustomInfoWindow(map, polyline);
      //  polyline.setInfoWindow(infoWindow);

   /*     polyline.setOnClickListener((polyline1, mapView, eventPos) -> {
           // polyline1.getOutlinePaint().setColor(Color.MAGENTA);

            polyline1.showInfoWindow();
            /*polyline1.setInfoWindow(iw);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.show_polyline_in_ar)
                    .setPositiveButton(R.string.show_in_ar, (dialog, id) -> {
                        Toast.makeText(builder.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                        polyline1.getOutlinePaint().setColor(settings.lineColor);
                        mapView.invalidate();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        polyline1.getOutlinePaint().setColor(settings.lineColor);
                        mapView.invalidate();
                    });
            // Create the AlertDialog object and return it
           // builder.create().show();
            mapView.invalidate();

            return false;
        });*/
        return polyline;
    }

    public Polyline createPolyLine(MapView map, List<GeoPoint> geoPoints, Settings settings, DbfRecord metadata) throws ParseException {
        Polyline polyline = new Polyline(map);

        polyline.setPoints(geoPoints);
        polyline.setTitle("A sample polyline");
        polyline.getOutlinePaint().setColor(settings.lineColor);
        polyline.getOutlinePaint().setStrokeWidth(5);

        shapeMetaSetter.set(metadata, polyline);

      /*  polyline.setOnClickListener((polyline1, mapView, eventPos) -> {
            polyline1.getOutlinePaint().setColor(Color.MAGENTA);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.show_polyline_in_ar)
                    .setPositiveButton(R.string.show_in_ar, (dialog, id) -> {
                        Toast.makeText(builder.getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                        polyline1.getOutlinePaint().setColor(settings.lineColor);
                        mapView.invalidate();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        polyline1.getOutlinePaint().setColor(settings.lineColor);
                        mapView.invalidate();
                    });
            // Create the AlertDialog object and return it
          //  builder.create().show();
            mapView.invalidate();
            return false;

        });*/
        return polyline;
    }

    public List<Marker> createMarkers(MapView mapView, List<GeoPoint> geoPoints, Settings settings, DbfRecord metadata) throws ParseException {
        List<Marker> markers = new ArrayList<>();

        for (GeoPoint gp : geoPoints) {
            Marker m = new Marker(mapView);
            m.setPosition(gp);
            m.setIcon(context.getResources().getDrawable(R.drawable.baseline_circle_24));
            m.getIcon().setColorFilter(settings.pointColor, PorterDuff.Mode.MULTIPLY);
            shapeMetaSetter.set(metadata, m);
/*
            CustomInfoWindow infoWindow = new CustomInfoWindow(mapView, m);
            m.setInfoWindow(infoWindow);*/
            markers.add(m);
        }
        return markers;
    }

    public List<Marker> createMarkers(MapView mapView, List<GeoPoint> geoPoints, Settings settings) {
        List<Marker> markers = new ArrayList<>();

        for (GeoPoint gp : geoPoints) {
            Marker m = new Marker(mapView);
            m.setPosition(gp);
            m.setIcon(context.getResources().getDrawable(R.drawable.baseline_circle_24));
            m.getIcon().setColorFilter(settings.pointColor, PorterDuff.Mode.MULTIPLY);
/*
            CustomInfoWindow infoWindow = new CustomInfoWindow(mapView, m);
            m.setInfoWindow(infoWindow);*/
            markers.add(m);
        }
        return markers;
    }

    public SimpleFastPointOverlay createPoints(List<GeoPoint> geoPoints, Settings settings, Activity activity) {
        List<IGeoPoint> pointsList = new ArrayList<>();
        for (int i = 0; i < geoPoints.size(); i++) {
            pointsList.add(new LabelledGeoPoint(geoPoints.get(i)));
        }

        // wrap them in a theme
        SimplePointTheme pt = new SimplePointTheme(pointsList, true);

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle().setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION).setRadius(15).setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE);

        opt.getPointStyle().setColor(settings.pointColor);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);
        sfpo.setOnClickListener((points, point) -> {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.show_point_in_ar)
                    .setPositiveButton(R.string.show_point_in_ar, (dialog, id) -> {
                        // START THE GAME!
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        // User cancelled the dialog
                    });
            // Create the AlertDialog object and return it
           // builder.create().show();
        });
        return sfpo;
    }

    /**
     * @param file  the shape file to be converted.
     * @param prefs allows the client to relax the level of validation when reading a shape file.
     * @return an arraylist of all overlays from the shapefile.
     * @throws Exception
     */
    public List<PointData[]> convert(File file, ValidationPreferences prefs) throws Exception {
        FileInputStream is = null;
        FileInputStream dbfInputStream = null;
        DbfReader dbfReader = null;
        ShapeFileReader r;

        List<PointData[]> ptsList = new ArrayList<>();
        metadataList.clear();
        try {
            File dbase = new File(file.getParentFile(), file.getName().replace(".shp", ".dbf"));
            if (dbase.exists()) {
                dbfInputStream = new FileInputStream(dbase);
                dbfReader = new DbfReader(dbfInputStream);
            }
            is = new FileInputStream(file);
            r = new ShapeFileReader(is, prefs);

            AbstractShape s;
            int recordCounter = 0;
            int geometryCount = 0;
            while ((s = r.next()) != null) {
                DbfRecord metadata = null;
                if (dbfReader != null)
                    metadata = dbfReader.read();


                mShapeType = s.getShapeType();
                switch (s.getShapeType()) {
                    case POINT: {
                        metadataList.add(metadata);
                        PointShape aPoint = (PointShape) s;
                        PointData[] points = {new PointData(aPoint.getX(), aPoint.getY())};
                        ptsList.add(points);

                        attributeToOverlayMapping.add(new AbstractMap.SimpleEntry<>(recordCounter, Collections.singletonList(geometryCount)));
                        geometryCount++;
                        recordCounter++;
                    }
                    break;

                    case POLYGON: {
                        PolygonShape aPolygon = (PolygonShape) s;

                        List<Integer> parts = new ArrayList<>();
                        for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
                            metadataList.add(metadata);
                            PointData[] points = aPolygon.getPointsOfPart(i);

                            ptsList.add(points);
                            parts.add(geometryCount);
                            geometryCount++;
                        }
                        attributeToOverlayMapping.add(new AbstractMap.SimpleEntry<>(recordCounter, parts));
                        recordCounter++;
                    }
                    break;

                    case POLYLINE: {
                        PolylineShape polylineShape = (PolylineShape) s;

                        List<Integer> parts = new ArrayList<>();
                        for (int i = 0; i < polylineShape.getNumberOfParts(); i++) {
                            metadataList.add(metadata);
                            PointData[] points = polylineShape.getPointsOfPart(i);
                            ptsList.add(points);
                            parts.add(geometryCount);
                            geometryCount++;
                        }
                        attributeToOverlayMapping.add(new AbstractMap.SimpleEntry<>(recordCounter, parts));
                        recordCounter++;
                    }
                    break;

                    default:
                        Log.w(IMapView.LOGTAG, s.getShapeType() + " was unhandled! " + s.getClass().getCanonicalName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
            try {
                dbfReader.close();
            } catch (Exception ex) {
            }
            try {
                dbfInputStream.close();
            } catch (Exception ex) {
            }
        }
        return ptsList;
    }

    public List<PointData[]> convert(File file) throws Exception {
        return convert(file, getDefaultValidationPreferences());
    }

    public List<Overlay> transformToWGS84(MapView map, ShapeType type, List<PointData[]> points, String csNameSrc, Settings settings) throws CRSException, CoordinateOperationException, IllegalCoordinateException, ParseException {
        final String csNameDest = "EPSG:4326";

        Set<CoordinateOperation> ops = createCoordinateOperations(csNameSrc, csNameDest);

        List<Overlay> folder = new ArrayList<>();
        List<GeoPoint> geoPoints = new ArrayList<>();
        if (!ops.isEmpty()) {
            Iterator<DbfRecord> it = metadataList.iterator();
            for (PointData[] p : points) {
                geoPoints.clear();

                for (PointData pData : p) {
                    double[] pointSource = new double[]{pData.getX(), pData.getY()};
                    CoordinateOperation op = CoordinateOperationFactory.getMostPrecise(ops);

                    double[] result = op.transform(pointSource);
                    geoPoints.add(new GeoPoint(result[1], result[0]));
                }

                if (it.hasNext()) {
                    if (type == ShapeType.POLYGON) {
                        folder.add(createPolygon(map, geoPoints, settings, it.next()));
                    } else if (type == ShapeType.POLYLINE) {
                        folder.add(createPolyLine(map, geoPoints, settings, it.next()));
                    } else if (type == ShapeType.MULTIPOINT || type == ShapeType.POINT) {
                        // add overlay
                        folder.addAll(createMarkers(map, geoPoints, settings, it.next()));
                    }
                }
            }
        }

        return folder;
    }

    public Set<CoordinateOperation> createCoordinateOperations(String csNameSrc, String csNameDest) throws CRSException, CoordinateOperationException {
        CRSFactory cRSFactory = new CRSFactory();
        RegistryManager registryManager = cRSFactory.getRegistryManager();

        registryManager.addRegistry(new EPSGRegistry());
        GeodeticCRS sourceCRS = (GeodeticCRS) cRSFactory.getCRS(csNameSrc);
        GeodeticCRS targetCRS = (GeodeticCRS) cRSFactory.getCRS(csNameDest);

        Set<CoordinateOperation> ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);

        if (sourceCRS.getDatum() == GeodeticDatum.WGS84 || targetCRS.getDatum() == GeodeticDatum.WGS84) {
            ops = CoordinateOperationFactory.excludeFilter(ops, FrenchGeocentricNTF2RGF.class);
            ops = CoordinateOperationFactory.excludeFilter(ops, NTv2GridShiftTransformation.class);
        }
        // If source CRS comes from the EPSG registry and is not a CompoundCRS,
        // we use BursaWolf or translation rather than GridBasedTransformation,
        // even if a GridBasef Transformation is available (precise transformation
        // may be available because we also read IGNF registry and precise
        // transformations have been stored in GeodeticDatum objects.
        else if (sourceCRS.getIdentifier().getAuthorityName().equals("EPSG") && !(sourceCRS instanceof CompoundCRS) && !(targetCRS instanceof CompoundCRS)) {
            ops = CoordinateOperationFactory.excludeFilter(ops, GridBasedTransformation.class);
        }

        return ops;
    }

    public List<GeoPoint> transformToUTM32(List<GeoPoint> points, String csNameSrc) throws CRSException, CoordinateOperationException, IllegalCoordinateException {
        final String csNameDest = "EPSG:25832";

        Set<CoordinateOperation> ops = createCoordinateOperations(csNameSrc, csNameDest);
        List<GeoPoint> geoPointsTransformed = new ArrayList<>();
        if (!ops.isEmpty()) {
            for (GeoPoint p : points) {
                double[] pointSource = new double[]{p.getLongitude(), p.getLatitude()};
                CoordinateOperation op = CoordinateOperationFactory.getMostPrecise(ops);

                double[] result = op.transform(pointSource);
                geoPointsTransformed.add(new GeoPoint(result[0], result[1]));
            }
        }

        return geoPointsTransformed;
    }

    public List<Overlay> getOverlay(MapView map, List<PointData[]> points, ShapeType type, Settings settings) throws ParseException {

        List<Overlay> folder = new ArrayList<>();
        List<GeoPoint> geoPoints = new ArrayList<>();

        Iterator<DbfRecord> it = metadataList.iterator();
        for (PointData[] p : points) {
            geoPoints.clear();
            for (PointData pData : p) {
                geoPoints.add(new GeoPoint(pData.getY(), pData.getX()));
            }

            if (it.hasNext()) {
                if (type == ShapeType.POLYGON) {
                    folder.add(createPolygon(map, geoPoints, settings, it.next()));
                } else if (type == ShapeType.POLYLINE) {
                    folder.add(createPolyLine(map, geoPoints, settings, it.next()));
                } else if (type == ShapeType.MULTIPOINT || type == ShapeType.POINT) {
                    folder.addAll(createMarkers(map, geoPoints, settings, it.next()));
                }
            }
        }

        return folder;
    }

    public CoordinateReferenceSystem getCRS(File file) throws CRSException, IOException {
        File prjFile = new File(file.getParentFile(), file.getName().replace(".shp", ".prj"));
        CRSFactory crsFactory = new CRSFactory();
        return crsFactory.createFromPrj(prjFile);
    }
}

