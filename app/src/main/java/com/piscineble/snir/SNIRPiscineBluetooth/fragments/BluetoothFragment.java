package com.piscineble.snir.SNIRPiscineBluetooth.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.piscineble.snir.SNIRPiscineBluetooth.R;
import com.piscineble.snir.SNIRPiscineBluetooth.bluetooth.BluetoothLeService;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.bluetooth.DeviceAdapter;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.MyDividerItemDecoration;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//TODO : ESSAYER DE SE CONNECTER A UN AUTRE APPAREIL DEVRAIT DECONNECTER L'ANCIER ET TENTER DE SE CONNECTER AU NOUVEAU. ICI RIEN NE SE PASSE : UNBINDSERVICE?
//TODO : ATTENDRE LA REPONSE DU BROADCAST AVANT DE POUVOIR FAIRE UNE AUTRE ACTION (APRES AVOIR RESOLU LE PROBLEME DE LA DEUXIEME CONNEXION BIEN SUR)

//TODO : TROUVER QUEL CLASSE DE BLUETOOTH ET MODIFIER L'ICONE EN FONCTION (DEVICE ADAPTER)

//TODO : CREER SPLASHSCREEN VIDE POUR LES AUTORISATIONS LORS DE LA PREMIERE UTILISATION (SURTOUT COARSE_LOCATION)

public class BluetoothFragment extends Fragment {

    private bluetoothFragmentCallback mListener;

    private List<BluetoothDevice> mFoundDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mPairedDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mConnectedDeviceList = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mConnectedDevice, mBluetoothDevice;
    private boolean mScanning;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private String mDeviceAddress;
    private RelativeLayout mRelativeLayout;


    private DeviceAdapter mPairedDeviceAdapter, mFoundDeviceAdapter, mConnectedDeviceAdapter;
    private ProgressBar mPairedDeviceProgressBar, mFoundDeviceProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int actionBarHeight;

    private TextView state, noDeviceFound;


    public BluetoothFragment() {
        // Constructeur
    }

    public static BluetoothFragment newInstance(BluetoothDevice bluetoothDevice) {
        BluetoothFragment bluetoothFragment = new BluetoothFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", bluetoothDevice);
        bluetoothFragment.setArguments(args);
        return bluetoothFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mConnectedDevice = getArguments().getParcelable("device");
            if(mConnectedDevice!=null){
                mConnectedDeviceList.add(mConnectedDevice);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        getActivity().setTitle("Bluetooth");
        mHandler = new Handler();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        noDeviceFound = (TextView) view.findViewById(R.id.not_found_device_textview);
        mFoundDeviceProgressBar = (ProgressBar) view.findViewById(R.id.found_device_progress_bar);
        noDeviceFound.setVisibility(TextView.GONE);

        if(mConnectedDevice==null){
            mRelativeLayout = (RelativeLayout) view.findViewById(R.id.connected_device_layout);
            mRelativeLayout.setVisibility(TextView.GONE);
        }

        //INITIALISATION DU RECYCLER VIEW CONTENANT LES APPAREILS CONNUS
        RecyclerView mConnectedDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.connected_device_recycler_view);
        mConnectedDeviceAdapter = new DeviceAdapter(mConnectedDeviceList);
        setConnectedRecyclerView(mConnectedDeviceRecyclerView, mConnectedDeviceAdapter, mConnectedDeviceList);

        //INITIALISATION DU RECYCLER VIEW CONTENANT LES APPAREILS CONNUS
        RecyclerView mPairedDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.paired_device_recycler_view);
        mPairedDeviceAdapter = new DeviceAdapter(mPairedDeviceList);
        setRecyclerView(mPairedDeviceRecyclerView, mPairedDeviceAdapter, mPairedDeviceList);

        //INITIALISATION DU RECYCLER VIEW CONTENANT LES APPAREILS DETECTES
        RecyclerView mFoundDeviceRecylcerView = (RecyclerView) view.findViewById(R.id.found_device_recycler_view);
        mFoundDeviceAdapter = new DeviceAdapter(mFoundDeviceList);
        setRecyclerView(mFoundDeviceRecylcerView, mPairedDeviceAdapter, mFoundDeviceList);


