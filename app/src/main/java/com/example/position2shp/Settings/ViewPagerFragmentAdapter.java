package com.example.position2shp.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerFragmentAdapter  extends FragmentStateAdapter {
    public MapSettingsFragment mapSettingsFragment;
    public TrackingSettingsFragment trackingSettingsFragment;

    Settings settings;

    public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity, Settings settings) {
        super(fragmentActivity);

        this.settings = settings;

        mapSettingsFragment = null;
        trackingSettingsFragment = null;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
            {
                if (mapSettingsFragment == null)
                    mapSettingsFragment = new MapSettingsFragment(settings);
                return mapSettingsFragment;
            }
            case 1:
            {
                if (trackingSettingsFragment == null)
                    trackingSettingsFragment = new TrackingSettingsFragment(settings);
                return trackingSettingsFragment;
            }

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}