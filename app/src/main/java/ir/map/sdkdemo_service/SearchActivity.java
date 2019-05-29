package ir.map.sdkdemo_service;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ir.map.sdk_common.MapirLatLng;
import ir.map.sdk_map.LatLngMapper;
import ir.map.sdk_map.annotations.MarkerOptions;
import ir.map.sdk_map.camera.CameraUpdateFactory;
import ir.map.sdk_map.geometry.LatLng;
import ir.map.sdk_map.maps.MapirMap;
import ir.map.sdk_map.maps.OnMapReadyCallback;
import ir.map.sdk_map.maps.SupportMapFragment;
import ir.map.sdk_services.ServiceHelper;
import ir.map.sdk_services.models.MapirError;
import ir.map.sdk_services.models.MapirSearchItem;
import ir.map.sdk_services.models.MapirSearchResponse;
import ir.map.sdk_services.models.base.ResponseListener;

public class SearchActivity extends AppCompatActivity implements
        ResponseListener<MapirSearchResponse>, View.OnClickListener {

    private EditText etSearch;
    private MapirMap mMap;
    private ImageView imgSearch;
    private MapirLatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        imgSearch = findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(this);

        setupMap();
    }

    private void setupMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map wrap the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMapView))
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapirMap var1) {
                            mMap = var1;
                            latLng = new MapirLatLng(35.6964895, 51.279745);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLngMapper.toLatLng(latLng), 12));

                            etSearch.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if (!s.toString().isEmpty() && s.toString().length() > 3) {
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                new ServiceHelper().autoCompleteSearch(
                                                        etSearch.getText().toString(),
                                                        latLng.latitude, latLng.longitude,
                                                        null, SearchActivity.this);
                                            }
                                        }, 2000);
                                    }
                                }
                            });
                        }
                    });
        }
    }

    @Override
    public void onSuccess(MapirSearchResponse response) {
        mMap.clear();
        for (MapirSearchItem item : response.getValues()) {
            mMap.addMarker(new MarkerOptions().title(item.text)
                    .position(new LatLng(item.coordinate.latitude, item.coordinate.longitude)));
            latLng = new MapirLatLng(item.coordinate.latitude, item.coordinate.longitude);//Zoom on last item in responses
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLngMapper.toLatLng(latLng), 12));

    }

    @Override
    public void onError(MapirError error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgSearch:
                new ServiceHelper()
                        .search(etSearch.getText().toString(), latLng.latitude, latLng.longitude, null, this);

                break;
        }
    }
}
