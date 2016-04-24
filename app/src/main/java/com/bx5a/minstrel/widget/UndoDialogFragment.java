package com.bx5a.minstrel.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 22/04/2016.
 */
public class UndoDialogFragment extends DialogFragment {
    private String text;
    private View.OnClickListener onClickListener;

    public UndoDialogFragment() {
        text = "";
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_undo);
        TextView textView = (TextView) dialog.findViewById(R.id.dialogUndo_text);
        textView.setText(text);
        Button button = (Button) dialog.findViewById(R.id.dialogUndo_button);
        button.setOnClickListener(onClickListener);

        // rounded corners
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // blur outside of dialog
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount=0.2f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        return dialog;
    }
}
