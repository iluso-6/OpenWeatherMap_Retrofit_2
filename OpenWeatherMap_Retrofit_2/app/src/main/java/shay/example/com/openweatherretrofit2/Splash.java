package shay.example.com.openweatherretrofit2;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


/**
 * Created by Shay de Barra 03/05/2018
 */


public class Splash extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 4711;
    private static final String TAG = "Splash Activity";

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView splashText;
    ProgressBar progressBar;
    ImageButton refresh;

    // method to go to the next activity with location data in intent
    private void startMainActivity(Location locationResult) {
        String latitude = locationResult.getLatitude() + "";// safe way to convert to string
        String longitude = locationResult.getLongitude() + "";


        Intent intent = new Intent(Splash.this, WeatherActivity.class);// select a new station
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
        progressBar.setVisibility(View.INVISIBLE);
        splashText.setVisibility(View.INVISIBLE);
        startActivity(intent, animation);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashText = findViewById(R.id.textViewSplash);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    splashText.setVisibility(View.VISIBLE);
                    startLocationApi(getApplicationContext());

                }
            }
        });

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert custom logo and name
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        }


        if (!checkPermissions()) {
            requestPermissions();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            splashText.setVisibility(View.VISIBLE);
            startLocationApi(this);

        }

    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.e(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.e(TAG, "Requesting permission");

            showCustomDialog();
        }
    }

    // this custom dialog will personalise the permission request prior to calling them
    private void showCustomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.permissions_dialog);
        dialog.setTitle("Location Permissions");
        dialog.setCancelable(false);
        Button ok = dialog.findViewById(R.id.button_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                startLocationPermissionRequest();// start the actual permissions request
            }
        });


        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        dialog.getWindow().setAttributes(layoutParams);
    }

    // launch the default permissions dialog
    private void startLocationPermissionRequest() {

        ActivityCompat.requestPermissions(Splash.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, " ** Permission granted. ** ");
                // Permission granted.
                startLocationApi(getApplicationContext());
            } else {

                Log.e(TAG, " ** Permission denied. ** ");
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless.

                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                refresh.setVisibility(View.VISIBLE);
                            }
                        });
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationApi(Context context) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            startMainActivity(location);
                            Log.e(TAG, "onSuccess: " + location.getLatitude());
                            // Logic to handle location object
                        }
                    }
                });
    }


    //     implementation 'com.android.support:design:27.1.1' Design library for this
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

}
