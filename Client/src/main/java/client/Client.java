package client;

import org.json.JSONObject;

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

    public Client(String serverSchema, String serverIP, String serverPort) {
        this.serverSchema = serverSchema;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Точка входа в программу
     *
     * @param args
     */
    public static void main(String[] args) {
        Client client = new Client("http", "192.168.0.154", "8080");
        client.start();
    }

    /**
     * Ввод текста сообщения
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите автора: ");
        this.author = scanner.nextLine();
        while (true) {
            System.out.print("Введите текст: ");
            String text = scanner.nextLine();
            this.sendMessage(text);
        }
    }

    /**
     * Отправка сообщения
     *
     * @param text текст сообщения
     */
    private void sendMessage(final String text) {
        Message message = new Message(this.author, text);
        JSONObject jsonObject = new JSONObject(message);
        byte[] input = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

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

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}