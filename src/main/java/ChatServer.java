import javax.imageio.plugins.tiff.ExifInteroperabilityTagSet;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;


// Чат сервер делаем слушателем, implements TCPConnectionListener
// Мы теперь являемся и чат сервером и TCPConnectionListener
public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        // сервер может принимать входящие соединение, может держать несколько соединений активными
        // и сможет рассылать сообщения
        // ServerSocket - умеет слушать входящее соединение, принимать его, создавать объект сокета
        // связанного с этим соединением, готовый сокет нам отдавать
        // Socket - установка соединения
        new ChatServer();
    }


    // лист для хранения соединений
    private final List<TCPConnection> connectionsList = new ArrayList<>();


    //private ChatServer(){ // можно сделать приватным
    public ChatServer() {
        System.out.println("Server running...");
        // Данный класс слушает порт и принимает соединение, в данном случае это 8189
        // используем try с ресурсами, что бы не закрывать serverSocket
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            // реализация работы сервера
            // реализуем бесконечный цикл в котором сервер будет принимать входящее соединение
            // реализуем без расчета остановки, просто запустили, он работает
            while (true) {
                // пишем еще один try который будет ловить не посредственно исключения при подключении клиентов
                try {
                    // на каждое новое соединение нужно создать новый TCPConnection
                    // мы как слушатели должны передать себя и объект сокета
                    // объект сокета при входящем соединении возвращает метод accept()
                    // метод accept() ждет нового соединения, и как только это соединение установилось,
                    // возвращает объект сокета, готовый который с ним связан, с этом соединением.
                    new TCPConnection(this, serverSocket.accept());

                } catch (IOException e) {
                    // в случае если, что то случиться при подключении клиента мы это просто будем логировать
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


    // Обработка наших событий
    //-------1. Синхронизируем все методы, что бы нельзя было из разных потоков в них попасть

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        // когда Connection готов мы его добавляем в список соединений
        connectionsList.add(tcpConnection);
        // лог, если у на соединение готово, всех оповестим
        // !! когда мы складываем объект со строчкой, у объекта не явно вызывается метод toString
        // в классе TCPConnection мы переопределили метод toString, так что получаем строчку
        sendToAllConnection("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        // если мы строчку приняли, нам ее нужно разослать всем клиентам
        sendToAllConnection(value);

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        // если tcpConnection отвалился, мы из списка соединений его удаляем
        connectionsList.remove(tcpConnection);
        sendToAllConnection("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        // если случилось исключение, мы его выводи в консоль, !! надо в логер
        System.out.println("TCPConnection exception " + e);

    }

    // Отдельный метод, для того чтобы, разослать сообщение всем клиентам
    private void sendToAllConnection(String value){
        // строчку которую отправляем просто логируем в нашу консоль, что бы всем ее видеть
        System.out.println(value);
        // проходимся по списку соединений, и отправляем всем строчку value
        final int sizeList = connectionsList.size();
        for (int i = 0; i < sizeList; i++) {
            connectionsList.get(i).sendString(value);
        }
    }
}
