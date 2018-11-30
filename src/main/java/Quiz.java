import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import database.DatabaseWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Quiz {
    String name;
    String initialMessage;
    String shareText;

    Map<Integer, String> questions = new HashMap<>();
    ArrayList<String> answers = new ArrayList<>();

    ArrayList<ArrayList<DestinationNode>> quizGraph;
    Map<String, Integer> answersIndexes;
    HashMap<String, HashMap<String, String>> characters;

    private DatabaseWorker db = new DatabaseWorker();

    Quiz(String quizData) throws QuizException {
        BuildQuiz(quizData);
    }

    Quiz(String name, String initialMessage, String shareText, Map<Integer, String> questions,
         ArrayList<String> answers, ArrayList<ArrayList<DestinationNode>> quizGraph,
         Map<String, Integer> answersIndexes, HashMap<String, HashMap<String, String>> characters) {
        this.name = name;
        this.initialMessage = initialMessage;
        this.shareText = shareText;
        this.questions = questions;
        this.answers = answers;
        this.quizGraph = quizGraph;
        this.answersIndexes = answersIndexes;
        this.characters = characters;
    }

    private void BuildQuiz(String data) throws QuizException {
        try {
            YamlReader reader = new YamlReader(data);
            QuizFile quizFile = reader.read(QuizFile.class);

            name = quizFile.name;
            initialMessage = quizFile.initialMessage;
            shareText = quizFile.shareText;
            characters = quizFile.characters;
            questions.put(0, "");
            answers.add(0, "");
            quizGraph = new ArrayList<>();
            answersIndexes = new HashMap<>();
            answersIndexes.put("", 0);
            quizGraph.add(new ArrayList<>());

            for (var item : quizFile.questions)
                questions.put(Integer.parseInt(item.get("id")) + 1, item.get("text"));

            var i = 0;
            for (var e : quizFile.answers) {
                answers.add(e.get("text"));
                answersIndexes.put(e.get("text"), i + 1);
                quizGraph.add(new ArrayList<>());
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
}