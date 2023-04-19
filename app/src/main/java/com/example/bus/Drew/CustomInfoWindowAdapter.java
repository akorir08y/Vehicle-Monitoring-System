package com.example.bus.Drew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.bus.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View Window;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context){
        mContext = context;
        Window = LayoutInflater.from(context).inflate(R.layout.custom_info_window,null);
    }
    private void rendorWindowText(Marker marker, View view){
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);

        if(!title.equals("")){
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.description);
        if(!snippet.equals("")){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendorWindowText(marker,Window);
        return Window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendorWindowText(marker,Window);
        return Window;
    }
}
