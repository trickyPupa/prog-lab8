package network;

import common.commands.abstractions.Command;

public class UserAuthRequest extends CommandRequest {
    public UserAuthRequest(Command c) {
        super(c, null);
    }
}
