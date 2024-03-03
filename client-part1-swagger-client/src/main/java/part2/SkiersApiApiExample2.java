package part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import part1.LiftRideEvent;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiersApiApiExample2 {
    private static final int TOTAL_THREADS = 100;
    private static final CountDownLatch countdownlatch = new CountDownLatch(TOTAL_THREADS);
    private static final int requests = 2000;
    private static final int qSize = TOTAL_THREADS * requests;
    private static final BlockingQueue<LiftRideEvent> q = new LinkedBlockingQueue<>(qSize);

    public static void main(String[] args) throws IOException {
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
        FileWriter writer = new FileWriter("output.csv");

        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    // int requestsCount = 0;

                    ApiClient client = new ApiClient();
                    client.setBasePath("http://localhost:8080/skiResort");
                    //client.setBasePath("http://54.185.240.211:8080/skiResort_war");
                    SkiersApi apiInstance = new SkiersApi(client);

                    for (int j = 0; j < requests; j++) {

                    //while (requestsCount < requests) {
                        try {
                            LiftRideEvent ride = q.take();
                            LiftRide body = new LiftRide();
                            //FileWriter writer = new FileWriter("Output.csv");
                            //for (int j = 0; j < requests; j++) {
                                //int retries = 0;
                                //boolean success = false;
                                //while (retries < 5 && !success) {
                            body.setLiftID(ride.getLiftID());
                            body.setTime(ride.getTime());

                            try {
                                long eachRequestStartTime = System.currentTimeMillis();

                                ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(body, ride.getResortID(), Integer.toString(ride.getSeasonID()),
                                        Integer.toString(ride.getDayID()), ride.getSkierID());
                                int statusCode = response.getStatusCode();

                                long eachRequestEndTime = System.currentTimeMillis();

                                long latency = eachRequestEndTime - eachRequestStartTime;

                                synchronized (writer) {
                                    writer.append(String.valueOf(eachRequestStartTime))
                                            .append(",")
                                            .append("POST")
                                            .append(",")
                                            .append(String.valueOf(latency)) // milliseconds
                                            .append(",")
                                            .append(String.valueOf(statusCode))
                                            .append("\n");
                                }

                                countdownlatch.countDown();
//                                 System.out.println("Thread " + Thread.currentThread().getId() +
//                                 " - Request " + (j + 1) + " completed successfully.");
                                // requestsCount++;
                                        // success = true;

                            } catch (ApiException e) {
                                System.err.println("Exception when calling SkiersApi#POST request");
                                        //retries ++;
//                                        if (retries == 5) {
//                                            unsuccessfulRequests.incrementAndGet();
//                                            break;
//                                        }
                            }
                                //}
                            //}
                            } catch(InterruptedException | IOException e){
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
            boolean t1 = generationService.awaitTermination(20, TimeUnit.SECONDS);
            executorService.shutdown();
            boolean t2 = executorService.awaitTermination(20, TimeUnit.SECONDS);

            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

            // mean latency
            List<Long> latencies = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader("output.csv"));
            String line;
            // int count = 0;
            long latencySum = 0;
            while ((line = br.readLine()) != null) { // && count != requests
                String[] values = line.split(",");
                if (values.length >= 3) {
                    if (isParsableAsLong(values[2].trim())) {
                        long num = Long.parseLong(values[2].trim());
                        latencies.add(num);
                        latencySum += num;
                    }
                }
                // count ++;
            }

            // median
            Collections.sort(latencies);
            int n = latencies.size();
            double median;
            if (n % 2 == 0) {
                median = (latencies.get(n / 2 - 1) + latencies.get(n / 2)) / 2.0;
            } else {
                median = latencies.get(n / 2);
            }
            // 99th percentile
            int index = (int) Math.floor(0.99 * n);

            System.out.println("All threads completed their requests. Terminating threads.");
            System.out.println("mean response time (millisecs): " + latencySum / qSize);
            System.out.println("median response time (millisecs): " + median );
            System.out.println("Total throughput in requests per second: " + ((qSize) / (runTime / 1000.0)));
            System.out.println("p99 (99th percentile response time (millisecs)): " + latencies.get(index));
            System.out.println("min and max response time (millisecs): " + latencies.get(0) + " " + latencies.get(n - 1));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int generateRandomNumber(int min, int max) {
        int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
        return random_int;
    }

    private static boolean isParsableAsLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}