package com.example.bluetooth_le2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button mAdvertiseButton;
    private static final String TAG = "BLEApp";
    private BluetoothAdapter mBluetoothAdapter;
    final private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdvertiseButton = (Button) findViewById(R.id.advertise_btn);

        mAdvertiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savedInstanceState == null)
                    mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                            .getAdapter();
                // Is Bluetooth supported on this device?
                if (mBluetoothAdapter != null) {

                    // Is Bluetooth turned on?
                    if (mBluetoothAdapter.isEnabled()) {

                        // Are Bluetooth Advertisements supported on this device?
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                            // Everything is supported and enabled, load the method
                            EditText wifiSsid = (EditText) findViewById(R.id.wifi_ssid);
                            EditText wifiPassword = (EditText) findViewById(R.id.wifi_password);
                            //wifiSsid.getText().toString(), wifiPassword.getText().toString()
                            advertise();

                        } else {

                            // Bluetooth Advertisements are not supported.
                            Log.e(TAG, "R.string.bt_ads_not_supported");
                        }
                    } else {
                        Log.e(TAG, "Prompt user to turn on Bluetooth");
                        // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                } else {

                    // Bluetooth is not supported.
                    Log.e(TAG, "R.string.bt_not_supported");
                }
            }
        });
    }

    private void advertise() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        // Check if all features are supported
        if (!adapter.isLe2MPhySupported()) {
            Log.e(TAG, "2M PHY not supported!");
            return;
        }
        if (!adapter.isLeExtendedAdvertisingSupported()) {
            Log.e(TAG, "LE Extended Advertising not supported!");
            return;
        }

        int maxDataLength = adapter.getLeMaximumAdvertisingDataLength();

        AdvertisingSetParameters.Builder parameters = (new AdvertisingSetParameters.Builder())
                .setLegacyMode(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setSecondaryPhy(BluetoothDevice.PHY_LE_2M);

        AdvertiseData data = (new AdvertiseData.Builder())
                .setIncludeDeviceName(true)
                .addServiceData(new ParcelUuid(UUID.randomUUID()), "123456789abcdefghij".getBytes(StandardCharsets.UTF_8)).build();

        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                if (advertisingSet == null) {
                    Log.i(TAG, "Advertising failed");
                }
                Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                        + status);
                AdvertisingSet currentAdvertisingSet = advertisingSet;

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.i(TAG,"Permission is not there");
                }
                try
                {
                    Thread.sleep(5000);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
                currentAdvertisingSet.setAdvertisingData((new AdvertiseData.Builder()).addServiceData(new ParcelUuid(UUID.randomUUID()),"abcdef".getBytes()).build());

                //currentAdvertisingSet.setScanResponseData(new AdvertiseData.Builder().addServiceData(new ParcelUuid(UUID.randomUUID()), "123456".getBytes(StandardCharsets.UTF_8)).build());

            }
            @Override
            public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(TAG, "onAdvertisingDataSet() :status:" + status);
            }

            @Override
            public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                if(advertisingSet==null){
                    Log.i(TAG, "Scan Response failed");
                }
                Log.i(TAG, "onScanResponseDataSet(): status:" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i(TAG, "onAdvertisingSetStopped():");
            }
        };
        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADVERTISE
            ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT,}, STORAGE_PERMISSION_CODE);
        }

        BluetoothAdapter.getDefaultAdapter().setName("BYJUs");
        advertiser.startAdvertisingSet(parameters.build(), data, null, null, null, callback);
        Toast.makeText(this, "Data Advertised", Toast.LENGTH_SHORT).show();



        // After the set starts, you can modify the data and parameters of currentAdvertisingSet.
//        currentAdvertisingSet[0].setAdvertisingData((new
//                AdvertiseData.Builder()).addServiceData(new ParcelUuid(UUID.randomUUID()),
//                "Without disabling the advertiser first, you can set the data, if new data is less than 251 bytes long.".getBytes()).build());
//
//        // Wait for onAdvertisingDataSet callback...
//
//        // Can also stop and restart the advertising
//        currentAdvertisingSet[0].enableAdvertising(false, 0, 0);
//        // Wait for onAdvertisingEnabled callback...
//        currentAdvertisingSet[0].enableAdvertising(true, 0, 0);
//        // Wait for onAdvertisingEnabled callback...
//
//        // Or modify the parameters - i.e. lower the tx power
//        currentAdvertisingSet[0].enableAdvertising(false, 0, 0);
//        // Wait for onAdvertisingEnabled callback...
//        currentAdvertisingSet[0].setAdvertisingParameters(parameters.setTxPowerLevel
//                (AdvertisingSetParameters.TX_POWER_LOW).build());
//        // Wait for onAdvertisingParametersUpdated callback...
//        currentAdvertisingSet[0].enableAdvertising(true, 0, 0);
            // Wait for onAdvertisingEnabled callback...

            // When done with the advertising:
            //advertiser.stopAdvertisingSet(callback);
        }
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == STORAGE_PERMISSION_CODE) {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Bluetooth Permission GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Bluetooth_Admin Permission GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth_Admin Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Bluetooth_Advertise Permission GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth_Advertise Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                    if (grantResults[5] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Bluetooth_Connect Permission GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth_Connect Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
