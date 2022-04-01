package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Класс описывающий обработку http запроса
 */
final public class MyHandler implements HttpHandler {
    private static final List<Message> messages = new ArrayList<>();
    private static int idCounter;

    /**
     * Обработка http запроса
     *
     * @param httpExchange
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String request = new BufferedReader(new InputStreamReader(inputStream)).readLine();

        //Здесь было бы неплохо прикрутить валидацию полученного сообщения на соответствие json-формату

        JSONObject jsonObject = new JSONObject(request);
        Message message = new Message(jsonObject, ++idCounter, new Date());

        //Добавление объекта Message в список
        messages.add(message);

        jsonObject = new JSONObject(message);
        String response = jsonObject.toString();
        System.out.println(response);

        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            outputStream.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}