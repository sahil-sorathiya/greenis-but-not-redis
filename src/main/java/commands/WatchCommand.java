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

        //: Check for subscribe-mode
        if(clientContext.subscribeModeFlag) {
            return RespWriter.writeString(new RespError("ERR Can't execute 'watch': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context"));
        }

        //: Check for transaction
        if(clientContext.transactionFlag) {
            return RespWriter.writeString(new RespError("ERR WATCH inside MULTI is not allowed"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If less than two arguments passed, throw error
        //: WATCH <key1> ... <keyN>
        if(command.size() < 2) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'watch' command"));
        }

        //: Iterate over keys from command
        for(int i = 1; i < command.size(); i++){
            String key = ((RespBulkString) command.get(i)).value;

            //: if key don't exist in store, map it to null in watched keys
            if(!dataStore.store.containsKey(key)){
                clientContext.watchedKeys.put(key, null);
            }

            //: if key exist in store, map it to object it stored
            clientContext.watchedKeys.put(key, dataStore.store.get(key));
        }

        return RespWriter.writeString(new RespSimpleString("OK"));
    }
}
