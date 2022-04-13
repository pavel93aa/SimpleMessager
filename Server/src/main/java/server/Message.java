package server;

import java.util.Date;

/**
 * Класс описывающий полное сообщение
 */
final public class Message extends ClientMessage {
    private final int id;
    private final Date date;

    public Message(ClientMessage clientsMessage, int id, Date date) {
        super(clientsMessage.getAuthor(), clientsMessage.getText());

        this.id = id;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Message{" +
                "author='" + getAuthor() + '\'' +
                ", text='" + getText() + '\'' +
                ", id=" + id +
                ", date=" + date +
                '}';
    }
}