package commands;

import context.*;
import resp.*;

import java.io.IOException;

public class PingCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("OK"));
        }

        //: If not exactly one argument passed, throw error
        //: PING
        if(clientContext.currentCommand.values.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'ping' command"));
        }
        return RespWriter.writeString(new RespSimpleString("PONG"));
    }
}
