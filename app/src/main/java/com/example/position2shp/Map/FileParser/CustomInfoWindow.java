package com.example.position2shp.Map.FileParser;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.position2shp.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomInfoWindow extends InfoWindow {
    public CustomInfoWindow(MapView mapView, Overlay overlay) {
        super(R.layout.custominfowindow, mapView);
        Button btn = mView.findViewById(R.id.btn_ar);
        btn.setOnClickListener(view -> Toast.makeText(view.getContext(), "AR Button clicked", Toast.LENGTH_LONG).show());

        TextView title = mView.findViewById(R.id.bubble_title);

        TextView text = mView.findViewById(R.id.tv_bubble1);
        TextView text2 = mView.findViewById(R.id.tv_bubble2);

        if (overlay.getClass() == Polygon.class) {
            Polygon p = (Polygon) overlay;
            title.setText(p.getTitle());
            text.setText(p.getSnippet());
            text.setText(p.getSubDescription());
        }
        else if (overlay.getClass() == Polyline.class) {
            Polyline p = (Polyline) overlay;
            title.setText(p.getTitle());
            text.setText(p.getSnippet());
            text.setText(p.getSubDescription());
        }
        else if (overlay.getClass() == Marker.class) {
            Marker m = (Marker) overlay;
            title.setText(m.getTitle());
            text.setText(m.getSnippet());
            text.setText(m.getSubDescription());
        }
    }

    @Override public void onOpen(Object item){
       // mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClose() {

    }
}
