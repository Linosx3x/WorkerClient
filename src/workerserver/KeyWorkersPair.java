package workerserver;

// used by server in order to keep keys paired with workers
public class KeyWorkersPair {

    private String key;
    private int worker1, worker2;

    public KeyWorkersPair(String key, int worker1, int worker2) {
        this.key = key;
        this.worker1 = worker1;
        this.worker2 = worker2;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getWorker1() {
        return worker1;
    }

    public void setWorker1(int worker1) {
        this.worker1 = worker1;
    }

    public int getWorker2() {
        return worker2;
    }

    public void setWorker2(int worker2) {
        this.worker2 = worker2;
    }
}
