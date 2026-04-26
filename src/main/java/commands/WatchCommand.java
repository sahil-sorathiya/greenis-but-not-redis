package commands;

import context.ClientContext;
import context.Context;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.util.ArrayList;

public class WatchCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        DataStore dataStore = context.serverContext.dataStore;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            return RespWriter.writeString(new RespError("ERR WATCH inside MULTI is not allowed"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: WATCH <key>
        if(command.size() != 2) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'watch' command"));
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: if key don't exist in store, map it to null in watched keys
        if(!dataStore.store.containsKey(key)){
            clientContext.watchedKeys.put(key, null);
            return RespWriter.writeString(new RespSimpleString("OK"));
        }

        //: if key exist in store, map it to object it stored
        clientContext.watchedKeys.put(key, dataStore.store.get(key));
        return RespWriter.writeString(new RespSimpleString("OK"));
    }
}
