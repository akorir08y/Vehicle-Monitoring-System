package com.example.bus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.bus.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class CustomInfoWindowAdapterPlace implements GoogleMap.InfoWindowAdapter{
    private final View mWindow;
    private Context mContext;


    public CustomInfoWindowAdapterPlace(Context context){
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window_place,null);
    }


    private void rendowWindowText(Marker marker, View view){
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title_place);

        if(!title.equals("")){
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.description_place);
        if(!snippet.equals("")){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }
}
