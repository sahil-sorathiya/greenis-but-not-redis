package context;

public class Context {
    public ServerContext serverContext;
    public ClientContext clientContext;

    public Context(ServerContext serverContext, ClientContext clientContext) {
        this.serverContext = serverContext;
        this.clientContext = clientContext;
    }
}
