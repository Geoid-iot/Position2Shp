package com.example.position2shp.Util;

import java.util.ArrayList;

public final class PositionUtils {

    public static String DEMO_SHAPEFILE = "/sdcard/trees";

    /* this is used to load the native code at application startup. The library has already been
     * unpacked into /data/data/org.maptools.shapelib.android/lib/libshapelib-android-demo.so at
     * installation time by the package manager.
     */
    static {
        System.loadLibrary("shp");
    }

    private PositionUtils(){}

    public static void setShpFilePath(String path){
        //DEMO_SHAPEFILE = path.concat("/Pos2Shp/shapefiles");
        DEMO_SHAPEFILE = path;

    }

    public static String[] getStringAttributeTable(String path, int fieldID) {
        return getStringAttributes(path, fieldID);
    }

    public static double[] getDoubleAttributeTable(String path, int fieldID) {
        return getDoubleAttributes(path, fieldID);
    }

    public static int[] getIntAttributeTable(String path, int fieldID) {
        return getIntAttributes(path, fieldID);
    }

    public static String[] getShpFieldValues(String path) {
        return getFieldNames(path);
    }

    public static int[] getShpFieldTypes(String path)
    {
        return getFieldTypes(path);
    }

    public static int getShapeFileType(String path) {

        return getShapefileType(path);
    }

    public static int getShpRecordCount(String path) {
        return getRecordCount(path);
    }


    public static void createPointShapefile() {
        createPointShapefile(DEMO_SHAPEFILE, new DataAdapter<Position>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public Position get(int index) {
                return null;
            }
        });
    }

    public static void createLineShapefile() {
        createLineShapefile(DEMO_SHAPEFILE, new DataAdapter<Position>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public Position get(int index) {
                return null;
            }
        });
    }

    public static void createPolygonShapefile() {
        createPolygonShapefile(DEMO_SHAPEFILE, new DataAdapter<Position>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public Position get(int index) {
                return null;
            }
        });
    }

    public static void appendShapefile(ArrayList<Position> positions) {
        updateShapefile(DEMO_SHAPEFILE, new DataAdapter<Position>() {
            @Override
            public int size() {
                return positions.size();
            }

            @Override
            public Position get(int index) {
                return positions.get(index);
            }
        });
    }

    private static native int getShapefileType(String filePath);
    private static native String[] getFieldNames(String filePath);

    private static native int[] getFieldTypes(String filePath);
    private static native String[] getStringAttributes(String filePath, int fieldID);

    private static native double[] getDoubleAttributes(String filePath, int fieldID);

    private static native int[] getIntAttributes(String filePath, int fieldID);
    private static native int getRecordCount(String filePath);
    private static native boolean createPointShapefile(String filePath, DataAdapter<Position> adapter);
    private static native boolean updateShapefile(String filePath, DataAdapter<Position> adapter);
    private static native boolean createLineShapefile(String filePath, DataAdapter<Position> adapter);
    private static native boolean createPolygonShapefile(String filePath, DataAdapter<Position> adapter);

    private interface DataAdapter<E> {

        int size();

        E get(int index);
    }

    public static class Position {
        private final double mLatitude;
        private final double mLongitude;
        private final String mDate;

        public Position(double latitude, double longitude, String date) {
            mLatitude = latitude;
            mLongitude = longitude;
            mDate = date;
        }

        public double getLatitude() {
            return mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public String getDate() {
            return mDate;
        }
    }
}
