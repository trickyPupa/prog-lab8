package network;

import common.commands.abstractions.Command;

public class UserRegisterRequest extends UserAuthRequest {
    String salt;

    public UserRegisterRequest(Command c, String salt) {
        super(c);
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }
}
