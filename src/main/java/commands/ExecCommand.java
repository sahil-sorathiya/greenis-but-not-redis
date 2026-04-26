package commands;

import context.*;
import resp.*;
import server.GreenisServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ExecCommand implements Command {

    @Override
    public String execute(Context context) throws IOException {
        ClientContext clientContext = context.clientContext;
        ServerContext serverContext = context.serverContext;

        ArrayList<RespObject> command = clientContext.currentCommand.values;

        System.out.println("here");
        //: If not exactly one argument passed, throw error
        //: EXEC
        if(command.size() != 1) {
            return RespWriter.writeString(new RespError("EXECABORT Transaction discarded because of: wrong number of arguments for 'exec' command"));
        }

        System.out.println("here");
        //: Exec without transaction start
        if(!clientContext.transactionFlag){
            return RespWriter.writeString(new RespError("ERR EXEC without MULTI"));
        }

        System.out.println("here");
        //: Run all commands from queue one by one and store responses
        ArrayList<RespObject> responses = new ArrayList<>();
        for(RespArray cmd: clientContext.commandQueue){
            String s = GreenisServer.handleCommand(cmd, clientContext, serverContext);
            InputStream in = new ByteArrayInputStream(s.getBytes());
            RespParser parser = new RespParser(in);
            RespObject r = parser.parse();
            responses.add(r);
        }

        System.out.println(responses.size());

        //: Reset flag and queue
        clientContext.transactionFlag = false;
        clientContext.commandQueue.clear();

        //: Return responses
        return RespWriter.writeString(new RespArray(responses));
    }
}
