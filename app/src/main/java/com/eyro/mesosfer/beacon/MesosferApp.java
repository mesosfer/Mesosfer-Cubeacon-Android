package com.eyro.mesosfer.beacon;

import android.app.Application;

import com.eyro.cubeacon.Cubeacon;
import com.eyro.mesosfer.Mesosfer;

/**
 * Created by Eyro on 12/20/16.
 */

public class MesosferApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Mesosfer SDK
        Mesosfer.initialize(this, "YOUR-APP-ID-HERE", "YOUR-CLIENT-KEY-HERE");

        // initialize Cubeacon SDK
        Cubeacon.initialize(this);
    }
}
