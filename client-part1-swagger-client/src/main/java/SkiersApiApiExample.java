import com.squareup.okhttp.Response;
import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiersApiApiExample {
    private static final int TOTAL_THREADS = 32; // can adjust # threads
    private static final CountDownLatch countdownlatch = new CountDownLatch(TOTAL_THREADS);
    private static final int requests = 200000;
    private static final int qSize = 128;
    private static final BlockingQueue<LiftRideEvent> q = new LinkedBlockingQueue<>(qSize);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // 1 thread: generating event objects, put them to q - producer
        ExecutorService generationService = Executors.newSingleThreadExecutor();
        generationService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i =0; i < qSize; i++) {
                    LiftRideEvent ride = new LiftRideEvent(generateRandomNumber(1, 100000), generateRandomNumber(1, 10),
                            generateRandomNumber(1, 40), generateRandomNumber(1, 360));
                    try {
                        q.put(ride);
                        // System.out.println(ride.toString());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // 2 thread: making POST requests to server by taking event from q - consumer
        ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
        // shared variables across threads
        AtomicInteger unsuccessfulRequests = new AtomicInteger(0);

        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    int requestsCount = 0;

                    while (requestsCount < requests) {
                        try {
                            LiftRideEvent ride = q.take();
                            ApiClient client = new ApiClient();
                            client.setBasePath("http://localhost:8080/skiResort");
                            SkiersApi apiInstance = new SkiersApi(client);
                            LiftRide body = new LiftRide(); // skiers post request body: liftID, time
                            for (int j = 0; j < requests; j++) {
                                long eachRequestStartTime = System.currentTimeMillis();

                                body.setLiftID(ride.getLiftID());
                                body.setTime(ride.getTime());
                                try {

                                    apiInstance.writeNewLiftRide(body, ride.getResortID(), Integer.toString(ride.getSeasonID()),
                                            Integer.toString(ride.getDayID()), ride.getSkierID());

                                    long eachRequestEndTime = System.currentTimeMillis();
                                    long latency = eachRequestEndTime - eachRequestStartTime;
                                    countdownlatch.countDown();

                                    requestsCount ++;

                                } catch (ApiException e) {
                                    System.err.println("Exception when calling SkiersApi#POST request");
                                    unsuccessfulRequests.incrementAndGet();
                                    e.printStackTrace();
                                }
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        try {
            // Wait for all threads to complete their requests
            countdownlatch.await();
            generationService.shutdown();
            executorService.shutdown();
            // Wait for all threads to terminate with a specified timeout 10000request :20sec
            boolean terminated = executorService.awaitTermination(300, TimeUnit.SECONDS);
            if (!terminated) {
                System.out.println("ExecutorService did not terminate within the specified timeout.");
            }

            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;

            System.out.println("All threads completed their requests. Terminating threads.");
            System.out.println("total requests: " + requests + ", successful requests: " + (requests - unsuccessfulRequests.get()));
            System.out.println("total requests: " + requests + ", unsuccessful requests: " + unsuccessfulRequests.get());
            System.out.println("Total run(wall)time: " + (endTime - startTime) + " milliseconds");
            System.out.println("Total throughput in requests per second: " + ((requests) / (runTime / 1000.0)));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int generateRandomNumber(int min, int max) {
        int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
        return random_int;
    }
}