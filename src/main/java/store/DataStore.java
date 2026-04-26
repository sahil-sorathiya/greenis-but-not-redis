package store;

import resp.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

public class DataStore {
    public final HashMap<String, RespObject> store;
    public final HashMap<String, Instant> expiry;
    public final HashMap<String, SynchronousQueue<RespObject>> keyToBlpopQueue;

    public DataStore(){
        this.store = new HashMap<>();
        this.expiry = new HashMap<>();
        this.keyToBlpopQueue = new HashMap<>();
    }
}
