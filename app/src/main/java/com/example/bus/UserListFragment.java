package com.example.bus;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bus.R;
import com.example.bus.Drew.PlacesInfo;
import com.example.bus.main.PlaceAutocompleteAdapter;
import com.example.bus.adapters.CustomInfoWindowAdapterPlace;
import com.example.bus.adapters.UserRecyclerAdapter;
import com.example.bus.models.ClusterMarker;
import com.example.bus.models.PolylineData;
import com.example.bus.models.User;
import com.example.bus.models.UserLocation;
import com.example.bus.util.MyClusterManagerRenderer;
import com.example.bus.util.ViewWeightAnimationWrapper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.bus.main.Constants.MAPVIEW_BUNDLE_KEY;

public class UserListFragment extends Fragment implements OnMapReadyCallback,OnPolylineClickListener,
        GoogleMap.OnInfoWindowClickListener,
        View.OnClickListener,UserRecyclerAdapter.UserListRecyclerClickListener{




    private static final String TAG = "UserListFragment";
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    //widgets
    private RecyclerView mUserListRecyclerView;
    private MapView mMapView;
    private CustomInfoWindowAdapterPlace customInfoWindowAdapterPlace;
    private AutoCompleteTextView editText;
    //vars
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mlocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    //objects
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GeoDataClient mGeoDataClient;
    private Marker mMarker;
    private PlacesInfo mPlace;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng( -4.47166,33.97559 ),new LatLng(3.93726,41.85688));
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //vars
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocation = new ArrayList<>();
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private ArrayList<Marker> mtripMarkers = new ArrayList<>();
    private UserRecyclerAdapter mUserRecyclerAdapter;
    private RelativeLayout mMapContainer;
    private GoogleMap googleMap;
    private LatLngBounds mMapBoundary;
    private ContextCompat contextCompat;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterManager mClusterManager;
    private UserLocation mUserPosition;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;
    private GeoApiContext geoApiContext = null;
    public Marker mSelectedMarker = null;
    int PLACE_PICKER_REQUEST = 1;
    private FirebaseAuth mAuth;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserList = getArguments().getParcelableArrayList(getString(R.string.intent_user_list));
            mUserLocation = getArguments().getParcelableArrayList(getString(R.string.intent_user_locations));
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        mMapView = view.findViewById(R.id.user_list_map);
        mGeoDataClient = Places.getGeoDataClient(getActivity());
        mAuth = FirebaseAuth.getInstance();

        view.findViewById(R.id.gps_pin).setOnClickListener(this);
        view.findViewById(R.id.info_pin).setOnClickListener(this);
        view.findViewById(R.id.places_pic).setOnClickListener(this);
        view.findViewById(R.id.refresh).setOnClickListener(this);
        editText = view.findViewById(R.id.txt_edit);
        mMapContainer = view.findViewById(R.id.map_container);
        view.findViewById(R.id.btn_full_screen_map).setOnClickListener(this);

        getLocationPermission();
        init();
        initUserListRecyclerView();
        initGoogleMap(savedInstanceState);
        setUserLocation();
        return view;
    }



    private void initUserListRecyclerView() {
        mUserRecyclerAdapter = new UserRecyclerAdapter(mUserList,this);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }
    }

    private void setUserLocation(){
        for(UserLocation userLocation:mUserLocation){
            if(userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid()));{
                mUserPosition = userLocation;
            }
        }
    }

    private void setCameraView(){

        double bottomBoundary = mUserPosition.getGeo_point().getLatitude()- .4;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude()- .4;
        double topBoundary = mUserPosition.getGeo_point().getLatitude()+ .4;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude()+ .4;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary,leftBoundary),new LatLng(topBoundary,rightBoundary));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,0));
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolylinesData.size()>0){
                    for(PolylineData polylineData:mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }

                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));


                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration<duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }

    private void removeTripMarkers(){
        for(Marker marker:mtripMarkers){
            marker.remove();
        }
    }

    public void resetSelectedMarker(){
        if(mtripMarkers!=null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }
    public void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for(final ClusterMarker clusterMarker: mClusterMarkers){

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(clusterMarker.getUser().getUser_id());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getUser().getUser_id().equals(updatedUserLocation.getUser().getUser_id())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeo_point().getLatitude(),
                                                updatedUserLocation.getGeo_point().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));

                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }

    private void resetMap(){
        if(googleMap != null) {
            googleMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    private void addMapMarkers(){
        if(googleMap !=null){
            resetMap();
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(),googleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(getActivity(),
                        googleMap,mClusterManager);
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for(UserLocation userLocation:mUserLocation){
                Log.d(TAG,"addMapMarkers:location:" + userLocation.getGeo_point().toString());
                try{
                    String snippet;
                    if(userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                        snippet = "This is you";
                    }
                    else{
                        snippet = "Determine route to" + userLocation.getUser().getUsername() + "?";
                    }
                    int avatar = R.drawable.cwm_logo;
                    try {
                        avatar = Integer.parseInt(userLocation.getUser().getAvatar());

                    }catch(NumberFormatException e){
                        Log.d(TAG,"addMapMarkers:no avatar for" + userLocation.getUser().getUsername() + ", setting default avatar.");
                        Log.e(TAG,"addMapMarker:" + e.getMessage());
                    }
                    ClusterMarker clusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(),userLocation.getGeo_point().getLongitude()),
                            userLocation.getUser().getUsername(),
                            snippet,
                            avatar,
                            userLocation.getUser());
                    mClusterManager.addItem(clusterMarker);
                    mClusterMarkers.add(clusterMarker);

                }catch(NullPointerException e){
                    Log.e(TAG,"addMapMarkers:NullPointerException:" + e.getMessage());
                }
            }
            mClusterManager.cluster();
            setCameraView();
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(false);
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        init();
        googleMap = map;
        //addMapMarkers();
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnPolylineClickListener(this);
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(marker.getSnippet().contains("Determine route to")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Look for the Best Route to the intended destination")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    getActivity().startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }else if (marker.getSnippet().contains("Address")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Look for the Best Route to the Intended Destination")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try {
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    getActivity().startActivity(mapIntent);
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage());
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }
                        }})
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

        }else{
            if(marker.getSnippet().equals("This is you")){
                marker.hideInfoWindow();
            }
            else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                String latitude = String.valueOf(marker.getPosition().latitude);
                                String longitude = String.valueOf(marker.getPosition().longitude);
                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");

                                try{
                                    if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                        getActivity().startActivity(mapIntent);
                                    }
                                }catch (NullPointerException e){
                                    Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                    Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getDirectionsLeg().endLocation.lat,
                        polylineData.getDirectionsLeg().endLocation.lng
                );

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip #" + index)
                        .snippet("Duration: " + polylineData.getDirectionsLeg().duration
                        ));


                marker.showInfoWindow();
                mtripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(),data);
                Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(String.valueOf(place));
                placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG,"geoLocate:geolocating");

        String searchString = editText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString,1);

        }catch(IOException e){
            Log.e(TAG,"geoLocate:IOException" + e.getMessage());
        }
        if(list.size()>0){
            Address address = list.get(0);

            Log.d(TAG,"geoLocate:found a location" + address.toString());

            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission:getting location permissions");
        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mlocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        Permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    Permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissions:called");
        mlocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult:permission failed");
                            mlocationPermissionsGranted = false;
                            return;
                        }
                        Log.d(TAG, "onRequestPermissionsResult:permission granted");
                        mlocationPermissionsGranted = true;
                        //initialize our map
                    }
                }
            }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation:getting the device location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            if (mlocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete:found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,"My Location");
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }


        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:Security Exception:" + e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom,String title) {
        Log.d(TAG, "moveCamera:moving the camera to:lat:" + latLng.latitude + "lng" +latLng.longitude);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        // Construct a CameraPosition focusing on target and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            googleMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom,PlacesInfo placesInfo) {
        Log.d(TAG, "moveCamera:moving the camera to:lat:" + latLng.latitude + "lng" +latLng.longitude);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        // Construct a CameraPosition focusing on target and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.clear();
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapterPlace(getActivity()));
        if(placesInfo!= null){
            try{
                String snippet = "Address:" + placesInfo.getAddress() + "\n" +
                        "Phone Number:" + placesInfo.getPhoneNumber() + "\n" +
                        "Website:" + placesInfo.getWebsiteUri() + "\n" +
                        "Price Rating:" + placesInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placesInfo.getName())
                        .snippet(snippet);
                mMarker =  googleMap.addMarker(options);

            }catch(NullPointerException e){
                Log.d(TAG,"moveCamera:NullPointerException" + e.getMessage());
            }
        }else{
            googleMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard();
    }
    private void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener(){


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>(){

        @Override
        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
            PlaceBufferResponse places = task.getResult();
            final Place place = places.get(0);
            try{
                mPlace = new PlacesInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG,"onResult: name"+ place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG,"onResult: address"+ place.getAddress());
                //mPlace.setAttributions(place.getAttributions().toString());
                //Log.d(TAG,"onResult: attributions"+ place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG,"onResult: id"+ place.getId());
                mPlace.setLatLng(place.getLatLng());
                Log.d(TAG,"onResult: latlng"+ place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG,"onResult: rating"+ place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG,"onResult: phone number"+ place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG,"onResult: website uri"+ place.getWebsiteUri());

                Log.d(TAG,"onResult: place:"+ mPlace.toString());

            }catch(NullPointerException e){
                Log.e(TAG,"onResult:NullPointerException:" + e.getMessage());
            }
            catch(RuntimeRemoteException e){
                Log.e(TAG,"onResult:NullPointerException:" + e.getMessage());
            }
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM,mPlace);


            places.release();
        }

    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_full_screen_map:{

                if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                }
                else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
                break;
            }
            case R.id.info_pin:{
                Log.d(TAG,"onClick:place info:" + mPlace.toString());
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG,"onClick:place info" + mPlace.toString());
                        mMarker.showInfoWindow();
                    }

                }catch(NullPointerException e){
                    Log.e(TAG,"onClick:NullPointerException" + e.getMessage());
                }
            }
            case R.id.gps_pin:{
                Log.d(TAG,"onClick:gps icon clicked");
                getDeviceLocation();
            }
            case R.id.refresh:{
                addMapMarkers();
            }
            case R.id.places_pic:{
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG,"onClick:GooglePlayServicesRepairableException" + e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG,"onClick:GooglePlayServicesNotAvailableException" + e.getMessage());
                }
            }
        }
    }

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }
    private void init(){
        Log.d(TAG,"init:initializing");



        editText.setOnItemClickListener(mAutocompleteClickListener);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getActivity(),mGeoDataClient,LAT_LNG_BOUNDS,null);
        editText.setAdapter(placeAutocompleteAdapter);


        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        ||actionId== EditorInfo.IME_ACTION_DONE
                        ||event.getAction()== event.ACTION_DOWN
                        ||event.getAction()== event.KEYCODE_ENTER){

                    geoLocate();

                }
                return false;
            }
        });
        hideSoftKeyboard();
    }

    @Override
    public void onUserClicked(int position) {
        Log.d(TAG,"onUserClicked:selected a user" + mUserList.get(position).getUser_id());

        String selectedUserId = mUserList.get(position).getUser_id();
        for(ClusterMarker clusterMarker:mClusterMarkers){
            if(selectedUserId.equals(clusterMarker.getUser().getUser_id())){
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(clusterMarker.getPosition().latitude,
                                clusterMarker.getPosition().longitude)),
                        600,null);
                break;
            }
        }
    }
}


















