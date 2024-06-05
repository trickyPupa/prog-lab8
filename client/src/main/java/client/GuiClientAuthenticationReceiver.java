package client;

import builders.AuthUser;
import builders.RegisterUser;
import common.abstractions.AbstractReceiver;
import common.user.Session;
import common.user.User;

public class GuiClientAuthenticationReceiver extends ClientAuthenticationReceiver {
    public GuiClientAuthenticationReceiver(AbstractReceiver receiver, AbstractClientRequestManager requestManager) {
        super(receiver, requestManager);
    }

    @Override
    public void authUser(Object[] args) {
        var pair = AuthUser.getUser(inputManager, outputManager, requestManager);
        User user = pair.getFirst();
        String salt = pair.getSecond();
        setCurrentSession(new Session(user, salt));

        addArg(args, user);
    }

    @Override
    public void registerUser(Object[] args) {
        var pair = RegisterUser.getUser(inputManager, outputManager, requestManager);
        User user = pair.getFirst();
        String salt = pair.getSecond();
        setCurrentSession(new Session(user, salt));

        addArg(args, user);
    }

    @Override
    public void logOut(Object[] args) {
        setCurrentSession(null);
    }
}
