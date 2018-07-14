package com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.piscineble.snir.SNIRPiscineBluetooth.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder> {

    private List<BluetoothDevice> deviceList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address;
        public ImageView icon;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.deviceName);
            address = (TextView) view.findViewById(R.id.macAddress);
            icon=(ImageView) itemView.findViewById(R.id.deviceIcon);
        }
    }

    public DeviceAdapter(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        if(device.getName()==null) {
            holder.name.setText("Appareil bluetooth");
        } else {
            holder.name.setText(device.getName());
        }
        holder.address.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        if (deviceList == null)
            return 0;
        else
            return  deviceList.size();
    }
}