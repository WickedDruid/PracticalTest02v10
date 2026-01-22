package ro.pub.cs.systems.eim.practicaltest02v10;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private static final String TAG = "ServerThread";

    private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public ServerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.d(TAG, "Server started in port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                Log.d(TAG, "Client connected on: " + clientSocket.getInetAddress());

                CommunicationThread communicationThread = new CommunicationThread(clientSocket);
                communicationThread.start();
            }
        } catch (IOException e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server socket: " + e.getMessage());
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}
