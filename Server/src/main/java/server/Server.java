package server;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Класс содержащий метод запуска сервера
 */
final public class Server {
    /**
     * Точка входа в программу
     *
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    /**
     * Запуск сервера
     */
    public void start() {
        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/api/messages/create", new MyHandler());
            httpServer.setExecutor(null);
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}