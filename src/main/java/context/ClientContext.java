package context;

import resp.RespArray;

import java.nio.channels.SocketChannel;

public class ClientContext {
    public final SocketChannel client;
    public final RespArray currentCommand;

    public ClientContext(SocketChannel client, RespArray currentCommand) {
        this.client = client;
        this.currentCommand = currentCommand;
    }
}
