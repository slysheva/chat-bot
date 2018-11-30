import database.DatabaseWorker;
import database.QuizDataSet;
import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizRunner implements IGame {
    private HashMap<Integer, Quiz> quizzes;
    private DatabaseWorker db = new DatabaseWorker();

    QuizRunner() {
        quizzes = new HashMap<>();
        db.connect();
    }

    void addQuiz(int id, Quiz quiz) {
        quizzes.put(id, quiz);
    }

    private int getNextQuestionIndex(Quiz quiz, int edgeIndex, int currentQuestionId) {
        for (DestinationNode item : quiz.quizGraph.get(currentQuestionId)) {
            if (item.Edge == edgeIndex)
                return item.Node;
        }
        return  0;
    }

    private List<String> getAnswersList(Quiz quiz, int currentQuestionId){
        List<String> answersList = new ArrayList<>();
        for (DestinationNode item : quiz.quizGraph.get(currentQuestionId)){
            answersList.add(quiz.answers.get(item.Edge));
        }
        return answersList;
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
            return new ChatBotReply("Подумай ещё раз!", getAnswersList(quiz, currentQuestionId));
        }
        currentQuestionId = getNextQuestionIndex(quiz, quiz.answersIndexes.get(request), currentQuestionId);
        if (quiz.quizGraph.get(currentQuestionId).size() == 0) {
            stop(userId);
            String characterName = quiz.questions.get(currentQuestionId);
            return new ChatBotReply(String.format("Всё понятно. " + quiz.characters.get(characterName).get("description"),
                    characterName),
                    quiz.characters.get(characterName).get("image"),
                    String.format("пикси %s", characterName));
        }
        db.updateCurrentQuestionId(userId, currentQuestionId);
        return new ChatBotReply(quiz.questions.get(currentQuestionId), getAnswersList(quiz, currentQuestionId));
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
