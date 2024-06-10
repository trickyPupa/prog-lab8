package commands;

import common.commands.abstractions.Command;
import managers.ServerControlReceiver;

import java.util.function.Function;

public class UpdateDataServerCommand extends AbstractServerCommand {

    public UpdateDataServerCommand(Object[] args) {
        super("update", "Обновляет данные из базы данных.", "no", args);
    }

    @Override
    public int execute(ServerControlReceiver rec) {
        return rec.updateData();
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return UpdateDataServerCommand::new;
    }
}
