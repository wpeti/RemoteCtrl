package com.wpeti.remotectrl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wpeti on 2016.03.06..
 */
public class SocketServer {

    private ServerSocket serverSocket;

    Handler updateConversationHandler;
    Thread serverThread = null;
    private TextView text;

    public static final int SERVERPORT = 1122;

    SocketServer(TextView OutputTextView) {
        text = OutputTextView;
        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    protected void CloseSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocket getServerSocket() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serverSocket;
    }

    class ServerThread implements Runnable {
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    CommunicationThread commThread = new CommunicationThread(getServerSocket().accept());
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
            text.setText(text.getText().toString() + "Client Says: " + msg + "\n");
        }
    }
}
