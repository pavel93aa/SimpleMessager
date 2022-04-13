package client;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс описывающий полное сообщение
 */
final public class Message extends ClientMessage {
    private int id;
    private Date date;

    public Message() {
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        // Раскомментировать в случае надобности отображения времени в UTC
        // simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date) + " " + getAuthor() + ": " + getText();
    }
}