import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    // в одном tcp соединение должен быть сокет с которым он связан.
    private final Socket socket;
    // должен быть поток/ он будет слушать входящее соединение, постоянно читать поток ввода,
    // если строчка прилетела, то будет событие
    private final Thread rxThread;
    // слушатель событий
    private final TCPConnectionListener evenListener;
    // потоки ввода вывода нужны// читать байт, отправить массив байт.
    // мы делаем TCP конекшен который будет работать со строками
    private final BufferedReader in;
    private final BufferedWriter out;

    // этот конструктор на создание сокета внутри
    public TCPConnection(TCPConnectionListener evenListener, String ipAdres, int port) throws IOException {
        // вызываем из этого конструктора, другой конструктор
        // создаем прямо тут сокет на основании нашего ip и порта
        this(evenListener, new Socket(ipAdres, port));
    }

    // этот конструктор отвечает за то что кто то снаружи создаст сокет
    // в данном случае, пусть тот кто создает соединение, позаботится передать в аргументы TCPConnectionListener
    public TCPConnection(TCPConnectionListener evenListener, Socket socket) throws IOException {
        // переменная слушателя событий
        this.evenListener = evenListener;
        this.socket = socket;
        // далее у сокета нужно получить входящий и исходящий поток что бы принимать какие то байты и писать какие
        // то байты, для этого есть два метода getInputStream(), getOutputStream()
        // Charset.forName("UTF-8") - ставим кодировку
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        // запускаем поток, который у нас будет слушать все входящее

        // rx это ресив(устойчивый) поток который слушает входящие соединение
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // в методе run мы должны слушать входящее соединение
                // читаем строку
                // цель --> написать клас TCP конекшен один раз, для использования в серверной и клиентской части
                // передаем себя(this), иначе просто this передаст анонимный клас runnable,
                // с хитростью - передаем экземпляр обрамляющего класса TCPConnection.this
                evenListener.onConnectionReady(TCPConnection.this);
                // дальше мы должны получать строчку, и сделать это должны в бесконечном цикле
                // пока поток не прерван !rxThread.isInterrupted()

                try {
                    while (!rxThread.isInterrupted()) {
                        // пока поток не прерван, мы получаем строчку, и отдаем ее evenListener void onReceiveString
                        //String msg = in.readLine();
                        // соответственно передаем туда объект нашего соединения, и нашу строчку
                        //evenListener.onReceiveString(TCPConnection.this,msg);
                        // упрощает конструкцию
                        evenListener.onReceiveString(TCPConnection.this, in.readLine());
                    }

                } catch (IOException e) {
                    evenListener.onException(TCPConnection.this, e);
                } finally {
                    // если секция try закончилась, про причини исключения или еще какой, поток был прерван
                    // говорим слушателю событий onDisconnect
                    evenListener.onDisconnect(TCPConnection.this);


                }
            }
        });
        rxThread.start();
    }


    // Данные методы мы синхронизируем что бы безопасно к ним обращаться из разных потоков
    // метод отправить сообщение, метод спрашивает строчку которую мы хотим отправить String value
    public synchronized void sendString(String value) {
        // попробуем поток вывода методом write написать
        try {
            // нужно добавить символ конца строки  "\r\n" (возврат каретки, и перенос строки), иначе мы не поймем текст
            out.write(value + "\r\n");
            // так как out использует BufferedWriter,(может просто записаться в буфер, и по сели не передаться)
            // мы должны использовать out.flush(), он протолкнет из буфера и отправить(сбросит буфер)
            out.flush();
        } catch (IOException e) {
            // мы лиснера оповестили о нашем событии
            evenListener.onException(TCPConnection.this, e);
            // и сделали  disconnect(), так как что то пошло не так
            disconnect();
        }
    }

    // метод оборвать соединение, что бы мы с наружи могли порвать соединение
    public synchronized void disconnect() {
        rxThread.interrupt(); // команда остановки потока
        try {
            socket.close(); // закрыть сокет
        } catch (IOException e) {
            // и передаем слушателю событий исключение
            evenListener.onException(TCPConnection.this, e);
        }
    }

    // и для логов нам нужен метод toString
    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
