package commands;

import context.ClientContext;
import context.Context;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

public class PublishCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        DataStore dataStore = context.serverContext.dataStore;

        //: Check for subscribe-mode
        if(clientContext.subscribeModeFlag) {
            return RespWriter.writeString(new RespError("ERR Can't execute 'publish': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context"));
        }

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("QUEUED"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly three arguments passed, throw error
        //: PUBLISH <channel-name> <message>
        if(command.size() != 3 ) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'publish' command"));
        }

        //: Extract channel-name from command
        String channelName = ((RespBulkString) command.get(1)).value;
        String message = ((RespBulkString) command.get(2)).value;

        //: Check for channel existence, if not then respond with RespInteger 0
        if(!dataStore.channels.containsKey(channelName)){
            return RespWriter.writeString(new RespInteger(0));
        }

        //: Prepare Response
        ArrayList <RespObject> temp = new ArrayList<>();
        temp.add(new RespBulkString("message"));
        temp.add(new RespBulkString(channelName));
        temp.add(new RespBulkString(message));
        String response = RespWriter.writeString(new RespArray(temp));

        //: Send message to all clients
        for(ClientContext cc: new HashSet<>(dataStore.channels.get(channelName))){
            ByteBuffer writeBuffer = ByteBuffer.wrap(response.getBytes());

            //: Write until entire response is sent
            while (writeBuffer.hasRemaining()) {
                cc.client.write(writeBuffer);
            }
        }

        return RespWriter.writeString(new RespInteger(dataStore.channels.get(channelName).size()));
    }
}
