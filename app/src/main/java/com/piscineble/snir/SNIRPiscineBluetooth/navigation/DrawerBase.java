package com.piscineble.snir.SNIRPiscineBluetooth.navigation;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.piscineble.snir.SNIRPiscineBluetooth.R;
import com.piscineble.snir.SNIRPiscineBluetooth.bluetooth.BluetoothLeService;
import com.piscineble.snir.SNIRPiscineBluetooth.bluetooth.SampleGattAttributes;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.AboutFragment;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.BluetoothFragment;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.DataFragment;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.SMSFragment;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DrawerBase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BluetoothFragment.bluetoothFragmentCallback {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice mConnectedBluetoothDevice, mBluetoothDevice;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private Handler handler1, handler2, handler3, handler4, handler5;

    private double pH, temperature, redox, bilan;
    public final static String DATA_NOTIFICATION =
            "com.example.bluetooth.le.DATA_NOTIFICATION";

    private int pHCharacteristicProperties, temperatureCharacteristicProperties, redoxCharacteristicProperties, bilanCharacteristicProperties;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
    private ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
    private ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
    private ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();


    private boolean marquer_rang_charac = false;
    private int rang_charac = -1;

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String listName = "NAME";
    private final String listUUID = "UUID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_bluetooth);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, new BluetoothFragment());
        ft.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_bluetooth) {
            fragment = BluetoothFragment.newInstance(mConnectedBluetoothDevice);
        } else if (id == R.id.nav_pool) {
            fragment = DataFragment.newInstance(mConnectedBluetoothDevice);

        } else if (id == R.id.nav_sms) {
            fragment = new SMSFragment();

        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();

        } else if (id == R.id.nav_about) {
            fragment = new AboutFragment();

        } else if (id == R.id.nav_quit) {
            finish();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment, "ACTIVE_FRAGMENT");
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onItemSelected(BluetoothDevice bluetoothDevice){
        connect(bluetoothDevice);
    }

    private void connect(BluetoothDevice bluetoothDevice){
        mBluetoothDevice=bluetoothDevice;
        mDeviceAddress = bluetoothDevice.getAddress();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnectedBluetoothDevice=mBluetoothDevice;
                TextView state = (TextView) findViewById(R.id.nav_connexion_state);
                if(mConnectedBluetoothDevice.getName()!=null){
                    state.setText("Connecté à " + mConnectedBluetoothDevice.getName());
                } else {
                    state.setText("Connecté à " + mConnectedBluetoothDevice.getAddress());
                }

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnectedBluetoothDevice = null;
                TextView state = (TextView) findViewById(R.id.nav_connexion_state);
                state.setText("Déconnecté");
                if(handler1!=null){
                    handler1.removeCallbacks(runnable1);
                }
                if(handler2!=null){
                    handler2.removeCallbacks(runnable2);
                }
                if(handler3!=null){
                    handler3.removeCallbacks(runnable3);
                }
                if(handler4!=null){
                    handler4.removeCallbacks(runnable4);
                }

            }else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                handler1 = new Handler();
                handler1.post(runnable1);
                handler2 = new Handler();
                handler2.post(runnable2);
                handler3 = new Handler();
                handler3.post(runnable3);
                handler4 = new Handler();
                handler4.post(runnable4);
                handler5 = new Handler();
                handler5.post(runnable5);

            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)!=null){
                    if(intent.getStringExtra(BluetoothLeService.EXTRA_DATA).equals("PH")){
                        pH=Double.parseDouble(intent.getStringExtra(BluetoothLeService.PH_DATA));
                    } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).equals("TEMPERATURE")){
                        temperature=Double.parseDouble(intent.getStringExtra(BluetoothLeService.TEMPERATURE_DATA));
                    } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).equals("REDOX")){
                        redox=Double.parseDouble(intent.getStringExtra(BluetoothLeService.REDOX_DATA));
                    } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).equals("BILAN")){
                        bilan=Double.parseDouble(intent.getStringExtra(BluetoothLeService.BILAN_DATA));
                    }


            }

                Log.i("DONNEES RECUES", "PH = "+pH+"\nTEMPERATURE = "+temperature+"\nREDOX = "+redox+"\nBILAN = "+bilan);
            }
        }
    };
    //
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();

            if ((uuid.equals(SampleGattAttributes.PISCINE_SERVICE))&&(!marquer_rang_charac)){
                marquer_rang_charac = true;
            }
            currentServiceData.put(
                    listName, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(listUUID, uuid);
            gattServiceData.add(currentServiceData);

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                if (marquer_rang_charac==true) rang_charac = charas.indexOf(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        listName, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(listUUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattCharacteristics.add(charas);
            Log.i("APP", ""+charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }


    //ICI, C'EST UN PEU BOURRIN MAIS J'AI PAS TROUVE MIEUX...
    private Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            getPH();
            handler1.postDelayed(runnable1, 800);
        }
    };

    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            getTemperature();
            handler2.postDelayed(runnable2, 1200);
        }
    };

    private Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            getRedox();
            handler3.postDelayed(runnable3, 1800);
        }
    };

    private Runnable runnable4 = new Runnable() {
        @Override
        public void run() {
            getBilan();
            handler3.postDelayed(runnable4, 2100);
        }
    };


    private Runnable runnable5 = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(DATA_NOTIFICATION);
            intent.putExtra("ph",""+pH);
            intent.putExtra("temperature",""+temperature);
            intent.putExtra("redox",""+redox);
            intent.putExtra("bilan", ""+bilan);
            sendBroadcast(intent);
            handler4.postDelayed(runnable5, 5000);
        }
    };

    private void getPH() {
        final BluetoothGattCharacteristic pHCharacteristic =
                charas.get(rang_charac-3);
        pHCharacteristicProperties = pHCharacteristic.getProperties();
        if ((pHCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(pHCharacteristic);
        }
        if ((pHCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = pHCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(pHCharacteristic, true);
        }
    }

    private void getTemperature(){
        //POUR LA TEMPERATURE
        final BluetoothGattCharacteristic temperatureCharacteristic = charas.get(rang_charac-2);
        temperatureCharacteristicProperties = temperatureCharacteristic.getProperties();
        if ((temperatureCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(temperatureCharacteristic);
        }
        if ((temperatureCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = temperatureCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(temperatureCharacteristic, true);
        }
    }

    private void getRedox(){
        //POUR LE REDOX
        final BluetoothGattCharacteristic redoxCharacteristic = charas.get(rang_charac-1);
        redoxCharacteristicProperties = redoxCharacteristic.getProperties();
        if ((redoxCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(redoxCharacteristic);
        }
        if ((redoxCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = redoxCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(redoxCharacteristic, true);
        }
    }

    private void getBilan() {
        final BluetoothGattCharacteristic bilanCharacteristic =
                charas.get(rang_charac);
        bilanCharacteristicProperties = bilanCharacteristic.getProperties();
        if ((bilanCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(bilanCharacteristic);
        }
        if ((bilanCharacteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = bilanCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(bilanCharacteristic, true);
        }
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}