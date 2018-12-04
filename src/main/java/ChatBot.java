import database.DatabaseWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChatBot {
    private QuizRunner runner;
    private DatabaseWorker db;

    // TODO: Подумать, как можно вынести все эти строки, нужно ли это
    protected final String quizNotActive = "Игра ещё не началась. Чтобы посмотреть " +
                                           "список доступных опросов, напиши команду /start";
    protected final String quizEnded = "Игра закончена. Чтобы начать заново, напиши /start";
    protected final String nextQuiz = "Чтобы пройти следующий тест, напиши /start";
    protected final String quizActive = "Игра уже идёт. Чтобы остановить, напиши /stop";
    protected final String start = "Привет! Чтобы пройти опрос, выбери его из списка. Чтобы добавить новый опрос, " +
                                   "напиши /add";
    protected final String addQuiz = "Чтобы добавить новый опрос, пришли мне его в виде текстового файла";
    protected final String quizParseError = "Произошла ошибка во время обработки файла. Попробуй ещё раз";
    protected final String quizAdded = "Опрос успешно добавлен!";
    protected final String unrecognized = "Сообщение не распознано. Попробуй ещё раз";
    protected final String quizzesList = "Вот список доступных опросов:";
    protected final String quizNotFound = "Опрос на найден. Попробуйте пройти другой";

    protected final Pattern quizSelection = Pattern.compile("[0-9]+:[ A-Za-zА-Яа-я?,.-]+");

    ChatBot() {
        runner = new QuizRunner();
        db = new DatabaseWorker();
        db.connect();
    }

    ChatBotReply answer(String message, int userId) {
        switch (message.toLowerCase()) {
            case "/start":
            case "старт":
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                return new ChatBotReply(start, getQuizzesList());
            case "/add":
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                else
                    return new ChatBotReply(addQuiz);
            case "/list":
                return new ChatBotReply(quizzesList, getQuizzesList());
            case "/stop":
            case "стоп":
                if (!runner.isActive(userId))
                    return new ChatBotReply(quizNotActive);
                runner.stop(userId);
                return new ChatBotReply(quizEnded);
            default:
                if (runner.isActive(userId)) {
                    ChatBotReply reply = runner.proceedRequest(message, userId);
                    if (reply.imageUrl == null)
                        return reply;
                    else {
                        return new ChatBotReply(reply.message + '\n' + nextQuiz,
                                reply.imageUrl, reply.shareText);
                    }
                }
                else {
                    Matcher m = quizSelection.matcher(message);
                    if (m.matches()) {
                        int quizId = Integer.parseInt(message.split(":")[0]);
                        if (!runner.start(userId, quizId))
                            return new ChatBotReply(quizNotFound, getQuizzesList());
                        ChatBotReply firstQuestion = runner.proceedRequest("", userId);
                        return new ChatBotReply(runner.getInitialMessage(quizId) +
                                '\n' + firstQuestion.message, firstQuestion.keyboardOptions);
                    }
                    else
                        return new ChatBotReply(unrecognized);
                }
        }
    }

    ChatBotReply addQuiz(String content) {
        try {
            if (content == null)
                return new ChatBotReply(quizParseError);
            Quiz quiz = new Quiz(content);
            db.addQuiz(Serializer.serialize(quiz));
        } catch (QuizException e) {
            return new ChatBotReply(quizParseError);
        }
        return new ChatBotReply(quizAdded);
    }

    List<String> getQuizzesList() {
        var quizzes = db.getQuizzesList();
        List<String> options = new ArrayList<>();
        for (var e: quizzes) {
            options.add(String.format("%s: %s", e.getFirst(), e.getSecond()));
        }
        return options;
    }
}
