package commands;

import context.*;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class SubscribeCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        DataStore dataStore = context.serverContext.dataStore;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("QUEUED"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly three or five arguments passed, throw error
        //: SUBSCRIBE <channel-name>
        if(command.size() != 2) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'subscribe' command"));
        }

        //: Extract channel-name from command
        String channelName = ((RespBulkString) command.get(1)).value;

        //: Add channel-name in client context
        clientContext.subscribedChannels.add(channelName);

        //: Check for channel existence, if not then create empty one
        if(!dataStore.channels.containsKey(channelName)){
            dataStore.channels.put(channelName, new HashSet<>());
        }

        //: Add this client to channel
        dataStore.channels.get(channelName).add(clientContext);

        //: Set flag
        clientContext.subscribeModeFlag = true;

        ArrayList<RespObject> response = new ArrayList<>();
        response.add(new RespBulkString("subscribe"));
        response.add(command.get(1));
        response.add(new RespInteger(clientContext.subscribedChannels.size()));

        return RespWriter.writeString(new RespArray(response));
    }
}
