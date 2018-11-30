import database.DatabaseWorker;
import database.QuizDataSet;
import org.glassfish.grizzly.utils.Pair;

import java.util.HashMap;

public class QuizRunner implements IGame {
    private HashMap<Integer, Quiz> quizzes;
    private DatabaseWorker db = new DatabaseWorker();

    protected final String noSuchAnswer = "Такого варианта ответа нет. Попробуй ещё раз";
    protected final String quizFinished = "Тест пройден.";

    QuizRunner() {
        quizzes = new HashMap<>();
        db.connect();
    }

    void addQuiz(int id, Quiz quiz) {
        quizzes.put(id, quiz);
    }

    private Pair<Integer, Integer> getGameData(int userId) {
        return db.getCurrentQuizState(userId);
    }

    @Override
    public ChatBotReply proceedRequest(String request, int userId) {
        Pair<Integer, Integer> gameData = getGameData(userId);
        int currentQuizId = gameData.getFirst();
        int currentQuestionId = gameData.getSecond();
        Quiz quiz = quizzes.get(currentQuizId);

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
        return quizzes.get(quizId).initialMessage;
    }

    @Override
    public boolean start(int userId, int quizId) {
        if (!quizExists(quizId)) {
            if (!loadQuiz(quizId))
                return false;
        }
        db.markGameActive(userId, quizId);
        return true;
    }

    private boolean loadQuiz(int quizId) {
        QuizDataSet data = db.getQuiz(quizId);
        if (data == null)
            return false;
        addQuiz(quizId, Serializer.deserialize(data));
        return true;
    }

    private boolean quizExists(int quizId) {
        return quizzes.containsKey(quizId);
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
