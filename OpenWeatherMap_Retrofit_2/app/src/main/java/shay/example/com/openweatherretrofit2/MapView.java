package shay.example.com.openweatherretrofit2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapView extends AppCompatActivity  implements OnMapReadyCallback{

    GoogleMap mMap;
    private LatLng camera_latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_expanded);

    /*    Slide slide = new Slide(Gravity.BOTTOM);
        slide.setInterpolator(AnimationUtils.loadInterpolator(this,android.R.interpolator.linear_out_slow_in));
        getWindow().setEnterTransition(slide);*/



        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.large_map);
        mapFragment.getMapAsync(this);
        camera_latLng = WeatherActivity.camera_bounds.getCenter();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));// custom json map style created
        setDefaultCameraPosition();
    }

    private void setDefaultCameraPosition() {

    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(camera_latLng,12));
    }
    }
