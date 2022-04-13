package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Класс содержащий метод запуска сервера
 */
final public class Server {
    private final String serverPort;
    private static final List<Message> messages = new ArrayList<>();
    private static int messageIdCounter;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public Server(String serverPort) {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.serverPort = serverPort;
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
    private void start() {
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

            ObjectMapper objectMapper = new ObjectMapper();
            ClientMessage clientMessage = objectMapper.readValue(request, ClientMessage.class);
            Date date = null;
            try {
                date = simpleDateFormat.parse((simpleDateFormat.format(new Date())));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Message message = new Message(clientMessage, ++messageIdCounter, date);

            // Добавление объекта Message в список сообщений
            messages.add(message);

            objectMapper.setDateFormat(simpleDateFormat);
            String response = objectMapper.writeValueAsString(message);

            // Вывод полученного сообщения
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
            Map<String, String> clientRequestParameters = Server.queryToMap(httpExchange.getRequestURI().getQuery());

            // Вывод параметров запроса клиента
            System.out.println(clientRequestParameters);

            // Получение messageID из отображения
            Integer messageId = Server.extractMessageId(clientRequestParameters);

            String response;
            int statusCode = 200;

            if (messageId == null) {
                response = "Ошибка";
                statusCode = 400;
            } else {
                List<Message> newMessages = new ArrayList<>();

                // Получение всех сообщений, id которых больше чем значение параметра message_id
                for (Message message : messages) if (messageId < message.getId()) newMessages.add(message);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setDateFormat(simpleDateFormat);

                response = objectMapper.writeValueAsString(newMessages);
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
}