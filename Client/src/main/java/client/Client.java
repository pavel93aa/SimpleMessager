package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

/**
 * Класс описывающий клиента
 */
final public class Client {
    private final String serverSchema;
    private final String serverIP;
    private final String serverPort;
    private String author;
    private int maxMessageId;

    private Client(String serverSchema, String serverIP, String serverPort) {
        this.serverSchema = serverSchema;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Точка входа в программу
     */
    public static void main(String[] args) {
        Client client = new Client("http", "192.168.0.154", "8080");
        client.start();
    }

    /**
     * Ввод автора и текста сообщения
     */
    private void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите автора: ");
        this.author = scanner.nextLine();

        // Создание потока
        Thread thread = new MyThread(this);

        // Запуск потока
        thread.start();

        // Ввод текста сообщения
        while (true) {
            this.sendMessage(scanner.nextLine());
        }
    }

    /**
     * Отправка сообщения
     *
     * @param text текст сообщения
     */
    private void sendMessage(String text) {
        ClientMessage clientMessage = new ClientMessage(this.author, text);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] input = new byte[0];
        try {
            input = objectMapper.writeValueAsString(clientMessage).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        URL url = null;
        try {
            url = new URL(serverSchema + "://" + serverIP + ":" + serverPort + "/api/messages/create");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) Objects.requireNonNull(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Objects.requireNonNull(httpURLConnection).setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(httpURLConnection).setRequestProperty("Content-Type", "application/json; utf-8");
        httpURLConnection.setDoOutput(true);

        //Запись в поток OutputStream отправляемого сообщения
        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(input, 0, input.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Получение ответа от сервера
        getResponse(httpURLConnection);
    }

    /**
     * Отправка запроса для получения последнего сообщения
     */
    void getMessage() {
        URL url = null;
        try {
            url = new URL(serverSchema + "://" + serverIP + ":" + serverPort + "/api/messages/get?message_id=" + maxMessageId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) Objects.requireNonNull(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Objects.requireNonNull(httpURLConnection).setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(httpURLConnection).setRequestProperty("Content-Type", "application/json; utf-8");
        httpURLConnection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        Message[] messages;
        try {
            messages = objectMapper.readValue(Objects.requireNonNull(getResponse(httpURLConnection)), Message[].class);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        // Получение последнего сообщения
        if (messages.length != 0) {
            int tempMaxId = 0;
            for (Message message : messages) {
                if (message.getId() > tempMaxId) {
                    tempMaxId = message.getId();
                }
                // Вывод последнего сообщения
                System.out.println(message);
            }
            maxMessageId = tempMaxId;
        }
    }

    /**
     * Получение ответа от сервера
     *
     * @param httpURLConnection объект соединения по сетевому протоколу НТТР
     * @return ответ сервера
     */
    private static String getResponse(HttpURLConnection httpURLConnection) {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}