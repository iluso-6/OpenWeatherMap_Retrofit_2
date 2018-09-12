package shay.example.com.openweatherretrofit2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import shay.example.com.openweatherretrofit2.Models.Model;
import shay.example.com.openweatherretrofit2.Models.Weather;
import shay.example.com.openweatherretrofit2.Util.Utilities;

/**
 * Created by Shay de Barra 03/05/2018
 */


public class WeatherActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraIdleListener {

    public static LatLngBounds camera_bounds;
    public static float camera_zoom;
    // foreword references for text fields
    TextView tempView, textViewTempHighLow, windView, humidView, windDirView, textViewSunInfo, barText, description;
    ImageView compass_img, weather_icon;
    TileOverlay tileOver;
    private String OWM_TILE_URL;
    private GoogleMap mMap;
    private GoogleMap mapThumb;
    private LatLng camera_latLng;
    private LatLng user_latLng;
    private boolean cameraPosHasBeenUpdated;
    private String tileType = "clouds";
    private ImageButton cloud_button;
    private boolean cloud_selected;
    private MapFragment mapFragment;

    @Override
    public void onMapReady(GoogleMap map) {
        initThumbMap();
        mMap = map;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));// custom json map style created
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMapClickListener(this);
        cameraPosHasBeenUpdated = false;
        setDefaultCameraPosition();
    }

    private void initThumbMap() {

        MapFragment thumbFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.thumb_map);
        thumbFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap tMap) {
                mapThumb = tMap;
                mapThumb.getUiSettings().setAllGesturesEnabled(false);
                mapThumb.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getApplicationContext(), R.raw.style_thumb));// custom json map style created

            }
        });
    }

    private void setDefaultCameraPosition() {
        CameraPosition cameraPosition;

    //    camera_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        // precautionary measure to set camera center
     //   camera_latLng = ((camera_latLng == null) ? camera_bounds.getCenter() : camera_latLng);

        cameraPosition = new CameraPosition.Builder()
                .target(user_latLng)      // Sets the center of the map to the users position
                .zoom(12)                   // Sets the zoom
                .bearing(0)                // -90 = west, 90 = east
                .tilt(0)
                .build();


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.e("onFinish", "Map: ");
                cameraPosHasBeenUpdated = false;
                if (tileOver != null) {
                    tileOver.remove();
                }
            }

            @Override
            public void onCancel() {

            }
        });

    }

    private void setCloudsCamera() {
        CameraPosition cameraPosition;

        //    LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        // precautionary measure to set camera center
        //     camera_latLng = ((camera_latLng == null) ? init_bounds.getCenter() : camera_latLng);

        cameraPosition = new CameraPosition.Builder()
                .target(camera_bounds.getCenter())      // Sets the center of the map
                .zoom(5)                   // Sets the zoom
                .bearing(0)
                .tilt(0)
                .build();
        tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTilePovider()));
        tileOver.setVisible(false);

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        tileOver.setVisible(true);

    }

    @Override
    public void onMapClick(LatLng latLng) {

    //    Log.e("onMapClick", "onMapClick: ");
    }

    @Override
    public void onCameraMove() {
        cameraPosHasBeenUpdated = true;
    //    Log.e("onCameraMove", "onCameraMove: ");
    }

    @Override
    public void onCameraIdle() {
        LatLngBounds init_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        camera_bounds = init_bounds;
        camera_zoom = mMap.getCameraPosition().zoom;
        camera_latLng = init_bounds.getCenter();
        if (cameraPosHasBeenUpdated) {
            cameraPosHasBeenUpdated = false;
            // pause everything on map
            mMap.getUiSettings().setAllGesturesEnabled(false);


            String updated_lat = init_bounds.getCenter().latitude + "";
            String updated_lon = init_bounds.getCenter().longitude + "";
            // get refreshed updated weather for these coordinates
            refreshWeatherData(updated_lat, updated_lon);
            Log.e("cameraPosHasBeenUpdated", "onCameraIdle: " );
        }
    }

    private void updateThumbnailMap(LatLngBounds init_bounds) {
        CameraPosition cameraPosition = null;

        cameraPosition = new CameraPosition.Builder()
                .target(init_bounds.getCenter())      // Sets the center of the map
                .zoom(0)                   // Sets the zoom
                .bearing(0)                // -90 = west, 90 = east
                .tilt(0)
                .build();

        mapThumb.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private TileProvider createTilePovider() {

        return new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String fUrl = String.format(OWM_TILE_URL, tileType == null ? "clouds" : tileType, zoom, x, y);
                URL url = null;
                try {
                    url = new URL(fUrl);
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                }
                //     Log.e("URL", "getTileUrl: " + url);
                return url;
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);

        OWM_TILE_URL = getString(R.string.tile_url);
        String query_lat = getIntent().getStringExtra("latitude");
        String query_lon = getIntent().getStringExtra("longitude");

        // convert values for map camera
        double latitude = Double.valueOf(query_lat);
        double longitude = Double.valueOf(query_lon);
        camera_latLng = new LatLng(latitude, longitude);
        user_latLng = camera_latLng;

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert custom logo and name
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        }


        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get the references in onCreate when first loading
        barText = findViewById(R.id.bar_text);

        compass_img = findViewById(R.id.compassImg);

        tempView = findViewById(R.id.textViewTemp);
        textViewTempHighLow = findViewById(R.id.tempHighLow);
        windView = findViewById(R.id.textViewWind);
        humidView = findViewById(R.id.textViewHumidity);
        windDirView = findViewById(R.id.textViewWindDir);
        textViewSunInfo = findViewById(R.id.textViewSunUpDown);

        // info box
        weather_icon = findViewById(R.id.weather_icon);
        description = findViewById(R.id.description_text);

        cloud_button = findViewById(R.id.cloud);
        cloud_selected = false;
        cloud_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cloud_selected) {
                    cloud_selected = true;
                    setCloudsCamera();
                    Log.e("cloud_selected", "true ");
                } else {
                    Log.e("cloud_selected", "false ");
                    cloud_selected = false;
                    setDefaultCameraPosition();
                }
            }
        });

        // start the API query
        refreshWeatherData(query_lat, query_lon);
        /*
        ImageView magnifyer = findViewById(R.id.magnifyer_btn);
        magnifyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapExpanded();
            }
        });*/
    }
