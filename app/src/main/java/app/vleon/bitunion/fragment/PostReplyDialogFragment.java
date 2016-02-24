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
import android.widget.TextView;
import android.widget.Toast;

import app.vleon.bitunion.MyApplication;
import app.vleon.bitunion.R;

public class PostReplyDialogFragment extends DialogFragment {
    MyApplication app;
    EditText mSubjectEditText = null;
    EditText mMessageEditText = null;
    EditText mAtUserEditText = null;
    TextView mQuoteInfoTextView = null;
    private String mQuotesMessage = "";
    private String mAtUser = null;
    private String mQuoteUser = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        app = (MyApplication) getActivity().getApplicationContext();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_reply_dialog, null);
        mMessageEditText = (EditText) view.findViewById(R.id.editText_postThreadMessage);
        mAtUserEditText = (EditText) view.findViewById(R.id.editTextAtUser);
        mQuoteInfoTextView = (TextView) view.findViewById(R.id.textViewQuoteInfo);
        mMessageEditText.setFocusable(true);
        mMessageEditText.requestFocus();
        if (mAtUser != null) {
            mAtUserEditText.setText(String.format("%s,", mAtUser));
            mAtUserEditText.setSelection(mAtUser.length() + 1); //设置光标位置
        }
        if (mQuoteUser != null) {
            mQuoteInfoTextView.setVisibility(View.VISIBLE);
            mQuoteInfoTextView.setText(String.format("已引用<%s>的帖子", mQuoteUser));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        attemptPostReply();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    public void attemptPostReply() {
        mMessageEditText.setError(null);
        String atUserMessage = getAtUserMessage();
        String bodyMessage = mMessageEditText.getText().toString();
        String message = mQuotesMessage + atUserMessage + "\n" + bodyMessage;
        if (TextUtils.isEmpty(message)) {
            mMessageEditText.requestFocus();
            mMessageEditText.setError("请输入内容");
        } else {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//            try {
//                app.getAPI().postReply(message, 0);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public String[] getAtUsers() {
        CharSequence s = mAtUserEditText.getText();
        if (s != null) {
            String[] users = s.toString().split(",");
            for (int i = 0; i < users.length; i++) {
                users[i] = users[i].trim();
            }
            return users;
        }
        return null;
    }

    public void setAtUsers(String user) {
        mAtUser = user;
    }

    private String getAtUserMessage() {
        String[] users = getAtUsers();
        String message = "";
        if (users != null) {
            for (String user : users) {
                if (!user.equals(""))
                    message += String.format("[@]%s[/@] ", user);
            }
        }
        return message;
    }

    public void setQuoteMessage(String quotes) {
        mQuotesMessage = quotes;
    }

    public void setQuoteUser(String user) {
        mQuoteUser = user;
    }
}
