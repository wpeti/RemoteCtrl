package com.wpeti.remotectrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TextView;

/**
 * Created by wpeti on 2016.03.08..
 */

public class SocketServiceResultReceiver extends ResultReceiver {
    TextView mTxtView;
    public SocketServiceResultReceiver(Handler handler, TextView textView) {
        super(handler);
        mTxtView = textView;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
         new SocketServiceUpdateUI(resultData.getString("socmsg"), mTxtView).run();
    }
}

