package com.example.position2shp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.example.position2shp.Map.ShapefileTypes;
import com.example.position2shp.Settings.Settings;

import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
	public static ShapefileTypes actualShpType = ShapefileTypes.Line;
	private Settings settings;

	@RequiresApi(api = Build.VERSION_CODES.N)
	@SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get permission to read and write shapefiles
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				ActivityCompat.requestPermissions(this, new String[]{
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.ACCESS_FINE_LOCATION,
								Manifest.permission.ACCESS_COARSE_LOCATION,
								Manifest.permission.ACCESS_BACKGROUND_LOCATION},
						1);
			}
		}

		settings = new Settings();

		getExternalFilesDir();

		// load default background shapefile from assets
		initializeBackgroundShpFile();

		copyPrjFiles();

		setContentView(R.layout.activity_main);



		//load/initialize the osmdroid configuration, this can be done
		Context ctx = getApplicationContext();
		Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

		NavHostFragment navController = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
		Bundle bundle = new Bundle();
		bundle.putParcelable("Settings", settings);

		Objects.requireNonNull(navController).getNavController().navigate(R.id.action_mapFragment_to_mapFragment, bundle);
	}

	private void getExternalFilesDir() {
		settings.externalFilesDir = getExternalFilesDir(null).getAbsolutePath();
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
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
					out = Files.newOutputStream(outFile.toPath());
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			copyAssets("prjFiles", destinationDir);
		}
		settings.projFilesDir = destinationDir;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void initializeBackgroundShpFile() {
		String destinationDir = settings.externalFilesDir + getString(R.string.background_shp) + "/Kreise_BW";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			copyAssets("Kreise_BW", destinationDir);
		}

		destinationDir =  settings.externalFilesDir + getString(R.string.background_shp) + "/AX_KommunalesGebiet";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			copyAssets("AX_KommunalesGebiet", destinationDir);
		}
	}
}
