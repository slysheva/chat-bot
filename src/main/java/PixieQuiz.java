import database.DatabaseWorker;
import database.GameDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class PixieQuiz implements IGame {
    private  ArrayList<String> questions = new ArrayList<>();
    private int questionsCount, answersCount;
    private ArrayList<String> answerOptions = new ArrayList<>();
    private ArrayList< ArrayList<DestinationNode> > testGraph;
    private Map<String, Integer> optionsIndex;
    private Map<String, String> charactersImages;

    private DatabaseWorker db = new DatabaseWorker();

    PixieQuiz(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner sc = new Scanner(file);
        parseQuizRules(sc);
        parseQuizData(sc);
        sc.close();

        db.connect();
        db.initDatabase();
    }

    private void parseQuizRules(Scanner sc) {
        String[] countInfo =  sc.nextLine().split("\\s+");
        questionsCount = Integer.parseInt(countInfo[0]);
        answersCount = Integer.parseInt(countInfo[1]);
    }

    private void parseQuizData(Scanner sc) {
        questions.add("");
        for (int i = 0; i < questionsCount; ++i)
            questions.add(sc.nextLine());
        testGraph = new ArrayList<>();
        optionsIndex = new HashMap<>();
        charactersImages = new HashMap<>();
        testGraph.add(new ArrayList<>());
        answerOptions.add("");
        optionsIndex.put("", 0);
        for (int i = 0; i < answersCount; ++i) {
            String answerOption = sc.nextLine();
            answerOptions.add(answerOption);
            optionsIndex.put(answerOption, i + 1);
            testGraph.add(new ArrayList<>());
        }
        for (int i = 0; i < answersCount; ++i) {
            String[] edgeInfo = sc.nextLine().split(" ");
            int node = Integer.parseInt(edgeInfo[0]) + 1;
            DestinationNode nextNode = new DestinationNode(Integer.parseInt(edgeInfo[1]) + 1,
                        Integer.parseInt(edgeInfo[2]) + 1);
            testGraph.get(node).add(nextNode);
        }
        testGraph.get(0).add(new DestinationNode(1, 0));

        while (sc.hasNextLine()) {
            String[] character = sc.nextLine().split(" ");
            charactersImages.put(character[0], character[1]);
        }
    }

    private int getNextQuestionIndex(int edgeIndex, int currentQuestionId) {
        for (DestinationNode item : testGraph.get(currentQuestionId)) {
            if (item.Edge == edgeIndex)
                return item.Node;
        }
        return  0; //так себе так делать
    }

    private List<String> getAnswersList(int currentQuestionId){
        List<String> answersList = new ArrayList<>();
        for (DestinationNode item : testGraph.get(currentQuestionId)){
            answersList.add(answerOptions.get(item.Edge));
        }
        return answersList;
    }

    private GameDataSet getGameData(int userId) {
        return db.getGameData(userId);
    }


    @Override
    public ChatBotReply proceedRequest(String request, int userId) {
        GameDataSet userData = getGameData(userId);
        int currentQuestionId = userData.currentQuestionId;

        if (!optionsIndex.containsKey(request)) {
            return new ChatBotReply("Подумай ещё раз!", getAnswersList(currentQuestionId));
        }
        currentQuestionId = getNextQuestionIndex(optionsIndex.get(request), currentQuestionId);
        if (testGraph.get(currentQuestionId).size() == 0) {
            markInactive(userId);
            String characterName = questions.get(currentQuestionId);
            return new ChatBotReply(String.format("Всё понятно. Твоя пикси %s",
                    characterName),
                    null,
                    charactersImages.get(characterName),
                    String.format("пикси %s", characterName));
        }
        db.setGameData(userId, new GameDataSet(userId, currentQuestionId));
        return new ChatBotReply(questions.get(currentQuestionId), getAnswersList(currentQuestionId));
    }

    @Override
    public void markActive(int userId) {
        db.markGameActive(userId);
    }

    @Override
    public void markInactive(int userId) {
        db.markGameInactive(userId);
    }

    @Override
    public boolean isActive(int userId) {
        return db.isGameActive(userId);
    }

    @Override
    public String getInitialMessage(int userId) {
        return "Сейчас мы узнаем, какая пикси тебе подходит.";
    }
}