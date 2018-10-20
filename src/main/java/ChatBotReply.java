import java.util.List;

class ChatBotReply {
    String message;
    List<String> keyboardOptions;
    String imageName;

    ChatBotReply(String message, List<String> options) {
        this.message = message;
        keyboardOptions = options;
        imageName = null;
    }

    ChatBotReply(String message, List<String> options, String imageName) {
        this.message = message;
        keyboardOptions = options;
        this.imageName = imageName;
    }
}
