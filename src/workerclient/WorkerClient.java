package workerclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class WorkerClient {

    private static int id;
    private static TrueWorker worker;
    private static Socket socket;
    private static OutputStream os = null;
    private static InputStream is = null;
    private static BufferedReader in = null;
    private static PrintWriter out = null;

    public static void main(String[] args) {
        new WorkerClient();
    }

    public WorkerClient() {
        handshake();
        while (communicate()) {
            // do nothing
        }
        System.out.println("Terminating...");
    }

    // the first communication, create worker with id given from server
    private void handshake() {
        try {
            socket = new Socket("localhost", 1234);
            os = socket.getOutputStream();
            out = new PrintWriter(os);
            is = socket.getInputStream();
            in = new BufferedReader(new InputStreamReader(is));
            System.out.println("Initialized the streams.");
            // ask for connection and worker's initialization
            out.println("connect");
            out.flush();
            System.out.println("Sent 'connect' message.");
            // get worker's id
            id = in.read();
            System.out.println("Connected. Known as worker #" + id + ".");
            // initialize TrueWorker
            worker = new TrueWorker(id);
            // worker.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            cleanUp();
        }
    }

    // the entire communication is handled by this method
    private boolean communicate() {
        try {
            String message = in.readLine();
            if (message != null) {
                System.out.println(message);
                out.println("get ok");
                out.flush();
                return true;
            } else {
                cleanUp();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            cleanUp();
            return false;
        }
    }

    // close socket and streams
    private void cleanUp() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (is != null) {
                is.close();
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (os != null) {
                os.close();
            }
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
