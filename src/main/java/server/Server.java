package server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервер для вычисления чисел Фибоначчи
 */
public class Server {

    // для хранения буферов отдельных соединений
    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();
    // для вычисления и хранения значений чисел Фибоначчи
    private static final FibonacciCalculator calculator = new FibonacciCalculator();

    public static void main(String[] args) throws IOException {
        // запускаем сервер
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost", 12345));
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        // переключаем в режим ожидания подключений
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        log("Server start");
        try {
            while (true) {
                selector.select();
                for (SelectionKey event : selector.selectedKeys()) {
                    if (event.isValid()) {
                        try {
                            if (event.isAcceptable()) {
                                // если состояние "подключился"
                                SocketChannel socketChannel = serverChannel.accept();
                                socketChannel.configureBlocking(false);
                                log("New connection from " + socketChannel.getRemoteAddress());
                                sockets.put(socketChannel, ByteBuffer.allocate(Long.BYTES));
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            } else if (event.isReadable()) {
                                // если состояние "готов читать"
                                SocketChannel socketChannel = (SocketChannel) event.channel();
                                ByteBuffer buffer = sockets.get(socketChannel);
                                int bytesCount = socketChannel.read(buffer);
                                log("Reading from " + socketChannel.getRemoteAddress() + ", " +
                                        "bytes read=" + bytesCount);
                                if (bytesCount == -1) {
                                    log("Connection closed " + socketChannel.getRemoteAddress());
                                    sockets.remove(socketChannel);
                                    socketChannel.close();
                                }
                                log("The received value is " + new BigInteger(buffer.array()).longValue());
                                buffer.clear();
                                if(bytesCount > 0) {
                                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                                }
                            } else if (event.isWritable()) {
                                // если состояние "готов записывать"
                                SocketChannel socketChannel = (SocketChannel) event.channel();
                                ByteBuffer buffer = sockets.get(socketChannel);
                                // переводим массив байт в long
                                long number = new BigInteger(buffer.array()).longValue();
                                long result = calculator.getResult(number);
                                log("The calculated value is " + result);
                                buffer.putLong(result);

                                int bytesWritten = socketChannel.write(
                                        ByteBuffer.wrap(
                                                buffer.array()));
                                buffer.clear();

                                log("Writing to " + socketChannel.getRemoteAddress() +
                                        ", bytes written=" + bytesWritten);
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            // закрываем соединение, если подключение было разорвано с ошибкой
                            log("error " + e.getMessage());
                            SocketChannel socketChannel = (SocketChannel) event.channel();
                            sockets.remove(socketChannel);
                            socketChannel.close();
                        }
                    }
                }
                // очищаем состояния
                selector.selectedKeys().clear();;
            }
        } catch (IOException e) {
            log("error " + e.getMessage());
        } finally {
            // выключаем сервер
            serverChannel.close();
        }
    }

    /**
     * Логирование сообщений
     * @param msg Сообщение для логирования
     */
    private static void log(String msg) {
        System.out.printf("[%s][%s] %s\n",
                Thread.currentThread().getName(),
                new Timestamp(System.currentTimeMillis()),
                msg);
    }


}