package workerserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class WorkerListener extends Thread {

    private Socket socket;
    private int number;
    private String message = "";
    private String response = "";
    private InputStream is = null;
    private BufferedReader in = null;
    private OutputStream os = null;
    private PrintWriter out = null;
    private int pr = 0;
    private long startTime;

    public WorkerListener(Socket s, int num) {
        socket = s;
        number = num;
        startTime = System.currentTimeMillis() / 1000;
    }

    @Override
    public void run() {
        try {
            is = socket.getInputStream();
            in = new BufferedReader(new InputStreamReader(is));
            os = socket.getOutputStream();
            out = new PrintWriter(os);
            System.out.println("Initialized the streams.");
            String incoming = in.readLine();
            handshake(incoming);
            incoming = null;
            while (true) {
                if (!message.equals("")) {
                    // send message
                    out.println(message);
                    out.flush();
                    System.out.println("Message sent.");
                    // set message empty again
                    message = "";
                    incoming = in.readLine();
                    System.out.println("Message received: " + incoming);
                    // if worker is alive
                    if (incoming != null) {
                        System.out.println("Worker response: " + incoming);
                        while (!response.equals("")) {
                            // do nothing
                        }
                        response = incoming;
                    } else { // if it's not
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Worker is down for maintainance.");
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
            System.out.println("Worker #" + number + " terminated.");
        }
    }

    // send the number to the worker as the first act of communication
    private void handshake(String incoming) {
        if (incoming.equals("connect")) {
            out.write(number);
            out.flush();
            System.out.println("Connection established: worker #" + number + ".");
        }
    }

    // when a get/put action required
    public boolean setMessage(String msg) {
        if (message.equals("")) {
            message = msg;
            this.pr = 0; // priority reduced to minimum
            return true;
        }
        return false;
    }

    // when a response is needed
    public String getResponse() {
        String tmp = "";
        if (!(response.equals(""))) {
            tmp = response;
            response = "";
        }
        return tmp;
    }

    public int getPR() {
        return this.pr;
    }

    public void incPR() {
        this.pr += 1;
    }

    // get the difference between current time and started time
    public long getUptime() {
        return (System.currentTimeMillis() / 1000) - startTime;
    }
}
