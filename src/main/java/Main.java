import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from greenis will appear here!");

        //: Port on which greenis server will listen
        int port = 6379;
        try (
                //: Open Server Socket Channel
                ServerSocketChannel serverChannel = ServerSocketChannel.open();

                //: Open Selector
                Selector selector = Selector.open()
        ){

            //: Bind port to socket
            serverChannel.bind(new InetSocketAddress(port));

            //: Make it non-blocking
            serverChannel.configureBlocking(false);

            //: Register Selector with Server-Socket channel to watch on event/key for Socket-Accept operation
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                //: Wait for event(s) to occur
                int totalReadyEvents = selector.select();

                //: Handle Spurious Wakeup
                if(totalReadyEvents == 0) continue;

                //: Iterate over all event(s)/key(s)
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {

                    //: Extract key
                    SelectionKey key = iterator.next();
                    //: Remove it from Set
                    iterator.remove();

                    //: If event is of type Socket-Accept operation
                    if (key.isAcceptable()) {

                        //: Get channel for event
                        SelectableChannel c = key.channel();

                        //: Check channel is ServerSocketChannel
                        if(c instanceof ServerSocketChannel server){
                            //: Accept the connection
                            SocketChannel client = server.accept();

                            //: Validation
                            if (client == null) continue;

                            //: Make it non-blocking
                            client.configureBlocking(false);

                            //: Register Selector with Client-Socket channel to watch on event/key for Socket-Read operation
                            client.register(selector, SelectionKey.OP_READ);
                        }
                        else{
                            throw new IOException("Channel is either null or not an instance of ServerSocketChannel");
                        }
                    }

                    //: If event is of type Socket-Read operation
                    else if (key.isReadable()) {

                        //: Get channel for event
                        SelectableChannel c = key.channel();

                        //: Check Channel is SocketChannel
                        if(c instanceof SocketChannel client){
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            int bytesRead = client.read(buffer);

                            if (bytesRead == -1) {
                                client.close();
                                continue;
                            }

                            buffer.flip();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            System.out.println(new String(data).trim());
                        }
                        else{
                            throw new IOException("Channel is either null or not an instance of ServerSocketChannel");
                        }
                    }
                }
            }


        } catch (IOException e) {
            System.out.println("IOException at Main: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception at Main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

