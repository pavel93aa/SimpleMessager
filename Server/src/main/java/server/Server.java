package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

        //Обработчик создания сообщения (маршрут /api/messages/create)
        HttpHandler handlerOfCreatingMessage = (httpExchange) -> {
            InputStream inputStream = httpExchange.getRequestBody();
            String request = null;
            try {
                request = new BufferedReader(new InputStreamReader(inputStream)).readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        };

        //Обработчик получения сообщения (маршрут /api/messages/get)
        HttpHandler handlerOfGettingMessage = (httpExchange) -> {
            String response = "This is the server response";
            System.out.println(response);

            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                outputStream.write(response.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        //Передача обработчиков в методы createContext() для каждого из маршрутов
        Objects.requireNonNull(httpServer).createContext("/api/messages/create", handlerOfCreatingMessage);
        httpServer.createContext("/api/messages/get", handlerOfGettingMessage);
        httpServer.setExecutor(null); // по умолчанию один поток
        httpServer.start();
        System.out.println("Server started on port " + Integer.parseInt(serverPort));
    }
}