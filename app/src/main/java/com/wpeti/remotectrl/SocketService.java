package com.wpeti.remotectrl;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wpeti on 2016.03.07..
 */
public class SocketService extends Service {
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    ResultReceiver resultReceiver;

    public static final int SERVERPORT = 11111;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = intent.getExtras();
        resultReceiver = intent.getParcelableExtra("receiver");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class ServerThread implements Runnable {
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private String msg;
        public updateUIThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            if (resultReceiver != null && !msg.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("socmsg", msg);
                resultReceiver.send(0, bundle);
            }
        }
    }

    public class MyResultReceiver extends ResultReceiver {
        TextView mTxtView;
        public MyResultReceiver(Handler handler, TextView textView) {
            super(handler);
            mTxtView = textView;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            new SocketServiceUpdateUI(resultData.getString("socmsg"), mTxtView).run();
        }
    }
}
