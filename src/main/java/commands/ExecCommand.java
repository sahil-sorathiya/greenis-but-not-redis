package commands;

import context.*;
import resp.*;
import server.GreenisServer;
import store.DataStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ExecCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        ServerContext serverContext = context.serverContext;
        DataStore dataStore = context.serverContext.dataStore;

        //: Check for subscribe-mode
        if(clientContext.subscribeModeFlag) {
            return RespWriter.writeString(new RespError("ERR Can't execute 'exec': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly one argument passed, throw error
        //: EXEC
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("EXECABORT Transaction discarded because of: wrong number of arguments for 'exec' command"));
        }

        //: Exec without transaction start
        if(!clientContext.transactionFlag){
            return RespWriter.writeString(new RespError("ERR EXEC without MULTI"));
        }

        //: Validate "Watched" keys
        for(String k: clientContext.watchedKeys.keySet()){
            //: If change found
            if(clientContext.watchedKeys.get(k) != dataStore.store.get(k)){
                //: Abort the transaction

                //: Reset flag and queue
                clientContext.transactionFlag = false;
                clientContext.commandQueue.clear();

                //: Reset watched keys
                clientContext.watchedKeys.clear();

                //: Respond with empty array
                return RespWriter.writeString(new RespArray(null));
            }
        }

        //: Reset flag
        clientContext.transactionFlag = false;

        //: Run all commands from queue one by one and store responses
        ArrayList<RespObject> responses = new ArrayList<>();

        for(RespArray cmd: new ArrayList<>(clientContext.commandQueue)){
            RespArray temp = clientContext.currentCommand;
            clientContext.currentCommand = cmd;

            String s = GreenisServer.handleCommand(cmd, clientContext, serverContext);

            clientContext.currentCommand = temp;

            InputStream in = new ByteArrayInputStream(s.getBytes());
            RespParser parser = new RespParser(in);
            RespObject r = parser.parse();
            responses.add(r);
        }

        //: Reset queue
        clientContext.commandQueue.clear();

        //: Return responses
        return RespWriter.writeString(new RespArray(responses));
    }
}
