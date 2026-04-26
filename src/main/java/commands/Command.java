package commands;

import context.Context;
import java.io.IOException;

public interface Command {
    String execute(Context context) throws IOException, InterruptedException;
}
