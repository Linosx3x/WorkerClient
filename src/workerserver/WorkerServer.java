package workerserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
        ObjectInputStream ois = null;
        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            while (true) {
                // read incoming stream
                String request = ois.readUTF();
                System.out.println(request);
                oos.writeInt(1);
                System.out.println("I sent it.");
                // what to do?
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // clean up
            try {
                if (ois != null) {
                    ois.close();
                }
                if (is != null) {
                    is.close();
                }
                if (oos != null) {
                    oos.close();
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
