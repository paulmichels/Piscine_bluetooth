package com.piscineble.snir.SNIRPiscineBluetooth.fragments;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.piscineble.snir.SNIRPiscineBluetooth.R;
import com.piscineble.snir.SNIRPiscineBluetooth.navigation.PagerAdapter;

public class DataNestedFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    public static DataNestedFragment newInstance(BluetoothDevice bluetoothDevice) {
        DataNestedFragment dataNestedFragment = new DataNestedFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", bluetoothDevice);
        dataNestedFragment.setArguments(args);
        return dataNestedFragment;
    }

    public DataNestedFragment() {
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
        View view= inflater.inflate(R.layout.fragment_data_nested, container, false);
        getActivity().setTitle("Ma piscine");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Général"));
        tabLayout.addTab(tabLayout.newTab().setText("Détails"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter (getFragmentManager(), tabLayout.getTabCount(), mBluetoothDevice);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }
}
