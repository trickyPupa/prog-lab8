package managers;

import common.abstractions.IOutputManager;

public class ServerOutputManager implements IOutputManager {
    private String response = "";
    private Exception e;

    @Override
    public void print(String s) {
        response += s + "\n";
    }

    @Override
    public void print(Object s){
        ;
    }

    public void addError(Exception e){
        this.e = e;
    }

    public String getResponse(){
        return response;
    }

    public String popResponce(){
        String s = response;
        response = "";
        return s;
    }

    public Exception getError(){
        var temp = e;
        e = null;
        return temp;
    }
}
