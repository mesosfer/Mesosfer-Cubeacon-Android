package com.eyro.mesosfer.beacon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.eyro.cubeacon.CBMonitoringListener;
import com.eyro.cubeacon.CBRegion;
import com.eyro.cubeacon.CBServiceListener;
import com.eyro.cubeacon.Cubeacon;
import com.eyro.cubeacon.MonitoringState;
import com.eyro.cubeacon.SystemRequirementManager;
import com.eyro.mesosfer.FindCallback;
import com.eyro.mesosfer.MesosferBeacon;
import com.eyro.mesosfer.MesosferException;
import com.eyro.mesosfer.MesosferLog;
import com.eyro.mesosfer.MesosferStoryline;
import com.eyro.mesosfer.MesosferStorylineDetail;
import com.eyro.mesosfer.SaveCallback;
import com.eyro.mesosfer.beacon.model.Beacon;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CBServiceListener, CBMonitoringListener {

  private Cubeacon beaconManager;
  private List<CBRegion> regions;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // init beacon manager instance
    beaconManager = Cubeacon.getInstance();

    // get list of parcel beacon from an activity intent
    List<Beacon> beaconList = getIntent().getParcelableArrayListExtra("BEACONS");

    // populate list of beacon into region scanning
    regions = new ArrayList<>();
    for (Beacon beacon : beaconList) {
      CBRegion region = new CBRegion(beacon.getIdentifier(), beacon.getProximityUUID(),
          beacon.getMajor(), beacon.getMinor());
      regions.add(region);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    // check all requirements to comply
    if (SystemRequirementManager.checkAllRequirementUsingDefaultDialog(this)) {
      // connecting to the service scanning
      beaconManager.connect(this);
    }
  }

  @Override
  protected void onDestroy() {
    if (beaconManager != null) {
      beaconManager.disconnect(this);
    }

    super.onDestroy();
  }

  @Override
  public void onBeaconServiceConnect() {
    try {
      // set beacon listener
      beaconManager.addMonitoringListener(this);

      // start scanning beacon when service already connected
      beaconManager.startMonitoringForRegions(regions);
    } catch (RemoteException e) {
      Log.e("MAIN", "Error happen while monitoring region: " + e);
    }
  }

  @Override
  public void didEnterRegion(CBRegion region) {
    Log.d("MAIN", "Entering region: " + region);
    searchForStoryline(region, MesosferBeacon.Event.ENTER);
    sendLog(region, MesosferBeacon.Event.ENTER);
  }

  @Override
  public void didExitRegion(CBRegion region) {
    Log.d("MAIN", "Exiting region: " + region);
    searchForStoryline(region, MesosferBeacon.Event.EXIT);
    sendLog(region, MesosferBeacon.Event.EXIT);
  }

  @Override
  public void didDetermineStateForRegion(MonitoringState state, CBRegion region) {
    switch (state) {
      case INSIDE:
        Log.d("MAIN", "Change state to entering region: " + region);
        break;
      case OUTSIDE:
        Log.d("MAIN", "Change state to exiting region: " + region);
        break;
    }
  }

  private void searchForStoryline(CBRegion region, MesosferBeacon.Event event) {
    MesosferStorylineDetail
        .getQuery()
        .whereEqualTo("beacons", region.getIdentifier())
        .whereEqualTo("event", event.name())
        .setLimit(1)
        .findAsync(new FindCallback<MesosferStorylineDetail>() {
          @Override
          public void done(List<MesosferStorylineDetail> list, MesosferException e) {
            if (e != null) {
              Log.e("MAIN", "Error happen when finding storyline: " + e);
              return;
            }

            if (list != null && !list.isEmpty()) {
              MesosferStorylineDetail detail = list.get(0);
              displayStoryline(detail);
            }
          }
        });
  }

  private void displayStoryline(MesosferStorylineDetail detail) {
    if (detail.getCampaign() == MesosferStoryline.Campaign.TEXT) {
      String title = detail.getAlertTitle();
      String message = detail.getAlertMessage();

      new AlertDialog.Builder(this)
          .setTitle(title)
          .setMessage(message)
          .setNegativeButton(android.R.string.ok, null)
          .show();

      showNotification(title, message);
    }
  }

  private void sendLog(CBRegion region, final MesosferBeacon.Event event) {
    String beaconId = region.getIdentifier();
    MesosferBeacon beacon = MesosferBeacon.createWithObjectId(beaconId);

    MesosferLog.createLog()
        .setEvent(event)
        .setBeacon(beacon)
        .setModule(MesosferBeacon.Module.PRESENCE)
        .sendAsync(new SaveCallback() {
          @Override
          public void done(MesosferException e) {
            if (e != null) {
              Log.e("MAIN", "Failed to send log: " + e);
              return;
            }

            String state = event == MesosferBeacon.Event.ENTER ? "Check in" : "Check out";
            Toast.makeText(MainActivity.this, state + " sent.", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void showNotification(String title, String message) {
    Intent notifyIntent = new Intent(this, MainActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivities(this, 1234,
        new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new Notification.Builder(this)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      notificationManager.notify(5678, notification);
    }
  }
}
