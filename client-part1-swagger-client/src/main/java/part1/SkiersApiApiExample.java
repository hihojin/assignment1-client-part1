package part1;

import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiersApiApiExample {

    // Total requests = Number of threads × Requests per thread == qsize
    //= 100 × 2000
    //= 200,000

    private static final int TOTAL_THREADS = 100;
    private static final int requests = 2000;
    private static final CountDownLatch countdownlatch = new CountDownLatch(TOTAL_THREADS * requests);
    private static final int qSize = TOTAL_THREADS * requests;
    private static final BlockingQueue<LiftRideEvent> q = new LinkedBlockingQueue<>(qSize);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // 1 thread: generating event objects, put them to q - producer
        ExecutorService generationService = Executors.newSingleThreadExecutor();
        generationService.submit(new EventGenerationThread(qSize, q));

        // 2 thread: making POST requests to server by taking event from q - consumer
        ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
        // shared variables across threads
        AtomicInteger unsuccessfulRequests = new AtomicInteger(0);

        ApiClientPool clientPool = new ApiClientPool(TOTAL_THREADS);

        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    SkiersApi client = clientPool.borrowClient();
                    // client.setBasePath("http://localhost:8080/Assignment2SkierServer_war_exploded");

                    for (int j = 0; j < requests; j++) { // requests per thread
                        try {
                            LiftRideEvent ride = q.take();
                            LiftRide body = new LiftRide(); // skiers post request body: liftID, time

                            body.setLiftID(ride.getLiftID());
                            body.setTime(ride.getTime());

                            client.writeNewLiftRideWithHttpInfo(body, ride.getResortID(), Integer.toString(ride.getSeasonID()),
                                    Integer.toString(ride.getDayID()), ride.getSkierID());
                            countdownlatch.countDown();
                        } catch (InterruptedException | ApiException e) {
                            System.err.println("Exception when calling SkiersApi#POST request");
                            unsuccessfulRequests.incrementAndGet();
                        }
                    }
                    clientPool.returnClient(client);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        try {
            // Wait for all threads to complete their requests
            countdownlatch.await();
            generationService.shutdown();
            boolean t1 = generationService.awaitTermination(3000, TimeUnit.SECONDS);
            executorService.shutdown();
            boolean t2 = executorService.awaitTermination(3000, TimeUnit.SECONDS);
            if (!t1) {
                System.out.println("t1 shutdown problem");
            }
            if (!t2) {
                System.out.println("t2 shutdown problem");
            }

            if (!executorService.isTerminated()) {
                System.out.println("Some threads in executorService did not terminate.");
            }

            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;

            System.out.println("All threads completed their requests. Terminating threads.");
            System.out.println("total requests: " + qSize + ", successful requests: " + (qSize  - unsuccessfulRequests.get()));
            System.out.println("total requests: " + qSize + ", unsuccessful requests: " + unsuccessfulRequests.get());
            System.out.println("Total run(wall)time: " + runTime + " milliseconds");
            System.out.println("Total throughput in requests per second: " + (qSize / (runTime / 1000.0)));

        } catch (InterruptedException e) {
            System.out.println("something went wrong in try block");
        }
    }
}