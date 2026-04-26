package context;

import store.DataStore;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class ServerContext {
    public final int serverPort;
    public final String dir;
    public final String dbFileName;

    public final ServerSocketChannel serverChannel;
    public final Selector selector;
    public final DataStore dataStore;

    public ServerContext(int serverPort, String dir, String dbFileName) throws IOException {
        this.serverPort = serverPort;
        this.dir = dir;
        this.dbFileName = dbFileName;

        this.serverChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        this.dataStore = new DataStore();
    }
}
