package com.piscineble.snir.SNIRPiscineBluetooth.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.piscineble.snir.SNIRPiscineBluetooth.R;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data.Data;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data.DataAdapter;

import java.util.ArrayList;
import java.util.List;


public class DataFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    public final static String DATA_NOTIFICATION =
            "com.example.bluetooth.le.DATA_NOTIFICATION";

    private List<Data> mDataList = new ArrayList<>();
    private DataAdapter mAdapter;
    private RecyclerView recyclerView;

    private String mDeviceName;
    private String mDeviceAddress;

    private double pH, temperature, redox, bilan;

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothDevice mBluetoothDevice;

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance(BluetoothDevice bluetoothDevice) {
        DataFragment dataOverviewFragment = new DataFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", bluetoothDevice);
        dataOverviewFragment.setArguments(args);
        return dataOverviewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBluetoothDevice = getArguments().getParcelable("device");
            if(mBluetoothDevice!=null){
                mDeviceName=mBluetoothDevice.getName();
                mDeviceAddress=mBluetoothDevice.getAddress();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_data_overview, container, false);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        if(mBluetoothDevice!=null){
            TextView deviceNameText = (TextView) view.findViewById(R.id.device_name);
            TextView stateText = (TextView) view.findViewById(R.id.state);
            if(mDeviceName!=null){
                deviceNameText.setText(mDeviceName);
            } else {
                deviceNameText.setText(mDeviceAddress);
            }
            stateText.setText("Connecté");
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.data_recycler_view);

        mAdapter = new DataAdapter(mDataList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        updateRecyclerView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        getActivity().registerReceiver(mDataUpdateReceiver, new IntentFilter(DATA_NOTIFICATION));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(mDataUpdateReceiver);
    }

    private void updateRecyclerView(){
        mDataList.clear();

        Data data = new Data("Température", temperature, null);
        mDataList.add(data);

        data = new Data("pH", pH, null);
        mDataList.add(data);

        data = new Data("Potentiel d'Oxydo-Réduction", redox, null);
        mDataList.add(data);

        data = new Data("Bilan de votre piscine", bilan, null);
        mDataList.add(data);

        mAdapter.notifyDataSetChanged();
    }

    private final BroadcastReceiver mDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pH=Double.parseDouble(intent.getStringExtra("ph"));
            temperature=Double.parseDouble(intent.getStringExtra("temperature"));
            redox=Double.parseDouble(intent.getStringExtra("redox"));
            bilan=Double.parseDouble(intent.getStringExtra("bilan"));
            updateRecyclerView();
        }
    };
}
