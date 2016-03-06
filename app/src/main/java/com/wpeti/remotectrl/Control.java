package com.wpeti.remotectrl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Control extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbDeviceConnection connection;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;

    EditText cxEditText;
    EditText cyEditText;
    TextView textView;
    Button connectBtn;

    TouchFrameLayout touchAreaLayout;

    SocketClient clientObj;
    SocketServer serverObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        cxEditText = (EditText) findViewById(R.id.editText3);
        cyEditText = (EditText) findViewById(R.id.editText4);
        textView = (TextView) findViewById(R.id.textView);
        connectBtn = (Button) findViewById(R.id.Connect);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String serverIP = prefs.getString(SettingsActivity.PREF_REMOTE_IP, "");
        if (prefs.getBoolean(SettingsActivity.PREF_ACT_AS_CLIENT, false)) {
            clientObj = new SocketClient() {{
                Server_ip = serverIP;
            }};
        } else {
            serverObj = new SocketServer(textView);
        }

        touchAreaLayout = (TouchFrameLayout) findViewById(R.id.touchArea);

        ViewTreeObserver viewTreeObserver = touchAreaLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    touchAreaLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    UpdateLabels((int) (touchAreaLayout.getWidth() / 2), (int) (touchAreaLayout.getHeight() / 2));
                }
            });
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ctrl_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_ACT_AS_CLIENT, false)) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Rect rect = new Rect();
            touchAreaLayout.getGlobalVisibleRect(rect);

            int touchLeft = (int) rect.left;
            int touchTop = (int) rect.top;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    UpdateLabels(x - touchLeft, y - touchTop);
                    break;
                case MotionEvent.ACTION_MOVE:
                    UpdateLabels(x - touchLeft, y - touchTop);
                    break;
                case MotionEvent.ACTION_UP:
                    UpdateLabels((int) (rect.width() / 2), (int) (rect.height() / 2));
                    break;
            }
        }
        return false;
    }


    private void UpdateLabels(int x, int y) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > touchAreaLayout.getWidth()) x = touchAreaLayout.getWidth();
        if (y > touchAreaLayout.getWidth()) y = touchAreaLayout.getHeight();

        SetCoordinates(x, y);
        SetCalculatedValues(x, y, 255);
    }

    private void SetCalculatedValues(int x, int y, int scale) {
        int doubleScale = 2 * scale;
        int calculatedX = (((int) (doubleScale * x / touchAreaLayout.getWidth())) - scale);
        int calculatedY = ((scale - (int) (doubleScale * y / touchAreaLayout.getHeight())));

        cxEditText.setText("CX: " + calculatedX);
        cyEditText.setText("CY: " + calculatedY);

        if (serialPort != null) {
            serialPort.write(String.format("[%d,%d]", calculatedX, calculatedY).getBytes());
        }
        if (clientObj != null
                && PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_ACT_AS_CLIENT, false)) {
            clientObj.SendMessage(String.format("[%d,%d]", calculatedX, calculatedY));
        }
    }

    private void SetCoordinates(int x, int y) {
//        xEditText.setText("X: " + x);
//        yEditText.setText("Y: " + y);

        touchAreaLayout.setCircleCoordinates(new Point(x, y));
    }

    public void onConnectClick(View view) {
        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                tvAppend(textView, String.valueOf(deviceVID));
                if (deviceVID == 0x2A03)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                    tvAppend(textView, "Found arduino device.");
                } else {
                    tvAppend(textView, "There weren't any arduino devices detected..");
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    //Broadcast Receiver to automatically start and stop the Serial connection.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                if (granted) {
                    connection = usbManager.openDevice(device);

                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);

                    if (serialPort != null) {
                        try {
                            if (serialPort.open()) { //Set Serial Connection Parameters.
                                setUiEnabled(true); //Enable Buttons in UI
                                serialPort.setBaudRate(9600);
                                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                serialPort.read(mCallback); //
                                tvAppend(textView, "Serial Connection Opened!\n");

                            } else {
                                Log.d("SERIAL", "PORT NOT OPEN");
                            }
                        } catch (Exception ex) {
                            Log.e("SERIAL", "wpeti: open port" + ex.toString());
                        }

                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onConnectClick(connectBtn);
            }
        }

        ;
    };

    //Defining a Callback which triggers whenever data is read.
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                tvAppend(textView, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        ftv.append(ftext);
                    }
                });
    }

    public void setUiEnabled(boolean bool) {
        connectBtn.setEnabled(!bool);
        textView.setEnabled(bool);
    }

    public void onClrClick(View view) {
        textView.setText("");
    }
}
