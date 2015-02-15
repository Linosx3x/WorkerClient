package workerclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class TrueWorker extends Thread {

    // the worker's id
    private int id;
    // the worker's file name 
    private String filename = "worker";
    private Map<String, String> parameters = new HashMap<>();
    private File keyValPairs;
    private String message = "";
    private String response = "";

    // create the worker
    public TrueWorker(int id) {
        this.id = id;
        this.filename = filename.concat(id + ".txt");
        this.keyValPairs = new File(filename);
    }

    @Override
    public void run() {
        while (true) {
            if (!message.equals("")) {
                System.out.println("Message is: " + message);
            }
            if (!message.equals("")) {
                if (message.startsWith("get")) {
                    StringTokenizer token = new StringTokenizer(message);
                    token.nextToken(" ");
                    String key = token.nextToken();
                    get(key);
                } else if (message.startsWith("put")) {
                    StringTokenizer token = new StringTokenizer(message);
                    token.nextToken(" ");
                    String key = token.nextToken(" ");
                    String value = token.nextToken();
                    put(key, value);
                } else {
                    System.out.println("Wrong message received! Done nothing.");
                }
                message = "";
            }
        }
    }

    @Override
    public void start() {
        System.out.println("The worker #" + id + " started.");
        if (readFile()) {
            System.out.println("Searching data in file named \"" + filename + "\"");
        } else {
            System.out.println("The worker #" + id + " finishing due to file problems.");
            try {
                this.join(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.start();
    }

    // set the message given from server
    public boolean setMessage(String msg) {
        if (message.equals("")) {
            message = msg;
            return true;
        }
        return false;
    }

    // get the response
    public String getResponse() {
        String tmp = "";
        if (!response.equals("")) {
            tmp = response;
            response = "";
        }
        return tmp;
    }

    // handles the get operation
    private void get(String key) {
        // String value = null;
        if (parameters.containsKey(key)) {
            response = parameters.get(key);
        } else {
            response = "Not found!";
        }
    }

    // handles the put operation
    private void put(String key, String value) {
        parameters.put(key, value);
        response = "ACK";
    }

    private boolean readFile() {
        // check if file doesn't exist
        if (!(keyValPairs.exists())) {
            try {
                keyValPairs.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else { // else, read the values from file 
            try {
                BufferedReader br = new BufferedReader(new FileReader(keyValPairs));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] value = line.split("=");
                    if (value.length > 1) {
                        parameters.put(value[0], value[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
