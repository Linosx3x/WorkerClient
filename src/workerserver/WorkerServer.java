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
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class WorkerServer {

    // MAX number of workers
    private static final int MAX_WORKERS = 3;
    private static String request;
    private static ServerSocket serverSocket;
    private static WorkerListener work[];
    private String type;
    private boolean success;
    private ArrayList<KeyWorkersPair> list = new ArrayList<KeyWorkersPair>();
    private static int workersScheduling[];

    public static void main(String[] args) {
        // initialize work[] & workersScheduling[] table
        work = new WorkerListener[MAX_WORKERS];
        workersScheduling = new int[MAX_WORKERS];
        for (int i = 0; i < MAX_WORKERS; i++) {
            work[i] = null;
            workersScheduling[i] = -1;
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
                // accept incomaxg connections
                Socket clientSocket = serverSocket.accept();
                // check if a new worker could be started
                WorkerListener cliThread = null;
                for (int i = 0; i < MAX_WORKERS; i++) {
                    if (work[i] == null || !work[i].isAlive()) {
                        cliThread = new WorkerListener(clientSocket, i);
                        work[i] = cliThread;
                        workersScheduling[i] = 0;
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
                        /*if (response != null && !response.equals("WD")) {
                         return response;
                         } else if (response.equals("WD") || response.equals("AD")) {
                         return response;
                         }*/
                        if (response != null && !response.equals("")) {
                            return response;
                        } else {
                            return "Something wrong happened!";
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
            String response = null;
            boolean set;
            String request = "get " + key;
            this.success = true;
            this.type = "get";
            int[] workers = {-1, -1};
            String[] responses = new String[2];
            for (int i = 0; i < list.size(); i++) {
                if (key.equals(list.get(i).getKey())) {
                    workers[0] = list.get(i).getWorker1();
                    workers[1] = list.get(i).getWorker2();
                    break;
                }
            }
            // while it is false, try again to set the message
            for (int i = 0; i < workers.length; i++) {
                do {
                    set = work[workers[i]].setMessage(request);
                } while (!set);
                System.out.println("Sent request: " + request);
                do {
                    response = work[workers[i]].getResponse();
                    if (!work[workers[i]].isAlive()) {
                        response = "An error occured";
                    }
                } while (response.equals(""));
                System.out.println("Got response from " + workers[i] + " is: " + response + "\n");
                responses[i] = response;
            }
            if (!(responses[0].equals(responses[1]))) {
                if (responses[0].equals("An error occured")) {
                        return responses[1];
                } else {
                    if (responses[1].equals("An error occured")) {
                        return responses[0];
                    }
                }
                if (work[workers[0]].getUptime() > work[workers[1]].getUptime()) {
                    response = responses[0];
                    update(key, response, workers[1]);
                } else {
                    response = responses[1];
                    update(key, response, workers[0]);
                }
            }
            return response;
            //return parameters.get(key);
        } else {
            return "Key not found";
        }
    }

    private boolean update(String key, String response, int worker) {
        boolean set = false;
        String request = "put " + key + " " + response;
        do {
            set = work[worker].setMessage(request);
        } while (!set);
        System.out.println("Sent update request: " + request + " to worker number: " + worker);
        do {
            response = work[worker].getResponse();
            if (!work[worker].isAlive()) {
                response = "An error occurred";
                set = false;
            }
        } while (response.equals(""));
        return set;
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
        boolean set, set2;
        String response = "";
        int[] max = new int[2];
        String request = "put " + key + " " + value;
        // calls algorithm, returns 2 workers
        //do {
        max = schedulingAlgorithm();
        /*    counter++;
         } while ((max[0] == -1 || max[1] == -1) && counter <= 5);
         if (counter > 5) {
         return "AD";
         }*/
        if (max[0] == max[1]) {
            max[1] = -1;
        }
        int pos = -1;
        boolean keyExists = false;
        String[] responses = new String[2];
        for (int i = 0; i < 2; i++) {
            // it's not alive and you know it!
            if (max[i] == -1) {
                break;
            }
            do {
                set = work[max[i]].setMessage(request);
            } while (!set);
            System.out.println("Sent request: " + request + " to worker number: " + max[i]);
            do {
                response = work[max[i]].getResponse();
                if (!work[max[i]].isAlive()) {
                    response = "An error occurred";
                }
            } while (response.equals(""));
            if (!(response.equals("An error occurred"))) {
                if (pos < list.size() && pos >= 0) {
                    if (list.get(pos).getWorker2() == -1) {
                        list.get(pos).setWorker2(max[i]);
                    }
                } else {
                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j).getKey().equals(key)) {
                            //wor[0]=true; 
                            if ((list.get(j).getWorker1() >= MAX_WORKERS || list.get(j).getWorker1() < 0) && i == 0) {
                                list.get(j).setWorker1(max[i]);
                                pos = j;
                                keyExists = true;
                                break;
                            }
                        }
                    }
                    if (!keyExists) {
                        if (i == 0) {
                            list.add(new KeyWorkersPair(key, max[i], -1));
                            pos = list.size() - 1;
                        }
                        keyExists = true;
                    }
                }
            }
            responses[i] = response;
        }
        if (!(responses[0].equals(responses[1]))) {
            if (responses[0].equals("An error occured")) {
                    System.out.println("Got response: " + responses[1] + "\n");
                    return responses[1];
                } else {
                    System.out.println("Got response: " + responses[0] + "\n");
                    return responses[0];
            }
        }
        System.out.println("Got response: " + response + "\n");
        /*if (response == null) {
         return "WD";
         }*/
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
                if (result == null || result.equals("")) {//result.equals("AD") || result.equals("WD")) {
                    success = false;
                }
                if (success && type.equalsIgnoreCase("put")) {
                    response = "  <head>\n"
                            + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                            + "</head> " + result;//"All went ok";
                } else if (success && type.equalsIgnoreCase("get")) {
                    response = "  <head>\n"
                            + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                            + "</head> " + result;
                } else if (!(success)) {
                    /*if (result.equals("AD")) {
                     response = "  <head>\n"
                     + "<meta http-equiv=\"refresh\" content=\"3;URL=http://localhost:8000/store\">\n"
                     + "</head> Unfortunately all workers are down";
                     } else {*/
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

    // the algorithm that decides which workers will accept the new key-value pair
    private int[] schedulingAlgorithm() {
        int[] tmp = new int[2];
        /*int max = -1, sec_max = -1;
         for (int i = 0; i < MAX_WORKERS; i++) {
         if(max==-1) {
         if (workersScheduling[i] != -1 && work[i] != null && work[i].isAlive() && work[max+1].isAlive()) {
         if (workersScheduling[i] < workersScheduling[max+1]) {
         sec_max = max+1;
         max = i;
         } else if (workersScheduling[i] == workersScheduling[max+1]) {
         sec_max = i;
         } else {
         if (workersScheduling[i] < workersScheduling[sec_max]) {
         sec_max = i;
         }
         }
         }
         }
         if(max!=-1) {
         if (workersScheduling[i] != -1 && work[i] != null && work[i].isAlive() && work[max].isAlive()) {
         if (workersScheduling[i] < workersScheduling[max]) {
         sec_max = max;
         max = i;
         } else if (workersScheduling[i] == workersScheduling[max]) {
         sec_max = i;
         } else {
         if (workersScheduling[i] < workersScheduling[sec_max]) {
         sec_max = i;
         }
         }
         }
         }
         }*/
        int max = 0, sec_max = 0;
        for (int i = 0; i < work.length; i++) {
            if (work[i] != null && work[i].isAlive()) {
                if (work[i].getPR() > work[max].getPR()) {
                    sec_max = max;
                    max = i;
                } else if (work[i].getPR() == work[max].getPR()) {
                    sec_max = i;
                } else {
                    if (work[i].getPR() > work[sec_max].getPR()) {
                        sec_max = i;
                    }
                }
            }
        }
        // leaves out the pre-set value if none alive
        if (work[max].isAlive()) {
            tmp[0] = max;
        } else {
            tmp[0] = -1;
        }
        if (work[sec_max].isAlive()) {
            tmp[1] = sec_max;
        } else {
            tmp[1] = -1;
        }
        if (tmp[1] != -1 || (tmp[0] != -1 && tmp[1] != -1)) {
            for (int i = 0; i < work.length; i++) {
                if (work[i] != null && work[i].isAlive() && i != max && i != sec_max) {
                    work[i].incPR();
                }
            }
        }
        return tmp;
    }
}
