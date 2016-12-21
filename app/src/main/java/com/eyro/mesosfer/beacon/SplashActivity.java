package com.eyro.mesosfer.beacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.eyro.mesosfer.FindCallback;
import com.eyro.mesosfer.MesosferBeacon;
import com.eyro.mesosfer.MesosferException;
import com.eyro.mesosfer.beacon.model.Beacon;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MesosferBeacon.getQuery().findAsync(new FindCallback<MesosferBeacon>() {
            @Override
            public void done(List<MesosferBeacon> list, MesosferException e) {
                // check if there is an exception happen
                if (e != null) {
                    Log.e("SPLASH", "Error when downloading beacon: " + e);
                    return;
                }

                // get the result
                if (list != null && !list.isEmpty()) {
                    // populate the result into list of Beacon
                    ArrayList<Beacon> beacons = new ArrayList<>();
                    for (MesosferBeacon cloudBeacon : list) {
                        Beacon beacon = new Beacon();
                        beacon.setProximityUUID(cloudBeacon.getProximityUUID());
                        beacon.setMajor(cloudBeacon.getMajor());
                        beacon.setMinor(cloudBeacon.getMinor());
                        beacon.setIdentifier(cloudBeacon.getObjectId());

                        beacons.add(beacon);
                    }

                    // open main activity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putParcelableArrayListExtra("BEACONS", beacons);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
