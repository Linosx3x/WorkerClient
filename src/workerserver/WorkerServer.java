package workerserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    private static String request;
    private static ServerSocket serverSocket;
    private boolean serverOn = true;

    public static void main(String[] args) {
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
                cliThread.start();
                System.out.println("Started new thread.");
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

class WorkerListener extends Thread {

    private Socket socket;
    private boolean threadRunning = true;

    public WorkerListener(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        InputStream is = null;
        BufferedReader in = null;
        OutputStream os = null;
        PrintWriter out = null;        
        try {
            is = socket.getInputStream();
            in = new BufferedReader(new InputStreamReader(is));
            os = socket.getOutputStream();
            out = new PrintWriter(os);            
            System.out.println("Initialized the streams.");
            while (true) {
                // read incoming stream
                String request = in.readLine();
                if (request != null) {
                    System.out.println(request);
                    out.write(1);
                    out.flush();
                    System.out.println("Connection established: worker #1.");
                    // what to do?
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // clean up
            try {
                if (in != null) {
                    in.close();
                }
                if (is != null) {
                    is.close();
                }
                if (out != null) {
                    out.close();
                }
                if (os != null) {
                    os.close();
                }
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        super.run();
    }
}
