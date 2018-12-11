import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class Behaviour {
    private HashMap<Pair<State, CommandType>, Callable<List<ChatAction>> > behaviour;

    public void setTransition(State state, CommandType command, Callable<List<ChatAction>> action){
        behaviour.put( new Pair<State, CommandType>(state, command), action);
    }
}

