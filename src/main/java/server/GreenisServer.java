package server;

import commands.*;
import context.*;
import resp.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GreenisServer {
    ServerContext serverContext;
    public GreenisServer() {}

    public void init(int port, String dir, String dbFileName) throws IOException {
        this.serverContext = new ServerContext(port, dir, dbFileName);
    }

    public void close() throws IOException {
        if(serverContext.serverChannel != null){
            serverContext.serverChannel.close();
        }
        if(serverContext.selector != null){
            serverContext.selector.close();
        }
    }

    public void bind() throws IOException {
        //: Bind port to socket
        serverContext.serverChannel.bind(new InetSocketAddress(serverContext.serverPort));
    }

    public void configureNonBlocking() throws IOException {
        //: Make it non-blocking
        serverContext.serverChannel.configureBlocking(false);
    }

    public void registerAcceptEvent() throws ClosedChannelException {
        //: Register Selector with Server-Socket channel to watch on event/key for Socket-Accept operation
        serverContext.serverChannel.register(serverContext.selector, SelectionKey.OP_ACCEPT);
    }

    public void handleEvents() throws IOException {
        while (true) {
            //: Wait for event(s) to occur
            int totalReadyEvents = serverContext.selector.select();

            //: Handle Spurious Wakeup
            if(totalReadyEvents == 0) continue;

            //: Iterate over all event(s)/key(s)
            Iterator<SelectionKey> iterator = serverContext.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {

                //: Extract Event/key
                SelectionKey key = iterator.next();
                //: Remove it from Set
                iterator.remove();

                //: If event is of type Socket-Accept operation
                if (key.isAcceptable()) handleAcceptEvent(key);

                //: If event is of type Socket-Read operation
                else if (key.isReadable()) handleReadEvent(key);

            }
        }
    }

    private void handleAcceptEvent(SelectionKey key) throws IOException {
        SelectableChannel c =  key.channel();

        //: Check channel is ServerSocketChannel
        if(c instanceof ServerSocketChannel server){
            //: Accept the connection
            SocketChannel client = server.accept();

            //: Validation
            if (client == null) return;

            //: Make it non-blocking
            client.configureBlocking(false);

            //: Register Selector with Client-Socket channel to watch on event/key for Socket-Read operation
            ClientContext clientContext = new ClientContext(client);
            client.register(serverContext.selector, SelectionKey.OP_READ, clientContext);
        }
        else{
            throw new IOException("Channel is either null or not an instance of ServerSocketChannel");
        }
    }

    private void handleReadEvent(SelectionKey key) throws IOException {
        //: Get channel for event
        SelectableChannel c = key.channel();

        //: Check Channel is SocketChannel
        if(c instanceof SocketChannel client) {
            String received = readFromChannel(client);

            if(received == null) return;

            RespArray command = parseCommand(received);

            ClientContext clientContext = (ClientContext) key.attachment();
            clientContext.currentCommand = command;
            String response = handleCommand(command, clientContext, serverContext);

            if(response == null) return;

            writeToChannel(client, response);
        }
        else{
            throw new IOException("Channel is either null or not an instance of ServerSocketChannel");
        }
    }

    private String readFromChannel(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder requestBuilder = new StringBuilder();

        int bytesRead;

        //: Read until no more data is available
        while ((bytesRead = client.read(buffer)) > 0) {
            buffer.flip();

            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            requestBuilder.append(new String(data));

            buffer.clear();
        }

        //: Client closed connection
        if (bytesRead == -1) {
            client.close();
            return null;
        }

        return requestBuilder.toString();
    }

    private void writeToChannel(SocketChannel client, String response) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(response.getBytes());

        //: Write until entire response is sent
        while (writeBuffer.hasRemaining()) {
            client.write(writeBuffer);
        }
    }

    private RespArray parseCommand(String received) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(received.getBytes());
        RespParser parser = new RespParser(inputStream);
        RespObject r = parser.parse();
        if(r == null) return null;
        return (RespArray) r;
    }

    public static String handleCommand(RespArray command, ClientContext clientContext, ServerContext serverContext) throws IOException {
        if(command == null){
            return null;
        }

        Context context = new Context(serverContext, clientContext);
        String commandName = ((RespBulkString) command.values.getFirst()).value;

        if (commandName.equalsIgnoreCase("PING")) {
            return new PingCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("ECHO")){
            return new EchoCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("SET")){
            return new SetCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("GET")){
            return new GetCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("INCR")){
            return new IncrCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("MULTI")){
            return new MultiCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("EXEC")){
            return new ExecCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("DISCARD")){
            return new DiscardCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("WATCH")){
            return new WatchCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("UNWATCH")){
            return new UnwatchCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("CONFIG")){
            return new ConfigCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("SUBSCRIBE")){
            return new SubscribeCommand().execute(context);
        }
        else if(commandName.equalsIgnoreCase("PUBLISH")){
            return new PublishCommand().execute(context);
        }
        else {
            return RespWriter.writeString(new RespError("ERR unknown command " + commandName));
        }
//        return null;
    }


}
