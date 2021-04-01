import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrimeCalculator {
    public static void main(String[] args) throws InterruptedException {
        int maxPrime = 2_000;
        List<Integer> numbers = Collections.synchronizedList(getNumbers(maxPrime));
        List<Integer> primes = getPrimes(numbers, maxPrime);
        Collections.sort(primes);
        primes.stream().map(prime -> prime + "\n").forEach(System.out::print);
    }

    private static List<Integer> getNumbers(int maxPrime) {
        return Stream.generate(new Supplier<Integer>() {
            int i = 2;

            @Override
            public Integer get() {
                return i++;
            }
        }).limit(maxPrime).collect(Collectors.toList());
    }

    private static List<Integer> getPrimes(List<Integer> numbers, int maxPrime) throws InterruptedException {
        List<Integer> primeNumbers = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(maxPrime);
        ExecutorService executors = Executors.newFixedThreadPool(Math.min(maxPrime / 100, 100));
        for (Integer candidate : numbers) {
            executors.submit(() -> {
                if (isPrime(candidate)) {
                    primeNumbers.add(candidate);
                }
                latch.countDown();
            });
        }
        latch.await();
        executors.shutdownNow();

        return primeNumbers;
    }

    private static boolean isPrime(int candidate) {
        for (int i = 2; i < candidate / 2; i++) {
            if (candidate % i == 0) {
                return false;
            }
        }
        return true;
    }
}
