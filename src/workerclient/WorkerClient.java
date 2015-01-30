package workerclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

// this class is responsible for the connection and the communication with the server
// everything else is being done by the TrueWorker class
public class WorkerClient {

    private static int id;
    private static TrueWorker worker;
    private static Socket socket;
    private static OutputStream os = null;
    private static ObjectOutputStream oos = null;
    private static InputStream is = null;
    private static ObjectInputStream ois = null;

    public static void main(String[] args) {
        handshake();
        //communicate();
    }

    private static void handshake() {
        try {
            socket = new Socket("localhost", 1234);
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            // ask for connection and worker's initialization
            oos.writeUTF("connect");
            // get worker's id
            id = ois.readInt();
            System.out.println("Connected. Known as worker #" + id + ".");
            // initialize TrueWorker
            worker = new TrueWorker(id);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void communicate() {
        while(true) {
            try {
                ois.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
