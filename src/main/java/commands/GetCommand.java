package commands;

import context.*;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class GetCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        DataStore dataStore = context.serverContext.dataStore;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("OK"));
        }
        
        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly two argument passed, throw error
        //: GET <key>
        if(command.size() != 2) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'get' command"));
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = dataStore.store.get(key);

        //: If key not found
        if(val == null){
            return RespWriter.writeString(new RespBulkString(null));
        }

        //: Check if val is not type of RespBulkString, throw error
        if(!(val instanceof RespBulkString)){
            return RespWriter.writeString(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
        }

        //: If key is expired
        if(Instant.now().isAfter(dataStore.expiry.get(key))){
            dataStore.store.remove(key);
            dataStore.expiry.remove(key);
            return RespWriter.writeString(new RespBulkString(null));
        }

        //: Respond with value if key is present and not expired
        return RespWriter.writeString((RespBulkString) val);
    }
}
