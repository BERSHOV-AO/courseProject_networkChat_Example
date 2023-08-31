// слушатель TCP соеденения
public interface TCPConnectionListener {

    // событие готового соеденения, передаем туда экземпляр самого соеденения TCPConnection
    void onConnectionReady(TCPConnection tcpConnection);
    // мы можем внезапно принять строчку/ в обработчике нам будет интересно узнать что за строчку мы приняли
    void onReceiveString(TCPConnection tcpConnection, String value);
    // может случится дисконект
    void onDisconnect(TCPConnection tcpConnection);
    // ожет случится исключение
    void onException(TCPConnection tcpConnection, Exception e);

}
