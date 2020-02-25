package com.example.hendrik.mianamalaga.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hendrik.mianamalaga.R;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterLanguageSpinner extends ArrayAdapter<Locale> {

    public AdapterLanguageSpinner(Activity context, int resourceID, int textViewID, ArrayList<Locale> localeArrayList) {
        super(context, resourceID, textViewID, localeArrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        return rowView(convertView, position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        return rowView( convertView, position );
    }

    private View rowView(View convertView, int position){
        Locale locale = getItem( position );

        viewHolder holder;
        View rowView = convertView;

        if( rowView == null ){

            holder = new viewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_element_language_spinner, null, false);
            holder.languageTextView = rowView.findViewById(R.id.list_element_language_spinner_text_view);
            rowView.setTag(holder);
        } else {
            holder = (viewHolder) rowView.getTag();
        }

        holder.languageTextView.setText( locale.getDisplayName() );

        return rowView;
    }

    private class viewHolder{
        TextView languageTextView;
    }
}
