package com.bx5a.minstrel.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;

/**
 * Created by guillaume on 24/04/2016.
 */
public class PlayableDialogFragment extends DialogFragment {
    private Playable playableObject;
    private int playableIndex;
    private View.OnClickListener nowListener;
    private String nowText;
    private View.OnClickListener nextListener;
    private String nextText;
    private View.OnClickListener lastListener;
    private String lastText;
    private View.OnClickListener removeListener;
    private String removeText;
    private boolean hideRemove;

    public PlayableDialogFragment() {
        hideRemove = false;
    }

    public void initForPlaylist(Context context, Playable playable, int index) {
        playableObject = playable;
        playableIndex = index;
        nowListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().setCurrentPlayableIndex(playableIndex);
                MasterPlayer.getInstance().play();
                dismiss();
            }
        };
        nextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().reorder(playableIndex, Position.Next);
                dismiss();
            }
        };
        lastListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().reorder(playableIndex, Position.Last);
                dismiss();
            }
        };
        removeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().remove(playableIndex);
                dismiss();
            }
        };
        nowText = context.getResources().getString(R.string.playable_now);
        nextText = context.getResources().getString(R.string.playable_next);
        lastText = context.getResources().getString(R.string.playable_last);
        removeText = context.getResources().getString(R.string.playable_remove);
    }

    public void initForSearch(Context context, Playable playable, int index) {
        playableObject = playable;
        playableIndex = index;
        nowListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().enqueue(playableObject, Position.Next);
                MasterPlayer.getInstance().next();
                dismiss();
            }
        };
        nextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().enqueue(playableObject, Position.Next);
                dismiss();
            }
        };
        lastListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().enqueue(playableObject, Position.Last);
                dismiss();
            }
        };
        removeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        nowText = context.getResources().getString(R.string.enqueue_now);
        nextText = context.getResources().getString(R.string.enqueue_next);
        lastText = context.getResources().getString(R.string.enqueue_last);
        removeText = context.getResources().getString(R.string.playable_remove);
        hideRemove = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_playable);

        ImageAndTextButton now = (ImageAndTextButton)dialog.findViewById(R.id.dialogPlayable_now);
        now.setText(nowText);
        ImageAndTextButton next = (ImageAndTextButton)dialog.findViewById(R.id.dialogPlayable_next);
        next.setText(nextText);
        ImageAndTextButton last = (ImageAndTextButton)dialog.findViewById(R.id.dialogPlayable_last);
        last.setText(lastText);
        ImageAndTextButton remove = (ImageAndTextButton)dialog.findViewById(R.id.dialogPlayable_remove);
        remove.setText(removeText);

        if (hideRemove) {
            remove.setVisibility(View.INVISIBLE);
        }

        now.setOnClickListener(nowListener);
        next.setOnClickListener(nextListener);
        last.setOnClickListener(lastListener);
        remove.setOnClickListener(removeListener);

        return dialog;
    }
}
