package common.commands.implementations;

import common.abstractions.AbstractAuthInterface;
import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class AuthCommand extends AbstractCommand {
    public AuthCommand(Object[] args) {
        super("log_in", "Команда для аутентификации существующего пользователя.", "login", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        ((AbstractAuthInterface) rec).authUser(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        ((AbstractAuthInterface) rec).authUser(getArgs());
    }
    @Override
    public Function<Object[], Command> getConstructor() {
        return AuthCommand::new;
    }
}
