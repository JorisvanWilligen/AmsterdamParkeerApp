package com.jorisvanwilligen.amsterdamparkeerapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class CustomAdapter extends ArrayAdapter<String>{

    public ArrayList<String> namenLijst = new ArrayList<String>();
    public ArrayList<String> adressenLijst = new ArrayList<String>();
    public ArrayList<String> afstandenLijst = new ArrayList<String>();

    public CustomAdapter(Context context, ArrayList<String> namen, ArrayList<String> adressen, ArrayList<String> afstanden) {
        super(context,R.layout.custom_row, namen);

        namenLijst = namen;                                                                                     //Gegevens van parkeerplaats doorspelen
        adressenLijst = adressen;
        afstandenLijst = afstanden;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {                                     //TextViews aanpassen op gegevens
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.custom_row, parent, false);

        String titelItem = namenLijst.get(position);
        TextView titelText = (TextView) customView.findViewById(R.id.titelText);
        titelText.setText(titelItem);


        String adressItem = adressenLijst.get(position);
        TextView adressText = (TextView) customView.findViewById(R.id.adressText);
        adressText.setText(adressItem);


        String afstandItem = afstandenLijst.get(position);
        TextView afstandText = (TextView) customView.findViewById(R.id.afstandText);
        afstandText.setText(afstandItem);


        return customView;
    }
}
