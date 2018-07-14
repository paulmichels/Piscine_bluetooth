package com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.phone;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.piscineble.snir.SNIRPiscineBluetooth.R;

import java.util.List;

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.MyViewHolder> {

    private List<Phone> mPhoneList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView text, number;

        public MyViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.phone_text);
            number = (TextView) view.findViewById(R.id.phone_number);
        }
    }


    public PhoneAdapter(List<Phone> phoneList) {
        this.mPhoneList = phoneList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Phone phone = mPhoneList.get(position);
        holder.text.setText(phone.getText());
        holder.number.setText(String.valueOf(phone.getNumber()));
    }

    @Override
    public int getItemCount() {
        if (mPhoneList == null)
            return 0;
        else
            return mPhoneList.size();
    }
}
