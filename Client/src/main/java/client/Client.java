package client;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private void sendMessage(final String text) {
        Message message = new Message(this.author, text);
        JSONObject jsonObject = new JSONObject(message);

        try {
            URL url = new URL(serverSchema + "://" + serverIP + ":" + serverPort + "/api/messages/create");

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setDoOutput(true);

            try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = bufferedReader.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}