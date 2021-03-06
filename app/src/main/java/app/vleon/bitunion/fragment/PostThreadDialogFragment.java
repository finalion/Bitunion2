package app.vleon.bitunion.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import app.vleon.bitunion.MyApplication;
import app.vleon.bitunion.R;

public class PostThreadDialogFragment extends DialogFragment {
    MyApplication app;
    EditText mSubjectEditText = null;
    EditText mMessageEditText = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        app = (MyApplication) getActivity().getApplicationContext();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_thread_dialog, null);
        mSubjectEditText = (EditText) view.findViewById(R.id.editText_postThreadTitle);
        mMessageEditText = (EditText) view.findViewById(R.id.editText_postThreadMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        attemptPostThread();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    public void attemptPostThread() {
        mSubjectEditText.setError(null);
        mMessageEditText.setError(null);
        String subject = mSubjectEditText.getText().toString();
        String message = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(subject)) {
            mSubjectEditText.requestFocus();
            mSubjectEditText.setError("请输入标题");
        } else if (TextUtils.isEmpty(message)) {
            mMessageEditText.requestFocus();
            mMessageEditText.setError("请输入内容");
        } else {
            try {
                app.getAPI().postNewThread(subject, message, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
