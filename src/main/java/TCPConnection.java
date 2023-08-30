import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    // в одном tcp соеденении должен быть сокет с которым он связан.
    private final Socket socket;
    // должен быть поток/ он будет слушать входящее соеденение, постоянно читать поток ввода,
    // если строчка прилетела, то будет событие
    private final Thread rxThread;
    // потоки ввода вывода нужны// читать байт, отправить массив байт.
    // мы делаем TCP конекшен который будет работать со строками
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        // далее у сокета нужно получить входящий и исходящий поток что бы принимать какие то байты и писать какие
        // то байты, для этого есть два метода getInputStream(), getOutputStream()
        // Charset.forName("UTF-8") - ставим кодировку
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        // запускаем поток, который у нас будет слушать все входящее

        // rx это ресив(устойчивый) поток который слушает входящие соеденения
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });


    }
}