/*
    private void showMapExpanded() {
        Log.e("XXXX", "showMapExpanded: ");
// Ordinary Intent for launching a new activity
        Intent intent = new Intent(this, MapView.class);

        // Get the transition name from the string
        String transitionName = getString(R.string.transition_string);

        // Define the view that the animation will start from
        View viewStart = mapFragment.getView();

        ActivityOptionsCompat options =

                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Objects.requireNonNull(viewStart),   // Starting view
                        transitionName    // The String
                );


        //Start the Intent
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }
*/
    private void refreshWeatherData(String query_lat, String query_lon) {
        //  Log.e("refreshWeatherData "+query_lat, "refreshWeatherData: "+query_lon );
        String baseURL = getString(R.string.base_url);
        String api_str = getString(R.string.api);
        String units = getString(R.string.metric);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        WeatherClient client = retrofit.create(WeatherClient.class);

        // Values to be mapped to the Model class , note the query Strings for Interface @Query ... crap documentation on this section, trial and error
        Call<Model> call = client.getWeatherData(query_lat, query_lon, api_str, units);

        // NB call.enqueue - for running on a separate thread
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(@NonNull Call<Model> call, @NonNull Response<Model> response) {
                Model model = response.body();

                assert model != null;
                createLayout(model);
            }

            @Override
            public void onFailure(@NonNull Call<Model> call, @NonNull Throwable t) {
                Log.e("ERROR", "onFailure: " + t);
                Toast.makeText(WeatherActivity.this, "Error: " + t, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createLayout(Model model) {
        if (mMap == null) {
            return;
        }
        LatLngBounds init_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        updateThumbnailMap(init_bounds);

        //

        //allow camera to update when map is moved again
        cameraPosHasBeenUpdated = true;
        mMap.getUiSettings().setAllGesturesEnabled(true);

        if (model == null) {
            return;
        } else if (model.getWind() == null) {
            return;
        } else if (model.getWind().getDeg() == null) {
            // the Logcat on Android 3 is unreliable, nearly sure this is catching error but no Log retrieved
            return;
        }

        // cast the wind direction to a clean number
        String deg_val = model.getWind().getDeg() + "";


        // clean String concentation's of values to be set
        String temp = getString(R.string.temp) + model.getMain().getTemp() + getString(R.string.celcius);
        String tempMinMax = getString(R.string.min) + model.getMain().getTempMin() + getString(R.string.celcius) + getString(R.string.max) + model.getMain().getTempMax() + getString(R.string.celcius);
        String wind = getString(R.string.wind) + model.getWind().getSpeed() + getString(R.string.kph);
        String windDir = getString(R.string.wind_dir) + deg_val + getString(R.string.degree);
        String humidity = getString(R.string.humidity) + model.getMain().getHumidity() + getString(R.string.percent);

        // set the compass rotation

        Float float_val = Float.parseFloat(deg_val);
        compass_img.setRotation(float_val);

        // set the values in the UI
        String title_name = model.getName();
        barText.setText(title_name);

        tempView.setText(temp);
        textViewTempHighLow.setText(tempMinMax);
        windView.setText(wind);
        windDirView.setText(windDir);
        humidView.setText(humidity);


        // pretty sure this is correct ...
        String sunrise_str = Utilities.getHumanReadable(model.getSys().getSunrise());
        String sunset_str = Utilities.getHumanReadable(model.getSys().getSunset());


        // head wrecking stuff ...
        String sun_up_down = getString(R.string.sunrise) + sunrise_str + getString(R.string.sunset) + sunset_str;
        textViewSunInfo.setText(sun_up_down);
        //    Log.e("SUNRISE: ", "createLayout: " + sun_up_down);

        // populate the info details box
        List<Weather> weather_list = model.getWeather();
        // the icon is in the first element {} of the list .get(0)
        String icon_url = getString(R.string.icon_url) + weather_list.get(0).getIcon() + getString(R.string.png);

        // excellent library to do the heavy lifting of Image handling
        Picasso.with(getApplicationContext()).load(icon_url).into(weather_icon);

        description.setText(weather_list.get(0).getDescription());


    }


}
