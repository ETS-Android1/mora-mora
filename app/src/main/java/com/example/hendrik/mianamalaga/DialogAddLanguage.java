package com.example.hendrik.mianamalaga;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DialogAddLanguage extends AppCompatDialogFragment {

    TextView mLanguageTextView;
    RecyclerView mLanguageRecyclerView;
    AdapterLanguageChoice mLanguageAdapter;
    ArrayList<Locale> mKnownLocalesArrayList;
    Locale mLanguageLocale;


    public static DialogAddLanguage newInstance(){
        DialogAddLanguage dialogAddLanguage = new DialogAddLanguage();
        return dialogAddLanguage;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_add_language, null);

        mLanguageTextView = baseView.findViewById(R.id.dialog_add_language_language_text_view);
        mLanguageRecyclerView = baseView.findViewById(R.id.dialog_add_language_language_recycler_view);

        mKnownLocalesArrayList = new ArrayList<>();
        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getLanguage(), locale);
        }

        for (Locale locale : localeMap.values()){
            mKnownLocalesArrayList.add( locale );
        }

        IOUtils.sortLocales( mKnownLocalesArrayList );

        mLanguageAdapter = new AdapterLanguageChoice( mKnownLocalesArrayList, R.layout.list_element_language_dialog );
        mLanguageAdapter.setOnItemClickListener((position, viewHolder) -> {
            mLanguageLocale =  mKnownLocalesArrayList.get( position );
            mLanguageTextView.setText( "Add language:      " + mLanguageLocale.getDisplayName() );
        });
        mLanguageRecyclerView.setAdapter(mLanguageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager( this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLanguageRecyclerView.setLayoutManager(layoutManager);

        mLanguageAdapter.notifyDataSetChanged();
        builder.setView(baseView);
        builder.setCancelable(true);

        builder.setPositiveButton(R.string.Add, (dialog, which) -> {
            if( mLanguageLocale != null){
                ((ActivityLanguageChoice)getActivity()).onDialogAddLanguagePosClick( mLanguageLocale );
            } else {
                mLanguageTextView.setText("You need to choose a language!");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            ;
        });

        return builder.create();
    }
}
