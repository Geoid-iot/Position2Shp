package com.example.position2shp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.example.position2shp.Map.ShapefileTypes;
import com.example.position2shp.Map.TrackingColour;
import com.example.position2shp.Settings.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
	public static String mPositionTrackingShapefilePath;
	public static ShapefileTypes actualShpType = ShapefileTypes.Line;
	public static TrackingColour colorPolygon = TrackingColour.GREEN;
	private Settings settings;

	@RequiresApi(api = Build.VERSION_CODES.N)
	@SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get permission to read and write shapefiles
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{
							Manifest.permission.READ_EXTERNAL_STORAGE,
							Manifest.permission.WRITE_EXTERNAL_STORAGE,},
					1);
		}

		settings = new Settings();

		getExternalFilesDir();

		// load default background shapefile from assets
		initializeBackgroundShpFile();

		copyPrjFiles();

		setContentView(R.layout.activity_main);

		mPositionTrackingShapefilePath = "";

		NavHostFragment navController = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
		Bundle bundle = new Bundle();
		bundle.putParcelable("Settings", settings);

		navController.getNavController().navigate(R.id.action_mapFragment_to_mapFragment, bundle);
	}

	private void getExternalFilesDir() {
		String externalFilesDir = getExternalFilesDir(null).getAbsolutePath();
		settings.externalFilesDir = externalFilesDir;
	}

	private void copyAssets(String assetDir, String assetDirDestination) {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list(assetDir);
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null) {
			for(String filename : files) {
				InputStream in;
				OutputStream out;
				try {
					in = assetManager.open(assetDir + File.separator + filename);

					File outFileDir = new File(assetDirDestination);
					if(!outFileDir.exists()){
						outFileDir.mkdirs();
					}

					File outFile = new File(assetDirDestination + File.separator + filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					out.flush();
					out.close();
				} catch(IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		int size = in.available();
		byte[] buffer = new byte[size];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void copyPrjFiles() {
		String destinationDir = settings.externalFilesDir + "/prjFiles";
		copyAssets("prjFiles", destinationDir);
		settings.projFilesDir = destinationDir;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void initializeBackgroundShpFile() {
		//ToDo: Kreise_BW in neuen ordner "Bckground Shapefiles schieben
		String destinationDir = settings.externalFilesDir + "/Kreise_BW";
		copyAssets("Kreise_BW", destinationDir);

		File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Felipups");
		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("error", "failed to create directory");
			}
		}

		settings.backgroundShpFile = settings.externalFilesDir + "/Kreise_BW/kreisebw.shp";
	}
}
