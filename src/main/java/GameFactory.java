import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class GameFactory implements IGameFactory {
    @Override
    public IGame create(Class<? extends IGame> game, String fileName)
    {
        try {
            return game.cast(game
                    .getDeclaredConstructors()[0]
                    .newInstance(fileName));
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
