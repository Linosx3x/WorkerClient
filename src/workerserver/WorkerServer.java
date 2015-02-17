package workerserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

public class WorkerServer {

    // MAX number of workers
    private static final int MAX_WORKERS = 3;
    private static String request;
    private static ServerSocket serverSocket;
    private static WorkerListener work[];
    private String type;
    private boolean success;

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
            // start the http server
            type = null;
            success = false;
            this.start();
            while (true) {
                // accept incoming connections
                Socket clientSocket = serverSocket.accept();
                // check if a new worker could be started
                WorkerListener cliThread = null;
                for (int i = 0; i < MAX_WORKERS; i++) {
                    if (work[i] == null || !work[i].isAlive()) {
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

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/store", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private String parseQuery(String query) {
        String result = new String();
        if (query != null) {
            String[] pairs = query.split("&");
            //String[] param=pair.split("=");
            String key = null;
            String value = null;
            try {
                if (pairs.length > 1) {
                    String[] param = pairs[0].split("=");
                    key = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                    param = pairs[1].split("=");
                    if (param.length > 1) {
                        value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                        String response = put(key, value);
                        if (success) {
                            return response;
                        }
                    } else {
                        return "  <head>\n"
                                + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                                + "</head> You entered no value for the key";
                    }
                } else if (pairs.length > 0) {
                    String[] param = pairs[0].split("=");
                    key = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                    result = get(key);
                    if (success) {
                        return result;
                    }

                } else {
                    this.success = false;
                    return "You entered something wrong";
                }
            } catch (UnsupportedEncodingException e) {
                this.success = false;
                return "You used unsupported encoding";
            }
        }
        return result;
    }

    private String get(String key) {
        //String result = new String();
        //if (parameters.containsKey(key)) {
        if (true) {
            String response;
            boolean set;
            String request = "get " + key;
            this.success = true;
            this.type = "get";
            // while it is false, try again to set the message
            do {
                set = work[0].setMessage(request);
            } while (!set);
            System.out.println("Sent request: " + request);
            do {
                response = work[0].getResponse();
            } while (response.equals(""));
            System.out.println("Got response: " + response);
            return response;
            //return parameters.get(key);
        } else {
            return "Key not found";
        }
    }

    private String put(String key, String value) {
        /*String result = null;
         try {
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(keyValPairs, true)));
         out.print(key + "=" + value + "\n");
         out.close();
         parameters.put(key, value);*/
        this.type = "put";
        this.success = true;
        boolean set;
        String response;
        String request = "put " + key + " " + value;
        do {
            set = work[0].setMessage(request);
        } while (!set);
        System.out.println("Sent request: " + request);
        do {
            response = work[0].getResponse();
        } while (response == null);
        System.out.println("Got response: " + response);
        return response;
        /*} catch (IOException ex) {
         Logger.getLogger(RestServer.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }
    //test

    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String result = "";
            String response = "";
            String request = t.getRequestURI().getQuery();
            if (request != null) {
                result = parseQuery(request);
                if (success && type.equalsIgnoreCase("put")) {
                    response = "  <head>\n"
                            + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                            + "</head> " + "All went ok";
                } else if (success && type.equalsIgnoreCase("get")) {
                    response = "  <head>\n"
                            + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                            + "</head> " + result;
                } else if (!(success)) {
                    response = "  <head>\n"
                            + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                            + "</head> " + result;
                }
            } else {
                response = "<!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n"
                        + "<form action=\"\" method=\"get\">\n" + "Key:<br>\n"
                        + "<input type=\"text\" name=\"key\" value=\"\">\n" + "<br>\n"
                        + "Value:<br>\n" + "<input type=\"text\" name=\"value\" value=\"\">\n"
                        + "<br><br>\n" + "<input type=\"submit\" value=\"Submit\">\n" + "</form> \n"
                        + "<form action=\"\" method=\"get\">\n" + "Key:&nbsp;\n"
                        + "<input type=\"text\" name=\"key\" value=\"\"><br>"
                        + "<input type=\"submit\" value=\"Get me my value\">\n" + "</form>"
                        + "\n" + "</body>\n" + "</html>";
            }
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
