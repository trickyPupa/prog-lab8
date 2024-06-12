package gui;

import client.ClientRequestManager;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;
import common.user.Session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class ManagersContainer {
    protected ClientRequestManager requestManager;
    protected Locale currentLocale = Locale.getDefault();
    protected Session session;
    protected Collection<Command> history;
    public Map<String, Function<Object[], Command>> commands = new HashMap<>();

    protected final Locale[] enabledLocales;

    public ManagersContainer(Locale[] locales) {
        enabledLocales = locales;
    }

    public ClientRequestManager getRequestManager() {
        return requestManager;
    }

    protected void setRequestManager(ClientRequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Collection<Command> getHistory() {
        return history;
    }

    public void setCommands(Map<String, AbstractCommand> cmlist){
        commands.clear();
        for (String name : cmlist.keySet()){
            commands.put(name, cmlist.get(name).getConstructor());
        }
    }
}
