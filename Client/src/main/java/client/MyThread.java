package client;

/**
 * Класс описывающий новый поток
 */
final public class MyThread extends Thread {
    private final Client client;

    public MyThread(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        // Запрос сообщений у сервера
        for (; ; ) {
            try {
                this.client.getMessage();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}