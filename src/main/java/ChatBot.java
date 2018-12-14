import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.List;

class ChatBot {
    private IGame gameInstance;
    private IGameFactory gameFactory;
    private List<Pair<String, Class<? extends IGame>>> tests;
    private Integer curTest;

    ChatBot(IGameFactory gameFactory, List<Pair<String, Class<? extends IGame>>> tests) {
        this.gameFactory = gameFactory;
        this.tests = tests;
        curTest = 0;
        gameInstance = gameFactory.create(tests.get(curTest).getSecond(), tests.get(curTest).getFirst());
    }

    ChatBotReply answer(String message, int userId) {
        ChatBotReply chatBotReply = new ChatBotReply("", new ArrayList<String>()); return chatBotReply;
    }
}
