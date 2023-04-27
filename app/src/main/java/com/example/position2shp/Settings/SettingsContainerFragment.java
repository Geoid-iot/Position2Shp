package com.example.position2shp.Settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.position2shp.MainActivity;
import com.example.position2shp.R;
import com.example.position2shp.Util.PositionUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;

public class SettingsContainerFragment extends Fragment {

    private Settings settings;
    public SettingsContainerFragment() {
        super(R.layout.settings_tab_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        settings = new Settings();
        if (getArguments() != null) {
            settings = SettingsContainerFragmentArgs.fromBundle(getArguments()).getSettings();
        }

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.pager2);
        ViewPagerFragmentAdapter myAdapter = new ViewPagerFragmentAdapter(getActivity(), settings);

        Button btnSave = requireView().findViewById(R.id.btn_save_settings);
        btnSave.setOnClickListener(v -> {
            if (myAdapter.mapSettingsFragment != null) {
                settings.setMapSettings(myAdapter.mapSettingsFragment.settings);
            }

            if (myAdapter.trackingSettingsFragment != null) {
                if (myAdapter.trackingSettingsFragment.createNewShp)
                {
                    String newShapefileName = myAdapter.trackingSettingsFragment.etNewShpFileName.getText().toString();
                    newShapefileName = newShapefileName.replace(".shp", "");
                    myAdapter.trackingSettingsFragment.createNewShapefile(newShapefileName);
                }
                else {
                    MainActivity.mPositionTrackingShapefilePath = myAdapter.trackingSettingsFragment.settings.externalFilesDir + File.separator+ myAdapter.trackingSettingsFragment.existingShapefileName;
                    String shapeFile = MainActivity.mPositionTrackingShapefilePath.replace(".shp", "");
                    PositionUtils.setShpFilePath(shapeFile);
                }

                settings.trackingSensitivity = Float.parseFloat(myAdapter.trackingSettingsFragment.etTrackingSensitivity.getText().toString());
            }

            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            com.example.position2shp.Settings.SettingsContainerFragmentDirections.ActionSettingsContainerFragmentToMapFragment actionToEditFragment = SettingsContainerFragmentDirections.actionSettingsContainerFragmentToMapFragment();

            actionToEditFragment.setSettings(settings);
            navController.navigate(actionToEditFragment);

        });

        Button btnBack = requireView().findViewById(R.id.btn_back_to_map);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view);
            com.example.position2shp.Settings.SettingsContainerFragmentDirections.ActionSettingsContainerFragmentToMapFragment actionToEditFragment = SettingsContainerFragmentDirections.actionSettingsContainerFragmentToMapFragment();
            actionToEditFragment.setSettings(settings);
            navController.navigate(actionToEditFragment);
        });

        viewPager.setAdapter(myAdapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0)
                        tab.setText("Map Settings");
                    else if (position == 1)
                        tab.setText("Tracking Settings");
                }).attach();
    }


}