        //PULL TO REFRESH
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!mScanning){ //Seulement si le scan n'est pas en cours
                    scanLeDevice(false);
                    mPairedDeviceList.clear();
                    mPairedDeviceAdapter.notifyDataSetChanged();
                    mFoundDeviceList.clear();
                    mFoundDeviceAdapter.notifyDataSetChanged();
                    getPairedDevice(view);
                    scanLeDevice(true);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        getPairedDevice(view);
        scanLeDevice(true);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof bluetoothFragmentCallback) {
            mListener = (bluetoothFragmentCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    //ON ARRETE LE SCAN
    @Override
    public void onDetach() {
        super.onDetach();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mListener = null;
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    //PERMET DE VERIFIER LES AUTORISATIONS BLUETOOTH
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //FONCTION QUI INITIALISE LES RECYCLERVIEW
    //On passe en argument le recyclerview en question, l'adapteur (voir classe DeviceAdapter) et la liste contenant des objets BluetoothDevice
    //La fonction met également en forme le recyclerview (traits séparateurs, animation...)
    private void setRecyclerView(RecyclerView recyclerView, DeviceAdapter deviceAdapter, final List<BluetoothDevice> deviceList){
        deviceAdapter = new DeviceAdapter(deviceList);
        RecyclerView.LayoutManager mFoundDeviceLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mFoundDeviceLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(deviceAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(mDeviceAddress!=null){
                    state.setText(mDeviceAddress);
                }
                //On envoie l'objet à l'activité
                mBluetoothDevice = deviceList.get(position);
                if (mListener != null) {
                    mListener.onItemSelected(mBluetoothDevice);
                }
                mDeviceAddress=mBluetoothDevice.getAddress();
                state=view.findViewById(R.id.macAddress);
                state.setText("Connexion en cours...");
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    private void setConnectedRecyclerView(RecyclerView recyclerView, DeviceAdapter deviceAdapter, final List<BluetoothDevice> deviceList){
        deviceAdapter = new DeviceAdapter(deviceList);
        RecyclerView.LayoutManager mFoundDeviceLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mFoundDeviceLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(deviceAdapter);
    }

    //FONCTION QUI RECUPERE LES APPAREILS BLUETOOTH ENREGISTRES DANS LE TELEPHONE ET MODIFIE L'UI SELON LE RESULTAT
    //Si on trouve un appareil, il est ajouté dans le recyclerview
    //Si on ne trouve pas d'appareil, on efface l'UI concernant les appareils associés
    //Si on rafraîchi le fragment, et qu'entre temps un appareil est supprimé/ajouté, l'UI s'adapte
    private void getPairedDevice (View view){
        mPairedDeviceProgressBar = (ProgressBar) view.findViewById(R.id.paired_device_progressbar);
        RelativeLayout mRelativeLayout = (RelativeLayout) view.findViewById(R.id.paired_device_layout);
        mRelativeLayout.setVisibility(TextView.VISIBLE);
        mPairedDeviceProgressBar.setVisibility(TextView.VISIBLE);
        mPairedDeviceList.clear();
        mPairedDeviceAdapter.notifyDataSetChanged();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mPairedDeviceList.addAll(pairedDevices);
            mPairedDeviceAdapter.notifyItemInserted(mPairedDeviceList.size());
            mPairedDeviceProgressBar.setVisibility(TextView.GONE);
        } else {
            mRelativeLayout.setVisibility(TextView.GONE);
        }
    }

    //FONCTION QUI SCAN LES APPAREILS BLUETOOTH LE A PROXIMITE
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFoundDeviceProgressBar.setVisibility(TextView.GONE);
                    mScanning=false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if(mFoundDeviceList.size()<1){
                        noDeviceFound.setVisibility(TextView.VISIBLE);
                    }
                    //Affichage de "Tirez pour rafraîchir" à la fin du scan et sous l'actionbar
                    if (getActivity()!=null) {
                        TypedValue tv = new TypedValue();
                        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                            Toast toast = Toast.makeText(getActivity(), "Tirez pour rafraîchir", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, actionBarHeight + 5);
                            toast.show();
                        }
                    }
                }
            }, SCAN_PERIOD);
            mFoundDeviceProgressBar.setVisibility(TextView.VISIBLE);
            noDeviceFound.setVisibility(TextView.GONE);
            mScanning=true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning=false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    //FONCTION QUI REMPLI LA LISTE D'OBJETS BLUETOOTHDEVICE AU RESULTAT DE LA FONCTION SCANLEDEVICE
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFoundDeviceList.contains(device)) {
                            mFoundDeviceList.add(device);
                            mFoundDeviceAdapter.notifyItemInserted(mFoundDeviceList.size());
                        }
                    }
                });

            }
        }
    };

    //L'INTERFACE QUI PERMET DE COMMUNIQUER AVEC L'ACTIVITE QUI CONTIENT CE FRAGMENT
    public interface bluetoothFragmentCallback {
        public void onItemSelected(BluetoothDevice bluetoothDevice);
    }

    //ON ATTEND DE SAVOIR SI ON S'EST CONNECTE OU DECONNECTE
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mFoundDeviceAdapter.notifyDataSetChanged();
                mRelativeLayout.setVisibility(TextView.VISIBLE);
                mConnectedDeviceList.clear();
                mConnectedDeviceList.add(mBluetoothDevice);
                mConnectedDeviceAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Connecté à "+ mDeviceAddress, Toast.LENGTH_LONG);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mRelativeLayout.setVisibility(TextView.GONE);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}

