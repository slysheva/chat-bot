import database.DatabaseWorker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChatBot {
    private QuizRunner runner;
    private DatabaseWorker db;

    protected final String quizNotActive = "Игра ещё не началась. Чтобы посмотреть " +
            "список доступных опросов, напиши команду /start";
    protected final String quizEnded = "Игра закончена. Чтобы начать заново, напиши /start";
    protected final String nextQuiz = "Чтобы пройти следующий тест, напиши /start";
    protected final String quizActive = "Игра уже идёт. Чтобы остановить, напиши /stop";
    protected final String start = "Привет! Выберите нужную опцию меню.";
    protected final String addQuiz = "Чтобы добавить новый опрос, пришли мне его в виде текстового файла";
    protected final String quizParseError = "Произошла ошибка во время обработки файла. %s";
    protected final String quizAdded = "Опрос успешно добавлен!";
    protected final String unrecognized = "Сообщение не распознано. Попробуй ещё раз";
    protected final String quizzesList = "Вот список доступных опросов:";
    protected final String quizNotFound = "Опрос на найден. Попробуйте пройти другой";
    protected final String invited = "Привет! Давай пройдём опрос, который тебе прислал твой друг.\n\n";
    protected final String btnAddQuiz = "Добавить опрос";
    protected final String btnListQuiz = "Список опросов";
    protected final String btnAddAdmin = "Добавить админа";
    protected final String btnCancel = "Отмена";
    protected final String stateAddAdmin = "add-admin";
    protected final String addAdmin = "Пришлите id нового администратора.";
    protected final String incorrectAdminId = "Пожалуйста, укажите корректный id.";
    protected final String returnToHome = "Возвращаемся в главное меню.";
    protected final String adminAdded = "Администратор добавлен.\n";

    protected final Pattern quizSelection = Pattern.compile("[0-9]+:[ A-Za-zА-Яа-я?,.-]+");

    protected final List<String> adminKeyboard = Arrays.asList(btnListQuiz, btnAddQuiz, btnAddAdmin);
    protected final List<String> userKeyboard = Arrays.asList(btnListQuiz, btnAddQuiz);
    protected final List<String> cancelKeyboard = Collections.singletonList(btnCancel);

    protected HashMap<Long, String> state = new HashMap<>();

    ChatBot(String botUsername, DatabaseWorker db) {
        runner = new QuizRunner(botUsername, db);
        this.db = db;
        this.db.connect();
    }

    ChatBotReply answer(String message, long userId) {
        if (state.containsKey(userId)) {
            if (message.equals(btnCancel)) {
                state.remove(userId);
                return new ChatBotReply(returnToHome, getKeyboard(userId));
            }
            switch (state.get(userId)) {
                case stateAddAdmin:
                    try {
                        long adminId = Long.parseLong(message);
                        db.addAdmin(adminId);
                        state.remove(userId);
                        return new ChatBotReply(adminAdded + returnToHome, getKeyboard(userId));
                    }
                    catch (NumberFormatException e) {
                        return new ChatBotReply(incorrectAdminId, cancelKeyboard);
                    }
                default:
                    state.remove(userId);
            }
        }

        switch (message) {
            case "/start":
            case "старт":
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                return new ChatBotReply(start, getKeyboard(userId));
            case "/add":
            case btnAddQuiz:
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                else
                    return new ChatBotReply(addQuiz);
            case "/list":
            case btnListQuiz:
                return new ChatBotReply(quizzesList, getQuizzesList());
            case "/stop":
            case "стоп":
                if (!runner.isActive(userId))
                    return new ChatBotReply(quizNotActive);
                runner.stop(userId);
                return new ChatBotReply(quizEnded);
            case btnAddAdmin:
                state.put(userId, stateAddAdmin);
                return new ChatBotReply(addAdmin, cancelKeyboard);
            default:
                if (runner.isActive(userId)) {
                    ChatBotReply reply = runner.proceedRequest(message, userId);
                    if (reply.imageUrl == null)
                        return reply;
                    else {
                        return new ChatBotReply(reply.message + '\n' + nextQuiz,
                                reply.imageUrl, reply.shareText);
                    }
                } else {
                    Matcher m = quizSelection.matcher(message);
                    if (m.matches()) {
                        int quizId = Integer.parseInt(message.split(":")[0]);
                        return startQuiz(userId, quizId, false);
                    } else if (message.startsWith("/start")) {
                        try {
                            int quizId = Integer.parseInt(message.split(" ")[1]);
                            return startQuiz(userId, quizId, true);
                        } catch (Exception e) {
                            return new ChatBotReply(start, getQuizzesList());
                        }
                    } else
                        return new ChatBotReply(unrecognized);
                }
        }
    }

    ChatBotReply startQuiz(long userId, int quizId, boolean fromInvite) {
        if (!runner.start(userId, quizId))
            return new ChatBotReply(quizNotFound, getQuizzesList());
        ChatBotReply firstQuestion = runner.proceedRequest("", userId);
        if (fromInvite)
            return new ChatBotReply(invited + runner.getInitialMessage(quizId) +
                    '\n' + firstQuestion.message, firstQuestion.keyboardOptions);
        else
            return new ChatBotReply(runner.getInitialMessage(quizId) +
                    '\n' + firstQuestion.message, firstQuestion.keyboardOptions);
    }

    ChatBotReply addQuiz(String content) {
        try {
            if (content == null)
                return new ChatBotReply(quizParseError);
            Quiz quiz = new Quiz(content, db);
            quiz.checkValidity();
            db.addQuiz(Serializer.serialize(quiz));
        } catch (QuizException e) {
            return new ChatBotReply(String.format(quizParseError, e.message));
        }
        return new ChatBotReply(quizAdded);
    }

    List<String> getQuizzesList() {
        var quizzes = db.getQuizzesList();
        List<String> options = new ArrayList<>();
        for (var e : quizzes) {
            options.add(String.format("%s: %s", e.getFirst(), e.getSecond()));
        }
        return options;
    }

    List<String> getKeyboard(long userId) {
        return db.isAdmin(userId) ? adminKeyboard : userKeyboard;
    }
}