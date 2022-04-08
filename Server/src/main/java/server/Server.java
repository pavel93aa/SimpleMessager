package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Класс содержащий метод запуска сервера
 */
final public class Server {
    private final String serverPort;

    private static final List<Message> messages = new ArrayList<>();
    private static int idCounter;

    public Server(String serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Сохранение значений параметров key и value полученного клиентского запроса в отображение
     *
     * @param query строка формата key1=value1&keyN=valueN
     * @return отображение со значениями параметров полученного клиентского запроса
     */
    private static Map<String, String> queryToMap(String query) {
        if (query == null) return null;
        Map<String, String> resultMap = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) resultMap.put(entry[0], entry[1]);
            else resultMap.put(entry[0], "");
        }
        return resultMap;
    }

    /**
     * Валидация и извлечение переданного клиентом параметра
     *
     * @param map переданные параметры пользователя в запросе
     * @return id сообщения отправленного клиентом, после которого будут возвращены все сообщения
     */
    private static Integer extractMessageId(Map<String, String> map) {
        // Проверка что ключ message_id есть
        if (!map.containsKey("message_id")) return null;

        // Нужна проверка, что параметр message_id должен быть только один в теле запроса

        // Проверка, что тип целое число
        int messageId;
        try {
            messageId = Integer.parseInt(map.get("message_id"));
        } catch (NumberFormatException e) {
            System.out.println("Значение параметра message_id - не число");
            return null;
        }

        // Проверка, что число положительное
        if (messageId < 0) return null;
        return messageId;
    }

    /**
     * Точка входа в программу
     */
    public static void main(String[] args) {
        Server server = new Server("8080");
        server.start();
    }

    /**
     * Запуск сервера
     */
    public void start() {
        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(Integer.parseInt(serverPort)), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Обработчик создания сообщения (маршрут /api/messages/create)
        HttpHandler handlerOfCreatingMessage = (httpExchange) -> {
            InputStream inputStream = httpExchange.getRequestBody();
            String request = null;
            try {
                request = new BufferedReader(new InputStreamReader(inputStream)).readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Здесь было бы неплохо прикрутить валидацию полученного сообщения на соответствие json-формату

            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(request));
            Message message = new Message(jsonObject, ++idCounter, new Date());

            // Добавление объекта Message в список
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
        };

        // Обработчик получения сообщения (маршрут /api/messages/get)
        HttpHandler handlerOfGettingMessage = (httpExchange) -> {

            // Преобразование параметра запроса клиента в отображение
            Map<String, String> params = Server.queryToMap(httpExchange.getRequestURI().getQuery());
            System.out.println(params);

            // Получение messageID из отображения
            Integer messageId = Server.extractMessageId(params);

            String response;
            int statusCode = 200;

            if (messageId == null) {
                response = "Ошибка";
                statusCode = 400;
            } else {
                List<Message> newMessages = new ArrayList<>();

                // Получение всех сообщений, id которых больше чем значение параметра message_id
                for (Message message : messages) {
                    if (messageId < message.getId()) newMessages.add(message);
                }

                JSONArray jsonArray = new JSONArray(newMessages);
                System.out.println(jsonArray);
                response = jsonArray.toString();
            }

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                httpExchange.sendResponseHeaders(statusCode, response.getBytes().length);
                outputStream.write(response.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // Передача обработчиков в методы createContext() для каждого из маршрутов
        Objects.requireNonNull(httpServer).createContext("/api/messages/create", handlerOfCreatingMessage);
        httpServer.createContext("/api/messages/get", handlerOfGettingMessage);
        httpServer.setExecutor(null); // По умолчанию один поток
        httpServer.start();
        System.out.println("Server started on port " + Integer.parseInt(serverPort));
    }
}