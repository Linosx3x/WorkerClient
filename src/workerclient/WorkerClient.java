package workerclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

// this class is responsible for the connection and the communication with the server
// everything else is being done by the TrueWorker class
public class WorkerClient {

    private static int id;
    private static TrueWorker worker;
    private static Socket socket;
    private static OutputStream os = null;
    private static InputStream is = null;
    private static BufferedReader in = null;
    private static PrintWriter out = null;

    public static void main(String[] args) {
        handshake();
        if (socket != null && in != null && out != null) {
            communicate();
        }
    }

    private static void handshake() {
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
        } catch (IOException ex) {
            ex.printStackTrace();
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

    private static void communicate() {
        while (true) {
            try {
                // do sth
                in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
