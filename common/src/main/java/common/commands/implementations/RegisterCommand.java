package common.commands.implementations;

import common.abstractions.AbstractAuthInterface;
import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RegisterCommand extends AbstractCommand {
    public RegisterCommand(Object[] args){
        super("sign_in", "Команда для регистрации нового пользователя.", "{user}", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        ((AbstractAuthInterface) rec).registerUser(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        ((AbstractAuthInterface) rec).registerUser(getArgs());
    }
    @Override
    public Function<Object[], Command> getConstructor() {
        return RegisterCommand::new;
    }
}
