package client;

/**
 * Класс описывающий сообщение
 */
final public class Message {
    private final String author;
    private final String text;

    public Message(final String author, final String text) {
        this.author = author;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "author='" + author + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}