public interface IGame {
    String getWelcomeMessage();
    String getInitialMessage();

    String proceedRequest(String request);

    void markActive();
    void markInactive();

    boolean isActive();
}
