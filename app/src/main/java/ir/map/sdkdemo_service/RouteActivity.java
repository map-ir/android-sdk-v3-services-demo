package ir.map.sdkdemo_service;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ir.map.sdk_common.MapirLatLng;
import ir.map.sdk_map.annotations.Polyline;
import ir.map.sdk_map.annotations.PolylineOptions;
import ir.map.sdk_map.camera.CameraUpdate;
import ir.map.sdk_map.camera.CameraUpdateFactory;
import ir.map.sdk_map.geometry.LatLng;
import ir.map.sdk_map.geometry.LatLngBounds;
import ir.map.sdk_map.maps.MapirMap;
import ir.map.sdk_map.maps.OnMapReadyCallback;
import ir.map.sdk_map.maps.SupportMapFragment;
import ir.map.sdk_services.RouteMode;
import ir.map.sdk_services.ServiceHelper;
import ir.map.sdk_services.models.MapirError;
import ir.map.sdk_services.models.MapirManeuver;
import ir.map.sdk_services.models.MapirRouteResponse;
import ir.map.sdk_services.models.MapirStepsItem;
import ir.map.sdk_services.models.base.ResponseListener;
import ir.map.sdkdemo_service.route.MapirPolyUtil;
import ir.map.sdkdemo_service.route.MapirSphericalUtil;

public class RouteActivity extends AppCompatActivity implements ResponseListener<MapirRouteResponse> {

    private MapirMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        setUpMap();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map wrap the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMapView))
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapirMap mapirMap) {
                            mMap = mapirMap;
                            // Check if we were successful in obtaining the map.
                            if (mMap != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.6964895, 51.279745), 12));
                                MapirLatLng latLng1 = new MapirLatLng(35.7413024, 51.421966);
                                MapirLatLng latLng2 = new MapirLatLng(35.7411668, 51.4261612);
                                new ServiceHelper()
                                        .getRouteInfo(latLng1, latLng2, RouteMode.BASIC, RouteActivity.this);
                            }
                        }
                    });

        }
    }

    @Override
    public void onSuccess(MapirRouteResponse response) {
        updateRoutingInfo(response);

    }

    @Override
    public void onError(MapirError error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show();
    }


    public void updateRoutingInfo(MapirRouteResponse routingInfo) {

        Polyline mainRouteLine = null;
        Polyline alternateRouteLine = null;
        List<LatLng> latLngsRouteListMain;
        List<LatLng> latLngsRouteListAlternative;
        List<MapirManeuver> mainRouteManeuver = new ArrayList<>();
        List<MapirManeuver> alternateRouteManeuver = new ArrayList<>();
        List<MapirStepsItem> steps = new ArrayList<>();
        List<LatLng> mainIntersectionsPoints = new ArrayList<>();
        List<Polyline> fullMainIntersectionsLines = new ArrayList<>();

        if (mainRouteLine != null) {
            mainRouteLine.remove();
        }
        if (alternateRouteLine != null) {
            alternateRouteLine.remove();
        }

        latLngsRouteListMain = new ArrayList<>();
        latLngsRouteListAlternative = new ArrayList<>();
        //Check Route Info Null
        //Check And Init Main Route If Exists
        if (routingInfo != null && routingInfo.routes != null) {
            for (int i = 0; i < routingInfo.routes.size(); i++) {
                if (routingInfo.routes.get(i).legs != null) {
                    for (int j = 0; j < routingInfo.routes.get(i).legs.size(); j++) {
                        if (routingInfo.routes.get(i).legs.get(j).steps != null) {
                            for (int k = 0; k < routingInfo.routes.get(i).legs.get(j).steps.size(); k++) {
                                if (routingInfo.routes.get(i).legs.get(j).steps.get(k) != null) {
                                    if (i == 0) {
                                        steps.add(routingInfo.routes.get(i).legs.get(j).steps.get(k));
                                        mainRouteManeuver.add(routingInfo.routes.get(i).legs.get(j).steps.get(k).maneuver);
                                        latLngsRouteListMain.addAll(MapirPolyUtil.decode(routingInfo.routes.get(i).legs.get(j).steps.get(k).geometry));
                                    } else if (i == 1) {
                                        alternateRouteManeuver.add(routingInfo.routes.get(i).legs.get(j).steps.get(k).maneuver);
                                        latLngsRouteListAlternative.addAll(MapirPolyUtil.decode(routingInfo.routes.get(i).legs.get(j).steps.get(k).geometry));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Create Main Line And Show It On Map
            if (latLngsRouteListMain.size() > 0) {
                mMap.addPolyline(new PolylineOptions().width(10).addAll(latLngsRouteListMain)
                  .color(getColor(R.color.colorAccent)));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : latLngsRouteListMain) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }

            //Create Alternate Line And Show It On Map
            if (latLngsRouteListAlternative.size() > 0) {
                mMap.addPolyline(new PolylineOptions().width(5).color(getColor(R.color.mapir_alternative_color))
                  .addAll(latLngsRouteListAlternative));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : latLngsRouteListAlternative) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

            }

            //Draw Intersections Lines
//            SphericalUtil.computeOffsetOrigin()
            if (mainRouteManeuver != null && mainRouteManeuver.size() > 0) {
                for (int i = 0; i < mainRouteManeuver.size(); i++) {
                    mainIntersectionsPoints.clear();
                    fullMainIntersectionsLines.clear();
                    LatLng base = new LatLng(mainRouteManeuver.get(i).location.get(1), mainRouteManeuver.get(i).location.get(0));
                    LatLng basePrevious = MapirSphericalUtil.computeOffset(base, 5, mainRouteManeuver.get(i).bearingBefore + 180);
                    LatLng baseNext = MapirSphericalUtil.computeOffset(base, 5, mainRouteManeuver.get(i).bearingAfter);

                    switch (mainRouteManeuver.get(i).type) {
                        case "depart":

                            break;
                        case "turn":
                            mainIntersectionsPoints.add(basePrevious);
                            mainIntersectionsPoints.add(base);
                            mainIntersectionsPoints.add(baseNext);
                            fullMainIntersectionsLines.add(mMap.addPolyline(
                              new PolylineOptions().color(Color.YELLOW)
                                .addAll(mainIntersectionsPoints)));
                            break;
                    }
                }
            }
        }

    }

}
