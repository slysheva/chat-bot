import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import database.QuizDataSet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Serializer {
    public static Quiz deserialize(QuizDataSet data) {
        Gson gson = new Gson();
        Type questions = new TypeToken<Map<Integer, String>>(){}.getType();
        Type answers = new TypeToken<ArrayList<String>>(){}.getType();
        Type quizGraph = new TypeToken<ArrayList<ArrayList<DestinationNode>>>(){}.getType();
        Type answersIndexes = new TypeToken<Map<String, Integer>>(){}.getType();
        Type characters = new TypeToken<HashMap<String, HashMap<String, String>>>(){}.getType();

        return new Quiz(data.name, data.initialMessage, data.shareText, gson.fromJson(data.questions, questions),
                gson.fromJson(data.answers, answers), gson.fromJson(data.quizGraph, quizGraph),
                gson.fromJson(data.answersIndexes, answersIndexes), gson.fromJson(data.characters, characters));
    }

    public static QuizDataSet serialize(Quiz quiz) {
        Gson gson = new Gson();
        return new QuizDataSet(quiz.name, quiz.initialMessage, quiz.shareText, gson.toJson(quiz.questions),
                gson.toJson(quiz.answers), gson.toJson(quiz.quizGraph), gson.toJson(quiz.answersIndexes),
                gson.toJson(quiz.characters));
    }
}
