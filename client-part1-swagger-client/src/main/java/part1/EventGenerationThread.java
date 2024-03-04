package part1;

import java.util.concurrent.BlockingQueue;

public class EventGenerationThread implements Runnable{

    private final int minSkierID;
    private final int maxSkierID;
    private final int minResortID;
    private final int maxResortID;
    private final int minLiftID;
    private final int maxLiftID;
    private final int minTime;
    private final int maxTime;

    private int qSize;
    private BlockingQueue<LiftRideEvent> q;
    public EventGenerationThread(int qSize, BlockingQueue<LiftRideEvent> q) {
        this.qSize = qSize;
        this.q = q;
        this.minSkierID = 1;
        this.maxSkierID = 100000;
        this.minResortID = 1;
        this.maxResortID = 10;
        this.minLiftID = 1;
        this.maxLiftID = 40;
        this.minTime = 1;
        this.maxTime = 360;
    }

    @Override
    public void run() {
        for (int i =0; i < this.qSize; i++) {
            LiftRideEvent ride = new LiftRideEvent(
                    generateRandomNumber(this.minSkierID, this.maxSkierID),
                    generateRandomNumber(minResortID, maxResortID),
                    generateRandomNumber(minLiftID, maxLiftID),
                    generateRandomNumber(minTime, maxTime));
            try {
                this.q.put(ride);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static int generateRandomNumber(int min, int max) {
        int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
        return random_int;
    }
}
