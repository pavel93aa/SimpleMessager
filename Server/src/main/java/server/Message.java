package server;

import org.json.JSONObject;

import java.util.Date;

/**
 * Класс описывающий сообщение
 */
final public class Message {
    private final String author;
    private final String text;
    private final int id;
    private final Date date;

    public Message(final JSONObject jsonObject, final int id, final Date date) {
        this.author = String.valueOf(jsonObject.get("author"));
        this.text = String.valueOf(jsonObject.get("text"));
        this.id = id;
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
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
                "author='" + author + '\'' +
                ", text='" + text + '\'' +
                ", id=" + id +
                ", date=" + date +
                '}';
    }
}