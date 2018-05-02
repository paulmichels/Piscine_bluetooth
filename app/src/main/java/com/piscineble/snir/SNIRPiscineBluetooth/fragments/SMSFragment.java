package com.piscineble.snir.SNIRPiscineBluetooth.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.gsm.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.piscineble.snir.SNIRPiscineBluetooth.R;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data.Data;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data.DataAdapter;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.phone.Phone;
import com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.phone.PhoneAdapter;

import java.util.ArrayList;
import java.util.List;

public class SMSFragment extends Fragment {

    private double pH, redox, temperature, bilan;
    private EditText message, numero;
    private List<Phone> mPhoneList = new ArrayList<>();
    private PhoneAdapter mAdapter;
    private RecyclerView recyclerView;
    public final static String DATA_NOTIFICATION =
            "com.example.bluetooth.le.DATA_NOTIFICATION";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_sms, container, false);
        getActivity().setTitle("Alerter pisciniste");

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.phone_recycler_view);
        mAdapter = new PhoneAdapter(mPhoneList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mPhoneList.clear();
        Phone phone;
        if(sharedPreferences.getString("key_phone_number", null)!=null){
            phone = new Phone("NUMERO DE TELEPHONE :", sharedPreferences.getString("key_phone_number", null));
        }else{
            phone = new Phone("NUMERO DE TELEPHONE :", "Aucun numéro défini dans paramètres");
        }
        mPhoneList.add(phone);
        mAdapter.notifyDataSetChanged();

        message = (EditText) view.findViewById(R.id.message);
        updateText(message);

        Button button = (Button)view.findViewById(R.id.envoyer);
        button.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                String num = sharedPreferences.getString("key_phone_number", null);
                String msg = message.getText().toString();
                //Si le numéro est supérieur à 4 caractères et que le message n'est pas vide on lance la procédure d'envoi
                if(num.length()== 10 && msg.length() > 0){
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(num, null, msg, null, null);
                    Toast.makeText(getActivity(), "Message envoyé!", Toast.LENGTH_SHORT).show();
                } else if(num.length()<10){
                    Toast.makeText(getActivity(), "Erreur : Entez un numero de téléphone valide", Toast.LENGTH_SHORT).show();
                } else if (msg.length()==0){
                    Toast.makeText(getActivity(), "Erreur : Entez un message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().registerReceiver(mDataUpdateReceiver, new IntentFilter(DATA_NOTIFICATION));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(mDataUpdateReceiver);
    }

    private final BroadcastReceiver mDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pH=Double.parseDouble(intent.getStringExtra("ph"));
            temperature=Double.parseDouble(intent.getStringExtra("temperature"));
            redox=Double.parseDouble(intent.getStringExtra("redox"));
            updateText(message);
        }
    };

    private void updateText(EditText editText){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if((sharedPreferences.getString("key_name", null))==null){
            editText.setHint("Remplissez vos coordonnées dans Paramètres pour la génération d'un message automatique");
        } else if((sharedPreferences.getString("key_surname", null)==null)){
            return;
        }else if((sharedPreferences.getString("key_address", null)==null)){
            return;
        }else if((sharedPreferences.getString("key_postal_code", null)==null)){
            return;
        }else if((sharedPreferences.getString("key_city", null)==null)){
            return;
        } else {
            editText.setText("La piscine de "
                    + sharedPreferences.getString("key_name", null) + " "
                    + sharedPreferences.getString("key_surname", null) + " située au :\n"
                    + sharedPreferences.getString("key_address", null) + "\n"
                    + sharedPreferences.getString("key_postal_code", null) + " "
                    + sharedPreferences.getString("key_city", null) + "\n A un problème.\n\n"
                    + "Résumé :\n"
                    + "pH = " + pH +"\n"
                    + "Redox = " + redox + "\n"
                    + "Bilan : " + bilan);
        }
    }
}
