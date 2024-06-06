package gui;

import client.ClientRequestManager;
import client.GuiClientReceiver;
import common.user.Session;

import java.util.Locale;

public class ManagersContainer {
    protected ClientRequestManager requestManager;
    protected GuiClientReceiver receiver;
    protected Locale currentLocale = Locale.getDefault();
    protected Session session;

    public ManagersContainer() {}

    public ClientRequestManager getRequestManager() {
        return requestManager;
    }

    public GuiClientReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(GuiClientReceiver receiver) {
        this.receiver = receiver;
    }

    public void setRequestManager(ClientRequestManager requestManager) {
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
}