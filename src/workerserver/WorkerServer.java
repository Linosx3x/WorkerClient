package workerserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    // MAX number of workers
    private static final int MAX_WORKERS = 3;
    private static String request;
    private static ServerSocket serverSocket;
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
        // accept communication in port 1234
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ioe) {
            System.out.println("Could not create server socket.");
            System.exit(-1);
        }
        try {
            while (true) {
                // accept incoming connections
                Socket clientSocket = serverSocket.accept();
                // check if a new worker could be started
                WorkerListener cliThread = null;
                for (int i = 0; i < MAX_WORKERS; i++) {
                    if (work[i] == null) {
                        cliThread = new WorkerListener(clientSocket, i);
                        work[i] = cliThread;
                        cliThread.start();
                        break;
                    }
                }
                if (cliThread != null) {
                    System.out.println("Started new thread.");
                } else {
                    System.out.println("Couldn't start new thread. There are already " + MAX_WORKERS + " workers!");
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            serverSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Problem stopping server socket. Exiting...");
            System.exit(-1);
        }
    }
}
