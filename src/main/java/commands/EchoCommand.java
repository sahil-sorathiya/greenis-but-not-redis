package commands;

import context.*;
import resp.*;

import java.io.IOException;
import java.util.ArrayList;

public class EchoCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        ArrayList<RespObject> command = ((RespArray) clientContext.currentCommand).values;

        //: If not exactly two arguments passed, throw error
        //: ECHO <message>
        if(((RespArray)clientContext.currentCommand).values.size() != 2){
            return RespWriter.writeString(new RespError("ERR wrong number of arguments for 'echo' command"));
        }
        return RespWriter.writeString(command.get(1));
    }
}
