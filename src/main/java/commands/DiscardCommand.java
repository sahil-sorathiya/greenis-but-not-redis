package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class DiscardCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;

        //: Check for subscribe-mode
        if(clientContext.subscribeModeFlag) {
            return RespWriter.writeString(new RespError("ERR Can't execute 'discard': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: DISCARD
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'discard' command"));
        }

        //: Discard without transaction start
        if(!clientContext.transactionFlag){
            return RespWriter.writeString(new RespError("ERR DISCARD without MULTI"));
        }

        //: Reset flag, queue and watch keys
        clientContext.transactionFlag = false;
        clientContext.commandQueue.clear();
        clientContext.watchedKeys.clear();

        //: Respond with OK
        return RespWriter.writeString(new RespSimpleString("OK"));
    }
}
