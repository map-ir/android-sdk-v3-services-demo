package ir.map.sdkdemo_service;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import ir.map.sdk_services.ServiceHelper;
import ir.map.sdk_services.models.MapirError;
import ir.map.sdk_services.models.base.ResponseListener;

public class StaticMapActivity extends AppCompatActivity implements ResponseListener<Bitmap> {

    private ImageView imgMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_map);

        imgMap = findViewById(R.id.imgMap);

        if (!ImageLoader.getInstance().isInited()) {
            // Create global configuration and initialize ImageLoader with this config
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
        }
        getStaticMap();
    }

    private void getStaticMap() {
        new ServiceHelper().getStaticMap(35.7475389, 51.366879, 12, this);
    }

    @Override
    public void onSuccess(Bitmap response) {
        imgMap.setImageBitmap(response);
    }

    @Override
    public void onError(MapirError error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show();
    }
}
