package ir.map.sdkdemo_service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reverse:
                startActivity(new Intent(this, ReverseActivity.class));
                break;
            case R.id.search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.route:
                startActivity(new Intent(this, RouteActivity.class));
                break;
            case R.id.staticMap:
                startActivity(new Intent(this, StaticMapActivity.class));
                break;
        }
    }
}
