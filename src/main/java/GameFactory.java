import java.io.FileNotFoundException;

public class GameFactory implements IGameFactory {
    @Override
    public IGame create() {
        try {
            return new PixieQuiz("PixieTest.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
