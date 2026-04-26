package commands;

import context.*;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class IncrCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        DataStore dataStore = context.serverContext.dataStore;

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly two argument passed, throw error
        //: INCR <key>
        if(command.size() != 2) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'incr' command"));
        }
        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: If key doesn't exist, add <key, 1>
        if(!dataStore.store.containsKey(key)){
            dataStore.store.put(key, new RespBulkString("1"));
            dataStore.expiry.put(key, Instant.MAX);
            return RespWriter.writeString(new RespInteger(1));
        }

        //: Key exists but value is not RespBulkString
        if(!(dataStore.store.get(key) instanceof RespBulkString)){
            return RespWriter.writeString(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
        }

        //: Key exists but value not of numeric type
        int val = 0;
        try{
            val = Integer.parseInt(((RespBulkString) dataStore.store.get(key)).value);
        } catch (NumberFormatException e){
            return RespWriter.writeString(new RespError("ERR value is not an integer or out of range"));
        }

        //: key exist and value is also numeric
        //: Increment value by 1, store and return it
        dataStore.store.put(key, new RespBulkString(String.valueOf(val+1)));
        return RespWriter.writeString(new RespInteger(val+1));

    }
}
