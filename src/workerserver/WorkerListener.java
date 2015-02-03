package workerserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class WorkerListener extends Thread {

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
