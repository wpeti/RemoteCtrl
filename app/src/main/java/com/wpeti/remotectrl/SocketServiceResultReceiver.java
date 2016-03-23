package com.wpeti.remotectrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;

/**
 * Created by wpeti on 2016.03.08..
 */

public class SocketServiceResultReceiver extends ResultReceiver {
    TextView mTxtView;
    UsbSerialDevice mSerialPort;
    public SocketServiceResultReceiver(Handler handler, TextView textView, UsbSerialDevice serialPort) {
        super(handler);
        mTxtView = textView;
        mSerialPort = serialPort;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
         new SocketServiceUpdateUI(resultData.getString("socmsg"), mTxtView, mSerialPort).run();
    }
}

