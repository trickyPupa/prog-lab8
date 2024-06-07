package network;

public class LoginCheckRequest extends Request {
    private String login;

    public LoginCheckRequest(String login) {
        this.login = login;
    }

    public String getLogin(){
        return login;
    }

    @Override
    public Object getContent() {
        return getLogin();
    }
}
