package context;

import resp.*;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ClientContext {
    public final SocketChannel client;
    public RespArray currentCommand;
    public boolean transactionFlag;
    public ArrayList<RespArray> commandQueue;
    public HashMap <String, RespObject> watchedKeys;
    public HashSet <String> subscribedChannels;

    public ClientContext(SocketChannel client) {
        this.client = client;
        this.currentCommand = null;
        this.transactionFlag = false;
        this.commandQueue = new ArrayList<>();
        this.watchedKeys = new HashMap<>();
        this.subscribedChannels = new HashSet<>();
    }
}
