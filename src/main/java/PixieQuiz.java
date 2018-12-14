//import com.esotericsoftware.yamlbeans.YamlException;
//import com.esotericsoftware.yamlbeans.YamlReader;
//import database.DatabaseWorker;
//import database.GameDataSet;
//
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.util.*;
//
//
//public class PixieQuiz implements IGame {
//    private Map<Integer, String> questions = new HashMap<Integer, String>();
//    private ArrayList<String> answerOptions = new ArrayList<>();
//    private ArrayList< ArrayList<DestinationNode> > testGraph;
//    private Map<String, Integer> optionsIndex;
//    private HashMap<String, HashMap<String, String>> characters;
//
//    private DatabaseWorker db = new DatabaseWorker();
//
//    PixieQuiz(String fileName) throws FileNotFoundException {
//        try {
//            YamlReader reader = new YamlReader(new FileReader(fileName));
//            QuizFile quizFile = reader.read(QuizFile.class);
//            characters = quizFile.characters;
//            questions.put(0, "");
//            answerOptions.add(0, "");
//            testGraph = new ArrayList<>();
//            optionsIndex = new HashMap<>();
//            optionsIndex.put("", 0);
//            testGraph.add(new ArrayList<>());
//            for (var item : quizFile.questions)
//                questions.put(Integer.parseInt(item.get("id")) + 1, item.get("text"));
//
//
//            var i = 0;
//            for (var e : quizFile.answers) {
//                answerOptions.add(e.get("text"));
//                optionsIndex.put(e.get("text"), i + 1);
//                testGraph.add(new ArrayList<>());
//                int node = Integer.parseInt(e.get("from")) + 1;
//                DestinationNode nextNode = new DestinationNode(Integer.parseInt(e.get("to")) + 1,
//                        i + 1);
//                testGraph.get(node).add(nextNode);
//                i++;
//            }
//            testGraph.get(0).add(new DestinationNode(1, 0));
//
//        } catch (YamlException e)
//        {
//            e.printStackTrace();
//        }
//
//        db.connect();
//        db.initDatabase();
//    }
//
//    private int getNextQuestionIndex(int edgeIndex, int currentQuestionId) {
//        for (DestinationNode item : testGraph.get(currentQuestionId)) {
//            if (item.Edge == edgeIndex)
//                return item.Node;
//        }
//        return  0;
//    }
//
//    private List<String> getAnswersList(int currentQuestionId){
//        List<String> answersList = new ArrayList<>();
//        for (DestinationNode item : testGraph.get(currentQuestionId)){
//            answersList.add(answerOptions.get(item.Edge));
//        }
//        return answersList;
//    }
//
//    private GameDataSet getGameData(int userId) {
//        return db.getGameData(userId);
//    }
//
//
//    @Override
//    public ChatBotReply proceedRequest(String request, int userId) {
//        GameDataSet userData = getGameData(userId);
//        int currentQuestionId = userData.currentQuestionId;
//
//        if (!optionsIndex.containsKey(request)) {
//            return new ChatBotReply("Подумай ещё раз!", getAnswersList(currentQuestionId));
//        }
//        currentQuestionId = getNextQuestionIndex(optionsIndex.get(request), currentQuestionId);
//        if (testGraph.get(currentQuestionId).size() == 0) {
//            markInactive(userId);
//            String characterName = questions.get(currentQuestionId);
//            return new ChatBotReply(String.format("Всё понятно. " + characters.get(characterName).get("description"),
//                    characterName),
//                    null,
//                    characters.get(characterName).get("image"),
//                    String.format("пикси %s", characterName));
//        }
//        db.setGameData(userId, new GameDataSet(userId, currentQuestionId));
//        return new ChatBotReply(questions.get(currentQuestionId), getAnswersList(currentQuestionId));
//    }
//
//    @Override
//    public void markActive(int userId) {
//        db.markGameActive(userId);
//    }
//
//    @Override
//    public void markInactive(int userId) {
//        db.markGameInactive(userId);
//    }
//
//    @Override
//    public boolean isActive(int userId) {
//        return db.isGameActive(userId);
//    }
//
//    @Override
//    public String getInitialMessage(int userId) {
//        return "Сейчас мы узнаем, какая пикси тебе подходит.";
//    }
//}