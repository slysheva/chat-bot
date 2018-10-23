import java.util.List;

class ChatBotReply {
    String message;
    List<String> keyboardOptions;
    String imageUrl;

    ChatBotReply(String message, List<String> options) {
        this.message = message;
        keyboardOptions = options;
        imageUrl = null;
    }

    ChatBotReply(String message, List<String> options, String imageUrl) {
        this.message = message;
        keyboardOptions = options;
        this.imageUrl = imageUrl;
    }
}
