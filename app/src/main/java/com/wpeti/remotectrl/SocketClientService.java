package com.wpeti.remotectrl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by wpeti on 2016.03.14..
 */
public class SocketClientService extends Service {
    private ArrayList<Thread> clientThread = new ArrayList<>();
    private ArrayMap<String, Socket> openClientSockets = new ArrayMap<>();
    private int serverPort = 11111;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String serverIpMsgPairStr = intent.getStringExtra("serverIpAndMsg");
            String serverIp = serverIpMsgPairStr.substring(0, serverIpMsgPairStr.indexOf("="));

            if (!openClientSockets.containsKey(serverIp)) {
                Thread tmpThread = new Thread(new ClientThread(serverIp));
                clientThread.add(tmpThread);
                tmpThread.start();
            }

            String msg = serverIpMsgPairStr.substring(serverIpMsgPairStr.indexOf("=") + 1);
            SendMessage(openClientSockets.get(serverIp), msg);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void SendMessage(Socket soc, String str) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(soc.getOutputStream())),
                    true);
            out.println(str);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {
        String serverIp = "";

        ClientThread(String svrIp) {
            serverIp = svrIp;
        }

        @Override
        public void run() {
            try {
                openClientSockets.put(serverIp,
                        new Socket(InetAddress.getByName(serverIp), serverPort));
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            for (Socket aSocket : openClientSockets.values()) {
                aSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
