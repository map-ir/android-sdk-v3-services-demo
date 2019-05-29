package ir.map.sdkdemo_service;

import android.app.Application;
import ir.map.sdk_map.Mapir;
import ir.map.sdk_services.ServiceSDK;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Mapir.getInstance(this, null);
        ServiceSDK.init(this);
    }
}
