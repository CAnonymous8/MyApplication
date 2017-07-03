package com.example.sunlinlin.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

public class MainActivity extends Activity implements BeaconConsumer {
    private String TAG = "MainActivity";
    private BeaconManager beaconManager;
    private TextView logText;
    int count = 0;
    private static final String UUID = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825";
    private static final String parce = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logText = (TextView) findViewById(R.id.log_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(parce));
        beaconManager.bind(this);
        Log.i(TAG, "onCreate: bind");
    }


    /**
     *
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "didEnterRegion: ");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "didExitRegion: ");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.i(TAG, "didDetermineStateForRegion: "+ i);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region(UUID, null, null, null));
        } catch (RemoteException e) {
        }
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.i(TAG, "didRangeBeaconsInRegion: " + collection.size());
                count++;
                if (collection.size() > 0) {
                    //collection.iterator().next().getDistance()
                    String text = "uuid: " + collection.iterator().next().getServiceUuid() + "\r\n" +
                        "dis: " + collection.iterator().next().getDistance() + "m" + "\r\n" +
                        "BluetoothName: " + collection.iterator().next().getBluetoothName() + "\r\n" +
                        "BluetoothAddress: " + collection.iterator().next().getBluetoothAddress() + "\r\n" +
                        "ParserIdentifier: " + collection.iterator().next().getParserIdentifier() + "\r\n" +
                        "BeaconTypeCode: " + collection.iterator().next().getBeaconTypeCode() + "\r\n" +
                        "Rssi: " + collection.iterator().next().getRssi() + "\r\n" +
                        "TxPower: " + collection.iterator().next().getTxPower() + "\r\n" +
                        "count: " + count+"\r\n"+
                        "id1:"+collection.iterator().next().getId1()+"\r\n"+
                        "id2:"+collection.iterator().next().getId2()+"\r\n"+
                        "id3:"+collection.iterator().next().getId3()+"\r\n"
                        ;
                    //Log.i(TAG, text);
                    setText(text);
                }
            }
        });
        try {
            //beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", Identifier.parse(UUID), null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));

            List<BeaconParser> l = beaconManager.getBeaconParsers();
            Log.i(TAG, "onBeaconServiceConnect: l.size=" + l.size());
            if (l.size() > 0) {
                for (BeaconParser p : l
                        ) {
                    Log.i(TAG, "onBeaconServiceConnect: l:" + p.getIdentifier());

                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "onBeaconServiceConnect: e " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        beaconManager.unbind(this);
        Log.i(TAG, "onDestroy: unbind");
        super.onDestroy();
    }

    public void setText(final String str) {
        runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                logText.setText(str);
            }
        });
    }

    public void fashe(View v) {
        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
// Change the layout below for other beacon types
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        final BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        Log.i(TAG, "fashe: "+beaconTransmitter.isStarted());
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
                Log.i(TAG, "onStartSuccess: "+beaconTransmitter.isStarted());
            }
        });
        Log.i(TAG, "fashe: "+beaconTransmitter.isStarted());
    }
}
