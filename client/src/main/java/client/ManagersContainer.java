package client;

import java.util.Locale;
import java.util.ResourceBundle;

public class ManagersContainer {
    protected ClientRequestManager requestManager;
    protected GuiClientReceiver receiver;
    protected Locale currentLocale = Locale.getDefault();

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
}
