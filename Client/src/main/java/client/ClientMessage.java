package client;

/**
 * Класс описывающий клиентское сообщение
 */
public class ClientMessage {
    private String author;
    private String text;

    public ClientMessage() {
    }

    public ClientMessage(String author, String text) {
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
        return "ClientMessage{" +
                "author='" + author + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}