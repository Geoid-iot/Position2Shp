package com.example.position2shp.Map;

import android.util.Log;

import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.data.ShapefileInfo;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import java.io.File;
import java.util.List;

/**
 * Created by Franziska on 29.05.2018.
 */

public class Shapefile {

    FeatureLayer featureLayer;
    ShapefileFeatureTable mTable;
    ArcGISMap mMap;
    SimpleRenderer mRenderer;

    Shapefile(ArcGISMap map, String mShapefilePath, SimpleRenderer renderer) {
        mMap = map;
        mTable = new ShapefileFeatureTable(mShapefilePath);
        mRenderer = renderer;
    }

    public void load() {
        mTable.loadAsync();
        Log.d("Loading Listener", "Loading startet");
        Log.d("Loading Listener", mTable.getLoadStatus().toString());

        mTable.addDoneLoadingListener(() -> {
            if (mTable.getLoadStatus() == LoadStatus.LOADED) {
                ShapefileInfo info = mTable.getInfo();
                featureLayer = new FeatureLayer(mTable);

                featureLayer.setRenderer(mRenderer);

                mMap.getOperationalLayers().add(featureLayer);
                Log.d("Loading Listener", "Loading done");
            }
        });
    }

    public ShapefileFeatureTable getFeatureTable() {
        return this.mTable;
    }

    ;

    // find all shapefiles in user folder
    static public void collectShapefilesInList(List<String> fileList, String filepath) {
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
                                String test = inFile2.getParent();
                                String filename = test.substring(test.indexOf(searchString) + searchString.length());

                                fileList.add(filename + File.separator + inFile2.getName());
                            }
                        }
                    }

                }
                else if (inFile.isFile() && inFile.getAbsolutePath().contains(".shp")) {
                    String searchString = "/files/";
                    String test = inFile.getParent();
                    String filename = test.substring(test.indexOf(searchString) + searchString.length());

                    fileList.add(filename + File.separator + inFile.getName());
                }
            }
        }
    }
}
