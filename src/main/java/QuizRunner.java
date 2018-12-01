import database.DatabaseWorker;
import database.QuizDataSet;
import org.glassfish.grizzly.utils.Pair;

import java.util.Objects;

public class QuizRunner implements IGame {
    private DatabaseWorker db = new DatabaseWorker();

    protected final String noSuchAnswer = "Такого варианта ответа нет. Попробуй ещё раз";
    protected final String quizFinished = "Тест пройден.";

    QuizRunner() {
        db.connect();
    }

    private Pair<Integer, Integer> getGameData(int userId) {
        return db.getCurrentQuizState(userId);
    }

    @Override
    public ChatBotReply proceedRequest(String request, int userId) {
        Pair<Integer, Integer> gameData = getGameData(userId);
        int currentQuizId = gameData.getFirst();
        int currentQuestionId = gameData.getSecond();
        Quiz quiz = Objects.requireNonNull(loadQuiz(currentQuizId));

        if (!quiz.answersIndexes.containsKey(request)) {
            return new ChatBotReply(noSuchAnswer, quiz.getAnswersList(currentQuestionId));
        }
        currentQuestionId = quiz.getNextQuestionIndex(quiz.answersIndexes.get(request), currentQuestionId);
        if (quiz.quizGraph.get(currentQuestionId).size() == 0) {
            stop(userId);
            String characterName = quiz.questions.get(currentQuestionId);
            return new ChatBotReply(String.format(quizFinished + quiz.characters.get(characterName).get("description"),
                    characterName),
                    quiz.characters.get(characterName).get("image"),
                    String.format("пикси %s", characterName));
        }
        db.updateCurrentQuestionId(userId, currentQuestionId);
        return new ChatBotReply(quiz.questions.get(currentQuestionId), quiz.getAnswersList(currentQuestionId));
    }

    @Override
    public String getInitialMessage(int quizId) {
        return Objects.requireNonNull(loadQuiz(quizId)).initialMessage;
    }

    @Override
    public boolean start(int userId, int quizId) {
        if (quizNotExists(quizId)) {
            return false;
        }
        db.markGameActive(userId, quizId);
        return true;
    }

    private Quiz loadQuiz(int quizId) {
        if (quizNotExists(quizId))
            return null;
        QuizDataSet data = db.getQuiz(quizId);
        return Serializer.deserialize(data);
    }

    private boolean quizNotExists(int quizId) {
        return !db.quizExists(quizId);
    }

    @Override
    public void stop(int userId) {
        db.markGameInactive(userId);
    }

    @Override
    public boolean isActive(int userId) {
        return db.isGameActive(userId);
    }
}
