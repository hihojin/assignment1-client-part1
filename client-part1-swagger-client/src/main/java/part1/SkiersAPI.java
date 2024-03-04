package part1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiersAPI implements Runnable{
    private final SkiersApi apiInstance;
    private final AtomicInteger unsuccessfulRequests;
    private final LiftRide body;
    private final LiftRideEvent ride;
    private int statusCode;

    public SkiersAPI(SkiersApi apiInstance, AtomicInteger unsuccessfulRequests, LiftRide body, LiftRideEvent ride) {
        this.apiInstance = apiInstance;
        this.unsuccessfulRequests = unsuccessfulRequests;
        this.body = body;
        this.ride = ride;
    }

    // Getter for the status code
    public int getLastStatusCode() {
        return statusCode;
    }

    @Override
    public void run() {
        //for (int j = 0; j < this.requests; j++) {
//            try {
//                LiftRideEvent ride = q.take();
//                LiftRide body = new LiftRide(); // skiers post request body: liftID, time
//
//                body.setLiftID(ride.getLiftID());
//                body.setTime(ride.getTime());
                try {
                    ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(body, ride.getResortID(), Integer.toString(ride.getSeasonID()),
                            Integer.toString(ride.getDayID()), ride.getSkierID());
                    statusCode = response.getStatusCode();
                    // this.countdownLatch.countDown();
                    // System.out.println("Thread " + Thread.currentThread().getId() + " " + (j+1) + " times completed successfully.");

                } catch (ApiException e) {
                    System.err.println("Exception when calling SkiersApi#POST request");
                    this.unsuccessfulRequests.incrementAndGet();
                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        //}
    }
}
