import static org.junit.jupiter.api.Assertions.*;

import org.glassfish.grizzly.utils.Pair;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

class Tests
{
    @Test
    void testIncorrectAnswerPixie() throws FileNotFoundException {
        var gameInstance = new PixieQuiz("pixie.yml");
        gameInstance.markActive(0);
        var answer = "???";
        gameInstance.proceedRequest("", 0);
        ChatBotReply curReply = gameInstance.proceedRequest(answer + "", 0);
        assertEquals(curReply.message, "Подумай ещё раз!");
    }

    @Test
    void testCorrectCharacterPixie() throws FileNotFoundException {
        var gameInstance = new PixieQuiz("pixie.yml");
        gameInstance.markActive(0);
        ChatBotReply curReply = gameInstance.proceedRequest("", 0);
        while (curReply.keyboardOptions != null) {
            curReply = gameInstance.proceedRequest(curReply.keyboardOptions.get(0), 0);
        }
        assertTrue(curReply.message.startsWith("Всё понятно. Твоя winx пикси - Диджит"));
    }

    @Test
    void testAnswerWhenGameIsEnded() {
        var tests = new ArrayList<Pair<String, Class<? extends IGame>>>();
        tests.add(new Pair<>("pixie.yml", PixieQuiz.class));
        var chatBot = new ChatBot(new GameFactory(), tests);
        chatBot.answer("Старт", 0);
        chatBot.answer("Стоп", 0);
        var reply = chatBot.answer("???", 0);
        assertEquals("Игра ещё не началась.", reply.message);
    }

    @Test
    void testAnswerWhenGameStarts() {
        var tests = new ArrayList<Pair<String, Class<? extends IGame>>>();
        tests.add(new Pair<>("pixie.yml", PixieQuiz.class));
        var chatBot = new ChatBot(new GameFactory(), tests);
        var reply = chatBot.answer("Старт", 0);
        assertEquals("Привет! Сейчас мы узнаем, кто ты из фей Winx.\n" +
                "По характеру ты...", reply.message);
    }

    @Test
    void testAnswerWhenGameIsStopped() {
        var tests = new ArrayList<Pair<String, Class<? extends IGame>>>();
        tests.add(new Pair<>("pixie.yml", PixieQuiz.class));
        var chatBot = new ChatBot(new GameFactory(), tests);
        chatBot.answer("Старт", 0);
        var reply = chatBot.answer("Стоп", 0);
        assertEquals("Игра закончена.", reply.message);
    }
}
