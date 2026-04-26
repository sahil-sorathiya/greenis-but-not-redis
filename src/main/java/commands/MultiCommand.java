package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class MultiCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        ServerContext serverContext = context.serverContext;

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: MULTI
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'multi' command"));
        }

        //: Set flag
        clientContext.transactionFlag = true;

        //: Respond with OK
        return RespWriter.writeString(new RespSimpleString("OK"));

    }
}
