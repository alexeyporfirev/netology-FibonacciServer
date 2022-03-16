package server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для вычисления чисел Фибоначчи
 */
public class FibonacciCalculator {

    // Хранение вычисленных ранее значений для будущих вычислений.
    // В качестве значений для ключа n хранит n-ое и (n-1)-ое значение чисел Фибоначчи
    private final Map<Long, long[]> results = new ConcurrentHashMap<Long, long[]>();

    /**
     * Вычисление заданного числа Фибоначчи с использованием вычисленных ранее значений
     * @param n Число, для которого вычисляется число Фибоначчи
     * @return число Фибоначчи
     */
    public long getResult(long n) {
        long nearest;
        long res = -1;
        if (results.get(n) != null) {
            res = results.get(n)[0];
        } else if ((nearest = findNearest(results.keySet(), n)) != -1) {
            res = getFibonnaciFromNearest(n, nearest);
        } else {
            res = getFibonacciValue(n);
        }
        return res;
    }


    /**
     * Нахождение ближайшего наименьшего числа к заданному числу во множестве вычисленных значений
     * @param set Множество с вычисленными значениями
     * @param value Заданное число
     * @return Ближайшее наименьшее число к заданному числу
     */
    private long findNearest(Set<Long> set, long value) {
        long nearest = -1;
        long range = Integer.MAX_VALUE;
        for (Long arg : set) {
            if (value - arg > 0 && range > value - arg) {
                range = value - arg;
                nearest = arg;
            }
        }
        return nearest;
    }

    /**
     * Прямое вычисление заданного числа Фибоначчи
     * @param n Число, для которого вычисляется число Фибоначчи
     * @return число Фибоначчи
     */
    private long getFibonacciValue(long n) {
        if(n == 0) return 0;
        else if (n == 1) return 1;
        else {
            long a = 0;
            long b = 1;
            long res = 0;
            for (long i = 2; i <= n; i++) {
                res = a + b;
                a = b;
                b = res;
            }
            results.put(n, new long[]{b, a});
            return res;
        }
    }

    /**
     * Вычисление заданного числа Фибоначчи с использованием ближайших к переданному числу ранее вычисленных значений
     * @param n Число, для которого вычисляется число Фибоначчи
     * @param nearest Ближайшее к переданному числу ранее вычисленное значение
     * @return число Фибоначчи
     */
    private long getFibonnaciFromNearest(long n, long nearest) {
        long a = results.get(nearest)[1];
        long b = results.get(nearest)[0];
        long res = b;
        for (long i = nearest + 1; i <= n; i++) {
            res = a + b;
            a = b;
            b = res;
        }
        results.put(n, new long[]{b, a});
        return res;
    }

}
