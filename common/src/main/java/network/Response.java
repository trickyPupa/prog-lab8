package network;

import common.commands.abstractions.Command;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

public class Response implements Serializable {
    protected String message = null;
    protected Exception error;
    protected Collection<Command> history;

    public Response(String msg, Collection<Command> history) {
        message = msg;
        this.history = history;
    }

    public Response(String msg, Collection<Command> history, Exception error) {
        this(msg, history);
        this.error = error;
    }

    public String getMessage(){
        return message;
    }

    public Collection<Command> getHistory(){
        return history;
    }

    public Optional<Exception> getError(){
        return Optional.ofNullable(error);
    }
}
