package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class ConfigCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        ServerContext serverContext = context.serverContext;

        //: Check for transaction
        if(clientContext.transactionFlag) {
            clientContext.commandQueue.add(clientContext.currentCommand);
            return RespWriter.writeString(new RespSimpleString("QUEUED"));
        }

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        //: If not exactly three arguments passed, throw error
        //: UNWATCH
        if(command.size() != 3) {
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'config' command"));
        }

        //: If second argument is "GET"
        if(((RespBulkString)command.get(1)).value.equalsIgnoreCase("GET")){
            //: If third argument is "dir"
            if(((RespBulkString)command.get(2)).value.equalsIgnoreCase("dir")){
                ArrayList<RespObject> response = new ArrayList<>();
                response.add(command.get(2));
                response.add(new RespBulkString(serverContext.dir));
                return RespWriter.writeString(new RespArray(response));
            }
            //: If third argument is "dbfilename"
            else if(((RespBulkString)command.get(2)).value.equalsIgnoreCase("dbfilename")){
                ArrayList<RespObject> response = new ArrayList<>();
                response.add(command.get(2));
                response.add(new RespBulkString(serverContext.dbFileName));
                return RespWriter.writeString(new RespArray(response));
            }
        }
        return RespWriter.writeString(new RespArray(new ArrayList<>()));
    }
}
