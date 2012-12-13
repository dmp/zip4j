
package net.itsuha.android.zip4j;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PasswordDialog extends DialogFragment {

    @SuppressWarnings("unused")
    private static final String TAG = PasswordDialog.class.getSimpleName();

    public static final String ARGUMENT_ZIPENTRY = "zipentry";
    private static final String KEY_PASSWORD = "password";
    private PasswordDialogCallback mListener;
    private ZipEntry mZipEntry;
    private EditText mPasswordEdit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PasswordDialogCallback) {
            mListener = (PasswordDialogCallback) activity;
        } else {
            Fragment f = getTargetFragment();
            if (f instanceof PasswordDialogCallback) {
                mListener = (PasswordDialogCallback) f;
            } else {
                throw new ClassCastException();
            }
        }
        mZipEntry = (ZipEntry) getArguments().get(ARGUMENT_ZIPENTRY);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.zip_password_fragment, null, false);
        EditText passwordEdit = (EditText) view.findViewById(R.id.zip_password_edittext);
        mPasswordEdit = passwordEdit;
        passwordEdit.requestFocus();
        passwordEdit.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    mListener.onOkButtonPressed(mZipEntry, mPasswordEdit.getText().toString());
                    PasswordDialog.this.dismiss();
                    return true;
                }
                return false;
            }
        });

        if (savedInstanceState != null) {
            String password = savedInstanceState.getString(KEY_PASSWORD);
            if (!TextUtils.isEmpty(password)) {
                mPasswordEdit.setText(password);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Encrypted zip file");
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onOkButtonPressed(mZipEntry, mPasswordEdit.getText().toString());
            }
        });
        builder.setView(view);
        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onCancel();
    }

    interface PasswordDialogCallback {
        void onOkButtonPressed(ZipEntry entry, String password);
    
        void onCancel();
    }

}
