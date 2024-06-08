package network;

import common.commands.abstractions.Command;
import common.model.entities.Movie;

import java.util.Collection;

public class GetDataResponse extends Response {
    protected Movie[] movies;

    public GetDataResponse(String msg, Collection<Command> history, Movie[] movies) {
        super(msg, history);
        this.movies = movies;
    }

    public Movie[] getData(){
        return movies;
    }
}
