package com.example.position2shp.Settings;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.position2shp.MainActivity;
import com.example.position2shp.Map.MapFragment;
import com.example.position2shp.Map.ReferenceSystems;
import com.example.position2shp.Map.ShapefileTypes;
import com.example.position2shp.R;
import com.example.position2shp.Settings.Spinner.ShapeFileTypeSpinner;
import com.example.position2shp.Util.PositionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TrackingSettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private TextView tvTrackingSensitivity;
    private TextView displayUnit;
    public EditText etTrackingSensitivity;
    public String existingShapefileName;
    public EditText etNewShpFileName;
    private final Settings settings;

    public TrackingSettingsFragment(Settings settings) {
        super(R.layout.trackingsettings);

        this.settings = settings;
    }

    public Settings getSettings(){
        return settings;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNewShpFileName = requireView().findViewById(R.id.eTNewFileName);

        displayUnit = requireView().findViewById(R.id.textView3);

        tvTrackingSensitivity = requireView().findViewById(R.id.tvTrackingSensitivity);
        etTrackingSensitivity = requireView().findViewById(R.id.etTrackingSensitivity);
        etTrackingSensitivity.setText(String.valueOf(settings.trackingSensitivity),
                TextView.BufferType.EDITABLE);

        // create dropdown menu for shapefile type (point, polyline, polygon)
        Spinner shapefileTypeSpinner = createShapefileTypeSpinner(view);

        // create dropdown menu for available reference systems (WGS84, UTM)
        createRefSystemSpinner(view);

        // create dropdown menu for available tracking mode (automatic or manual)
        createTrackingModeSpinner(view);

        RadioButton rbtnCreate =  requireView().findViewById(R.id.btnCreateNew);
        RadioButton rbtnEdit =  requireView().findViewById(R.id.btnUseExisting);

        TextView tvCreateNewShapefile = requireView().findViewById(R.id.tvCreateNewShapefile);

        rbtnCreate.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId)
            {
                settings.createNewShapefile = true;
                rbtnEdit.setChecked(false);
                tvCreateNewShapefile.setText(R.string.create_new_shapefile);
                ArrayAdapter<String> shpFilesTypesAdapter = createShapefileTypesArrayAdapter(view);
                shapefileTypeSpinner.setAdapter(shpFilesTypesAdapter);
                ShapeFileTypeSpinner shpFileTypeSpinner = new ShapeFileTypeSpinner();
                shapefileTypeSpinner.setOnItemSelectedListener(shpFileTypeSpinner);
                etNewShpFileName.setVisibility(View.VISIBLE);
            }
        });
        rbtnCreate.setChecked(true);

        rbtnEdit.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId)
            {
                settings.createNewShapefile = false;
                rbtnCreate.setChecked(false);
                tvCreateNewShapefile.setText(R.string.edit_an_existing_shapefile);
                ArrayAdapter<String> spinnerArrayAdapter = createExistingShapefilesArrayAdapter(view);
                shapefileTypeSpinner.setAdapter(spinnerArrayAdapter);
                shapefileTypeSpinner.setOnItemSelectedListener(this);
                etNewShpFileName.setVisibility(View.GONE);
            }
        });
        rbtnCreate.setChecked(true);
    }

    private void createTrackingModeSpinner(View view){
        String[] trackingMode = {"Automatic tracking", "Manual adding"};
        Spinner spinnerTrackingMode = requireView().findViewById(R.id.spinnerTrackingMode);
        ArrayAdapter<String> trackingModeAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, trackingMode);
        trackingModeAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinnerTrackingMode.setAdapter(trackingModeAdapter);
        spinnerTrackingMode.setSelection(0);
        spinnerTrackingMode.setOnItemSelectedListener(this);
    }

    private ArrayAdapter<String> createExistingShapefilesArrayAdapter(View view){
        //create list with existing shapefile names
        List<String> fileList = new ArrayList<>();

        String filepath = settings.externalFilesDir + getResources().getString(R.string.new_shp);
        MapSettingsFragment.collectShapefilesInList(fileList, filepath);

        String[] existingShapefiles = new String[fileList.size()];
        existingShapefiles = fileList.toArray(existingShapefiles);

        // add list with existing shape file names to spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, existingShapefiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        return spinnerArrayAdapter;
    }

    private Spinner createShapefileTypeSpinner(View view){
        Spinner shapefileTypeSpinner = requireView().findViewById(R.id.spinnerShapefileType);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> shpFilesTypesAdapter = createShapefileTypesArrayAdapter(view);
        shapefileTypeSpinner.setAdapter(shpFilesTypesAdapter);

        ShapeFileTypeSpinner shpFileTypeSpinner = new ShapeFileTypeSpinner();
        shapefileTypeSpinner.setOnItemSelectedListener(shpFileTypeSpinner);
        shapefileTypeSpinner.setSelection(MainActivity.actualShpType.ordinal());

        return shapefileTypeSpinner;
    }

    private ArrayAdapter<String> createShapefileTypesArrayAdapter(View view){
        String[] shpTypes = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            shpTypes = MapFragment.getNames(ShapefileTypes.class);
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> shpFilesTypesAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, shpTypes);
        shpFilesTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return shpFilesTypesAdapter;
    }

    private void createRefSystemSpinner(View view){
        //create dropdown with available reference systems
        String[] refSystem = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            refSystem = MapFragment.getNames(ReferenceSystems.class);
        }

        ArrayAdapter<String> refSystemAdapter = new ArrayAdapter<>
                (view.getContext(), android.R.layout.simple_spinner_item, refSystem);
        refSystemAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        Spinner referenceSystemSpinner = requireView().findViewById(R.id.spinnerReferenceSystems);
        referenceSystemSpinner.setAdapter(refSystemAdapter);

        referenceSystemSpinner.setOnItemSelectedListener(this);
        referenceSystemSpinner.setSelection(settings.refSystem);
    }

    public void createNewShapefile(String newShapefileName) {
        if (newShapefileName.isEmpty()) {
            Toast.makeText(requireContext(), "Shapefile name is empty!", Toast.LENGTH_LONG).show();
            return;
        }

        String destinationFilepath = settings.externalFilesDir + getResources().getString(R.string.new_shp) + newShapefileName;

        File outFileDir = new File(destinationFilepath);
        if(!outFileDir.exists()){
            outFileDir.mkdirs();
        }
        destinationFilepath = destinationFilepath + "/" + newShapefileName;
        String finalDestinationFilepath = destinationFilepath;
        new Thread(() -> {
            //PositionUtils.setShpFilePath(Environment.getExternalStorageDirectory().toString());
            PositionUtils.setShpFilePath(finalDestinationFilepath);

            if (MainActivity.actualShpType == ShapefileTypes.Line)
                PositionUtils.createLineShapefile();

            else if (MainActivity.actualShpType == ShapefileTypes.Point)
                PositionUtils.createPointShapefile();

            else if (MainActivity.actualShpType == ShapefileTypes.Polygon)
                PositionUtils.createPolygonShapefile();

            File destProjFile = new File(finalDestinationFilepath + ".prj");
            if (settings.refSystem == ReferenceSystems.UTM_32N.ordinal())
            {
                File srcProjFile = new File(settings.projFilesDir + "/25832.prj");
                try {
                    copy(srcProjFile, destProjFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                File srcProjFile = new File(settings.projFilesDir + "/4326.prj");
                try {
                    copy(srcProjFile, destProjFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }).start();

        settings.positionTrackingShapefilePath = destinationFilepath.concat(".shp");
    }

    public static void copy(File src, File dst) throws IOException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try (InputStream in = Files.newInputStream(src.toPath())) {
                try (OutputStream out = Files.newOutputStream(dst.toPath())) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        final int viewId = adapterView.getId();
        if (viewId == R.id.spinnerTrackingMode) {
            switch (position) {
                // Automatic tracking
                case 0:
                    tvTrackingSensitivity.setVisibility(View.VISIBLE);
                    etTrackingSensitivity.setVisibility(View.VISIBLE);
                    displayUnit.setVisibility(View.VISIBLE);
                    settings.automaticTracking = true;
                    break;
                // Manual adding
                case 1:
                    tvTrackingSensitivity.setVisibility(View.GONE);
                    etTrackingSensitivity.setVisibility(View.GONE);
                    displayUnit.setVisibility(View.GONE);

                    settings.automaticTracking = false;
                    /*CharSequence text = "Not implemented yet!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();*/
                    break;
                default:
                    break;
            }
        }
        else if (viewId == R.id.spinnerReferenceSystems)
        {
            ReferenceSystems refSystem = ReferenceSystems.values()[position];
            switch (refSystem){
                case WGS84:
                    settings.refSystem = ReferenceSystems.WGS84.ordinal();
                    break;
                case UTM_32N:
                    settings.refSystem = ReferenceSystems.UTM_32N.ordinal();
                    break;
            }
        }
        else if (viewId == R.id.spinnerShapefileType)
        {
            if (settings != null && !settings.createNewShapefile) {
                existingShapefileName = adapterView.getSelectedItem().toString();
                String externalFilesDir = settings.externalFilesDir + File.separator + existingShapefileName;
                String shpFile = externalFilesDir.replace(".shp", "");
                int shapeFileType = PositionUtils.getShapeFileType(shpFile);
                if (shapeFileType == 1) {
                    MainActivity.actualShpType = ShapefileTypes.Point;
                } else if (shapeFileType == 3) {
                    MainActivity.actualShpType = ShapefileTypes.Line;
                } else if (shapeFileType == 5) {
                    MainActivity.actualShpType = ShapefileTypes.Polygon;
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
