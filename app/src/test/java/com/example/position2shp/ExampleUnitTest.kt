package com.example.position2shp

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

        var file: File = File(wgs84Points)
        var outputFolder: File = file.parentFile!!

        wgs84Points = "C:\\Users\\Franzi\\Desktop\\Pos2Shp\\point_EPSG_3857\\point_EPSG_3857.csv";
        file = File(wgs84Points)
        outputFolder = file.parentFile!!
    }

    @Test
    fun geoJSONToShapefileTest() {

        // Ändern Sie die Projektion der Karte auf EPSG:4326 (WGS84)

    }
}