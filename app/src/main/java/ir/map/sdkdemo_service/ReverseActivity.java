package ir.map.sdkdemo_service;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import ir.map.sdk_common.MapirLatLng;
import ir.map.sdk_map.annotations.MarkerOptions;
import ir.map.sdk_map.camera.CameraUpdateFactory;
import ir.map.sdk_map.geometry.LatLng;
import ir.map.sdk_map.maps.MapirMap;
import ir.map.sdk_map.maps.OnMapReadyCallback;
import ir.map.sdk_map.maps.SupportMapFragment;
import ir.map.sdk_services.ServiceHelper;
import ir.map.sdk_services.models.MapirError;
import ir.map.sdk_services.models.MapirReverse;
import ir.map.sdk_services.models.base.ResponseListener;

public class ReverseActivity extends AppCompatActivity implements ResponseListener<MapirReverse> {

    private TextView tvInstruction;
    private MapirMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvInstruction.setText("Click on the map to show Reverse Info");

        setUpMap();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map wrap the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMapView))
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapirMap var1) {
                            mMap = var1;
                            // Check if we were successful in obtaining the map.
                            if (mMap != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.6964895, 51.279745), 12));
                                mMap.setOnMapClickListener(new MapirMap.OnMapClickListener() {
                                    @Override
                                    public void onMapClick(LatLng paramLatLng) {
                                        mMap.addMarker(new MarkerOptions().position(paramLatLng).title("point bookmark"));
                                        new ServiceHelper().getReverseGeoInfo(
                                                paramLatLng.getLatitude(), paramLatLng.getLongitude(), ReverseActivity.this);
                                    }
                                });
                            }
                        }
                    });

        }
    }

    @Override
    public void onSuccess(MapirReverse response) {
        Toast.makeText(this, response.addressCompact, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(MapirError error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show();
    }
}
