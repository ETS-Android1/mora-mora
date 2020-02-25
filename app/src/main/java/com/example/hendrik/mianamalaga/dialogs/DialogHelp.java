package com.example.hendrik.mianamalaga.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.hendrik.mianamalaga.R;

public class DialogHelp extends AppCompatDialogFragment {

    String mHelpText;

    public static DialogHelp newInstance(String helpText){
        DialogHelp helpDialog = new DialogHelp();
        Bundle args = new Bundle();
        args.putString("helpText", helpText);
        helpDialog.setArguments( args );
        return  helpDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        if( getArguments() != null){
            mHelpText = getArguments().getString("helpText");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_help, null);

        TextView helpTextView = baseView.findViewById(R.id.dialog_help_text_view);

        if( helpTextView != null ){
            helpTextView.setText( mHelpText );
        }

        builder.setView(baseView);
        builder.setCancelable(true);
        builder.setPositiveButton("ok", (dialog, which) -> dismiss());

        return builder.create();

    }


}
