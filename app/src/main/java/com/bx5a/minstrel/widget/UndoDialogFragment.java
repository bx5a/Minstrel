package com.bx5a.minstrel.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * Created by guillaume on 22/04/2016.
 */
public class UndoDialogFragment extends DialogFragment {
    private String text;
    private DialogInterface.OnClickListener onClickListener;

    public UndoDialogFragment() {
        text = "";
        onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        };
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(text)
               .setPositiveButton("Undo", onClickListener);
        return builder.create();
    }
}
