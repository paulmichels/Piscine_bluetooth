package com.piscineble.snir.SNIRPiscineBluetooth.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.piscineble.snir.SNIRPiscineBluetooth.R;


public class DataDetailsFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    public static DataDetailsFragment newInstance(BluetoothDevice bluetoothDevice) {
        DataDetailsFragment dataDetailsFragment = new DataDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", bluetoothDevice);
        dataDetailsFragment.setArguments(args);
        return dataDetailsFragment;
    }

    public DataDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBluetoothDevice = getArguments().getParcelable("device");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_details, container, false);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        return view;
    }
}
