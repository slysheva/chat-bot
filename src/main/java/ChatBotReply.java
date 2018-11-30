import java.util.List;

class ChatBotReply {
    String message;
    List<String> keyboardOptions;
    String imageUrl;
    String characterName;

    ChatBotReply(String message) {
        this.message = message;
        keyboardOptions = null;
        imageUrl = null;
        characterName = null;
    }

    ChatBotReply(String message, List<String> options) {
        this.message = message;
        keyboardOptions = options;
        imageUrl = null;
        characterName = null;
    }

    ChatBotReply(String message, String imageUrl, String characterName) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.characterName = characterName;
    }
}
