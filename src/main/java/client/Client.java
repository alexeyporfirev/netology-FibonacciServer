package client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Клиент для запроса чисел Фибоначчи
 */
public class Client {

    public static void main(String[] args) throws IOException {
        //устанавливаем соединение
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 12345);
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(socketAddress);


        try (Scanner scanner = new Scanner(System.in)) {
            final ByteBuffer inputBuffer = ByteBuffer.allocate(Long.BYTES);
            String msg;
            long number;
            // пока не введем end читаем и отправляем на сервер числа
            while (true) {
                System.out.println("Enter number for calculation...");
                msg = scanner.nextLine();
                if ("end".equals(msg))
                    break;

                // работаем с типом Long
                number = Long.parseLong(msg);
                inputBuffer.putLong(number);
                socketChannel.write(
                        ByteBuffer.wrap(
                                inputBuffer.array()));
                inputBuffer.clear();
                // ждем 1 с
                Thread.sleep(1000);
                // читаем ответ сервера
                int bytesCount = socketChannel.read(inputBuffer);
                //выводим результат на экран
                // переводим массив байт в long
                System.out.printf("Result: %d\n", new BigInteger(inputBuffer.array()).longValue());
                inputBuffer.clear();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //закрываем соединение
            socketChannel.close();
        }
    }

}
