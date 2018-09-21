public class GameFactory implements IGameFactory {
    @Override
    public IGame create() {
        final GuessNumberGame game = new GuessNumberGame();
        System.out.println(game.getWelcomeMessage());
        return game;
    }
}
