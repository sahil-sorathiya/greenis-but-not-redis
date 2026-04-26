package context;

import resp.RespArray;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Queue;

public class ClientContext {
    public final SocketChannel client;
    public RespArray currentCommand;
    public boolean transactionFlag;
    public ArrayList<RespArray> commandQueue;

    public ClientContext(SocketChannel client, RespArray currentCommand) {
        this.client = client;
        this.currentCommand = currentCommand;
        this.transactionFlag = false;
        this.commandQueue = new ArrayList<>();
    }
}
