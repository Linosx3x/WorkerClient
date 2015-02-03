package workerserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    // MAX number of workers
    private static final int MAX_WORKERS = 3;
    private static String request;
    private static ServerSocket serverSocket;
    private boolean serverOn = true;
    private static WorkerListener work[];

    public static void main(String[] args) {
        // initialize work[] table
        work = new WorkerListener[MAX_WORKERS];
        for (int i = 0; i < MAX_WORKERS; i++) {
            work[i] = null;
        }
        new WorkerServer();
    }

    public WorkerServer() {
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ioe) {
            System.out.println("Could not create server socket.");
            System.exit(-1);
        }
        // Successfully created Server Socket. Now wait for connections.
        while (serverOn) {
            try {
                // Accept incoming connections.
                Socket clientSocket = serverSocket.accept();
                WorkerListener cliThread = new WorkerListener(clientSocket);
                for (int i = 0; i < MAX_WORKERS; i++) {
                    if (work[i] == null) {
                        work[i] = cliThread;
                        cliThread.start();
                        break;
                    }
                }
                if (cliThread.isAlive()) {
                    System.out.println("Started new thread.");
                } else {
                    System.out.println("Couldn't start new thread. There are already " + MAX_WORKERS + " workers!");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        try {
            serverSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Problem stopping server socket.");
            System.exit(-1);
        }
    }
}
