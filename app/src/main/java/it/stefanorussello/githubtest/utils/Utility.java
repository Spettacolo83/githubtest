package it.stefanorussello.githubtest.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ProgressBar;

import it.stefanorussello.githubtest.R;

public class Utility {

    public static String TAG = Utility.class.getName();
    private ProgressDialog progressDialog;

    public void showLoading(final Context context, final String message) {
        Log.d(TAG, "showLoading: " + message);
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(message);
                    ProgressBar lProgressBar = new android.widget.ProgressBar(context, null, android.R.attr.progressBarStyle);
                    progressDialog.setIndeterminateDrawable(lProgressBar.getIndeterminateDrawable());
                    if (!((Activity)context).isFinishing()) {
                        progressDialog.show();
                    }
                }
            }
        });
    }

    public void dismissLoading(final Context context) {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                if (progressDialog != null) {
                    if (progressDialog.isShowing() && !((Activity) context).isFinishing()) {
                        progressDialog.dismiss();
                    }
                }

                progressDialog = null;
            }
        });
    }

    public void showAlert(final Context context, final String title, final String message) {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if (!((Activity) context).isFinishing()) {
                    alertDialog.show();
                }
            }
        });
    }
}
