package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class PingCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("QUEUED"));
        }

        //: If not exactly one argument passed, throw error
        //: PING
        if(clientContext.currentCommand.values.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'ping' command"));
        }

        //: If subscribe-mode is on, respond with RespArray
        if(clientContext.subscribeModeFlag) {
            ArrayList <RespObject> response = new ArrayList<>();
            response.add(new RespBulkString("pong"));
            response.add(new RespBulkString(""));
            return RespWriter.writeString(new RespArray(response));
        }

        return RespWriter.writeString(new RespSimpleString("PONG"));
    }
}
