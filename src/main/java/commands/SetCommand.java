package commands;

import context.*;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;

public class SetCommand implements Command {
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
        //: SET <key> <value> [<EX/PX> <time>]
        if(command.size() != 3 && command.size() != 5) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'set' command"));
        }

        //: Extract key and value from command
        String key = ((RespBulkString) command.get(1)).value;
        RespObject value = command.get(2);

        //: Check if user want to set expiry on the key
        if(command.size() > 3){
            String unit = ((RespBulkString) command.get(3)).value;
            String time = ((RespBulkString) command.get(4)).value;

            //: Validate unit argument
            if(!unit.equalsIgnoreCase("PX") && !unit.equalsIgnoreCase("EX")){
                return RespWriter.writeString(new RespError("ERR syntax error"));
            }

            //: Validate seconds argument, that it is parsable or not, If not throw error
            try {
                //: If Unit is EX, multiply it to 1000
                long millis = Long.parseLong(time) * ((unit.equalsIgnoreCase("EX")) ? 1000 : 1);
                //: Store expiry timestamp in datastore
                dataStore.expiry.put(key, Instant.now().plusMillis(millis));
            } catch (NumberFormatException e) {
                return RespWriter.writeString(new RespError("ERR value is not an integer or out of range"));
            } catch (DateTimeException e){
                return RespWriter.writeString(new RespError("ERR invalid expire time in 'set' command"));
            }


        }
        //: If not set expiry to max timestamp possible
        else{
            dataStore.expiry.put(key, Instant.MAX);
        }

        //: Store key and value in store
        dataStore.store.put(key, value);

        //: Respond OK
        return RespWriter.writeString(new RespSimpleString("OK"));
    }
}
