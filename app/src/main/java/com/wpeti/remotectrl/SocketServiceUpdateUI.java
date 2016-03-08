package com.wpeti.remotectrl;

import android.widget.TextView;

/**
 * Created by wpeti on 2016.03.08..
 */
public class SocketServiceUpdateUI implements Runnable {
    public TextView mTextView;

    String updateString;

    public SocketServiceUpdateUI(String updateString, TextView txtview) {
        mTextView = txtview;
        this.updateString = updateString;
    }

    public void run() {
        if (updateString != null && !updateString.isEmpty()) {
            mTextView.setText(mTextView.getText() + updateString);
        }
    }
}
