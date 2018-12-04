public interface IGame {
    String getInitialMessage(int quizId);

    ChatBotReply proceedRequest(String request, int userId);

    boolean start(int userId, int quizId);
    void stop(int userId);

    boolean isActive(int userId);
}
