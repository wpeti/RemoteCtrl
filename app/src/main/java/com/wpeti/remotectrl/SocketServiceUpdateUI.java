package com.wpeti.remotectrl;

import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;

/**
 * Created by wpeti on 2016.03.08..
 */
public class SocketServiceUpdateUI implements Runnable {
    public TextView mTextView;
    public UsbSerialDevice mSerialPort;
    String updateString;

    public SocketServiceUpdateUI(String updateString, TextView textView, UsbSerialDevice serialPort) {
        mTextView = textView;
        mSerialPort = serialPort;
        this.updateString = updateString;
    }

    public void run() {
        if (updateString != null && !updateString.isEmpty()) {
            mTextView.setText(/*mTextView.getText() + */updateString);
        }
        if (mSerialPort != null) {
            mSerialPort.write(updateString.getBytes());
        }
    }
}
