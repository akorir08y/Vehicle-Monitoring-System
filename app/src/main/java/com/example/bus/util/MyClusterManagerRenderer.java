package com.example.bus.util;

import android.content.Context;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import com.example.bus.R;
import com.example.bus.models.ClusterMarker;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {


    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {

        final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_bis);
        markerOptions.icon(icon).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster) {
        return false;
    }
    /*
     * Update the GPS coordinate of a ClusterItem
     * @param clusterMarker
     */
    public void setUpdateMarker(ClusterMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }
}
