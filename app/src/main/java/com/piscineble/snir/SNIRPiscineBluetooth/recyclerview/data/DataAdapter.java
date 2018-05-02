package com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.piscineble.snir.SNIRPiscineBluetooth.R;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

    private List<Data> mDataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView data, value, status;

        public MyViewHolder(View view) {
            super(view);
            data = (TextView) view.findViewById(R.id.phone_text);
            value = (TextView) view.findViewById(R.id.phone_number);
            status = (TextView) view.findViewById(R.id.status);
        }
    }


    public DataAdapter(List<Data> dataList) {
        this.mDataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Data data = mDataList.get(position);
        holder.data.setText(data.getType());
        holder.value.setText(String.valueOf(data.getValue()));
        holder.status.setText(data.getStatus());

        //TODO : CHANGER SELON LETAT (SE RENSEIGNER SUR LES VALEURS)
        if(data.getStatus()=="Bon")holder.status.setTextColor(Color.parseColor("#009933"));
        if(data.getStatus()=="Moyen")holder.status.setTextColor(Color.parseColor("#ff9933"));
        if(data.getStatus()=="Mauvais")holder.status.setTextColor(Color.parseColor("#cc0000"));

    }

    @Override
    public int getItemCount() {
        if (mDataList == null)
            return 0;
        else
            return  mDataList.size();
    }
}