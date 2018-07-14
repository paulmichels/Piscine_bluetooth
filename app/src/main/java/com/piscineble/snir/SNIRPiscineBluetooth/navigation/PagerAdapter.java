package com.piscineble.snir.SNIRPiscineBluetooth.navigation;

import android.bluetooth.BluetoothDevice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.piscineble.snir.SNIRPiscineBluetooth.fragments.DataDetailsFragment;
import com.piscineble.snir.SNIRPiscineBluetooth.fragments.DataFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    private BluetoothDevice mBluetoothDevice;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, BluetoothDevice bluetoothDevice) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.mBluetoothDevice = bluetoothDevice;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                DataFragment dataFragment = DataFragment.newInstance(mBluetoothDevice);
                return dataFragment;
            case 1:
                DataDetailsFragment dataDetailsFragment = DataDetailsFragment.newInstance(mBluetoothDevice);
                return dataDetailsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}