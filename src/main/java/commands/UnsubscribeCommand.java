package commands;

import context.ClientContext;
import context.Context;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.util.ArrayList;

public class UnsubscribeCommand implements Command {

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

        //: If not exactly two arguments passed, throw error
        //: UNSUBSCRIBE <channel-name>
        if(command.size() != 2 ) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'unsubscribe' command"));
        }

        //: Extract channel-name from command
        String channelName = ((RespBulkString) command.get(1)).value;

        //: Validate channel is subscribed first, if yes then remove it, otherwise ignore it
        if(clientContext.subscribedChannels.contains(channelName)){
            clientContext.subscribedChannels.remove(channelName);
        }

        //: Remove from datastore if channel exist
        if(dataStore.channels.containsKey(channelName)){
            dataStore.channels.get(channelName).remove(clientContext);
        }

        //: Respond with RespArray
        ArrayList<RespObject> response = new ArrayList<>();
        response.add(new RespBulkString("unsubscribe"));
        response.add(command.get(1));
        response.add(new RespInteger(clientContext.subscribedChannels.size()));

        return RespWriter.writeString(new RespArray(response));
    }
}
