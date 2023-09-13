package com.example.position2shp

import com.example.position2shp.Map.FileParser.CreateNewShapefile
import com.example.position2shp.Map.FileParser.ShapefileToGeoJSON
import com.example.position2shp.Map.Proj4TileSystem
import org.junit.Test
import java.io.File
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun createShapefileTest() {
        var wgs84Points =
            "C:\\Users\\Franzi\\Desktop\\Pos2Shp\\point_EPSG_4326\\point_EPSG_4326.csv";
        CreateNewShapefile.test(wgs84Points, "4326");

        var file: File = File(wgs84Points)
        var outputFolder: File = file.parentFile!!
        if (outputFolder.exists()) {
            for (f in Objects.requireNonNull(
                outputFolder.listFiles()
            )) if (f.absolutePath.contains(".shp")) ShapefileToGeoJSON.test(f.absolutePath);
        }

        wgs84Points = "C:\\Users\\Franzi\\Desktop\\Pos2Shp\\point_EPSG_3857\\point_EPSG_3857.csv";
        CreateNewShapefile.test(wgs84Points, "3857");
        file = File(wgs84Points)
        outputFolder = file.parentFile!!
        if (outputFolder.exists()) {
            for (f in Objects.requireNonNull(
                outputFolder.listFiles()
            )) if (f.absolutePath.contains(".shp")) ShapefileToGeoJSON.test(f.absolutePath);
        }
    }

    @Test
    fun geoJSONToShapefileTest() {

        // Ändern Sie die Projektion der Karte auf EPSG:4326 (WGS84)
        val test = Proj4TileSystem()
        assert(test.equals(null))
    }
}