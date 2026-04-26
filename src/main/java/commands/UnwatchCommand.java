package commands;

import context.ClientContext;
import context.Context;
import resp.*;
import store.DataStore;

import java.io.IOException;
import java.util.ArrayList;

public class UnwatchCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("QUEUED"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: UNWATCH
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'unwatch' command"));
        }

        //: Reset watched keys
        clientContext.watchedKeys.clear();

        //: Respond with OK
        return RespWriter.writeString(new RespSimpleString("OK"));
    }
}
