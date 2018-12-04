import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import database.DatabaseWorker;

import java.util.*;


public class Quiz {
    String name;
    String initialMessage;
    String shareText;

    Map<Integer, String> questions = new HashMap<>();
    ArrayList<String> answers = new ArrayList<>();

    ArrayList<ArrayList<DestinationNode>> quizGraph;
    Map<String, Integer> answersIndexes;
    HashMap<String, HashMap<String, String>> results;

    private DatabaseWorker db = new DatabaseWorker();

    Quiz(String quizData) throws QuizException {
        BuildQuiz(quizData);
    }

    Quiz(String name, String initialMessage, String shareText, Map<Integer, String> questions,
         ArrayList<String> answers, ArrayList<ArrayList<DestinationNode>> quizGraph,
         Map<String, Integer> answersIndexes, HashMap<String, HashMap<String, String>> results) {
        this.name = name;
        this.initialMessage = initialMessage;
        this.shareText = shareText;
        this.questions = questions;
        this.answers = answers;
        this.quizGraph = quizGraph;
        this.answersIndexes = answersIndexes;
        this.results = results;
    }

    private void BuildQuiz(String data) throws QuizException {
        try {
            YamlReader reader = new YamlReader(data);
            QuizFile quizFile = reader.read(QuizFile.class);

            name = quizFile.name;
            initialMessage = quizFile.initialMessage;
            shareText = quizFile.shareText;
            results = quizFile.results;
            questions.put(0, "");
            answers.add(0, "");
            quizGraph = new ArrayList<>();
            answersIndexes = new HashMap<>();
            answersIndexes.put("", 0);

            for (var item : quizFile.questions)
                questions.put(Integer.parseInt(item.get("id")) + 1, item.get("text"));

            for (var i = 0; i < questions.size(); i++)
            {
                quizGraph.add(new ArrayList<>());
            }

            var i = 0;
            for (var e : quizFile.answers) {
                answers.add(e.get("text"));
                answersIndexes.put(e.get("text"), i + 1);
                int node = Integer.parseInt(e.get("from")) + 1;
                DestinationNode nextNode = new DestinationNode(Integer.parseInt(e.get("to")) + 1,
                        i + 1);
                quizGraph.get(node).add(nextNode);
                i++;
            }
            quizGraph.get(0).add(new DestinationNode(1, 0));

            db.connect();
            db.initDatabase();
        } catch (YamlException e)
        {
            throw new QuizException();
        }
    }

    int getNextQuestionIndex(int edgeIndex, int currentQuestionId) {
        for (DestinationNode item : quizGraph.get(currentQuestionId)) {
            if (item.Edge == edgeIndex)
                return item.Node;
        }
        return  0;
    }

    List<String> getAnswersList(int currentQuestionId){
        List<String> answersList = new ArrayList<>();
        for (DestinationNode item : quizGraph.get(currentQuestionId)){
            answersList.add(answers.get(item.Edge));
        }
        return answersList;
    }

    boolean dfs(int current, ArrayList<Integer> color, boolean hasCycle){
        color.set(current, 1);
        for (var i = 0; i < quizGraph.get(current).size(); i++){
            if (color.get(quizGraph.get(current).get(i).Node) == 0) {
                hasCycle = dfs(quizGraph.get(current).get(i).Node, color, hasCycle);
            }
            if (color.get(quizGraph.get(current).get(i).Node) == 1) {
                hasCycle = true;
            }
        }
        color.set(current, 2);
        return hasCycle;
    }

    boolean isValid() {
        ArrayList<Integer> color = new ArrayList<>(Collections.nCopies(questions.size(), 0));

        if (dfs(1, color, false))
            return false;
        for (var i = 1; i < questions.size(); i++)
            if (color.get(i) != 2)
                return false;
        return true;
    }
}