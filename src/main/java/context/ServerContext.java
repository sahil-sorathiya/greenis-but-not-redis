package context;

import store.DataStore;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class ServerContext {
    public final int serverPort;
    public final ServerSocketChannel serverChannel;
    public final Selector selector;
    public final DataStore dataStore;

    public ServerContext(int serverPort) throws IOException {
        this.serverPort = serverPort;
        this.serverChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        this.dataStore = new DataStore();
    }
}
