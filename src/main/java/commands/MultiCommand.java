package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class MultiCommand implements Command {
    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;

        //: Check for subscribe-mode
        if(clientContext.subscribeModeFlag) {
            return RespWriter.writeString(new RespError("ERR Can't execute 'multi': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: MULTI
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'multi' command"));
        }

        if(clientContext.transactionFlag){
            return RespWriter.writeString(new RespError("ERR MULTI calls can not be nested"));
        }

        //: Set flag
        clientContext.transactionFlag = true;

        //: Respond with OK
        return RespWriter.writeString(new RespSimpleString("OK"));

    }
}
