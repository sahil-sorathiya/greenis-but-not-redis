package context;

import resp.*;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientContext {
    public final SocketChannel client;
    public RespArray currentCommand;
    public boolean transactionFlag;
    public ArrayList<RespArray> commandQueue;
    public HashMap <String, RespObject> watchedKeys;

    public ClientContext(SocketChannel client) {
        this.client = client;
        this.currentCommand = null;
        this.transactionFlag = false;
        this.commandQueue = new ArrayList<>();
        this.watchedKeys = new HashMap<>();
    }
}
