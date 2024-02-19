package part1;

public class LiftRideEvent {
    private int skierID;
    private int resortID;
    private int liftID;
    private int seasonID;
    private int dayID;
    private int time;

    public LiftRideEvent(int skierID, int resortID, int liftID, int time) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.liftID = liftID;
        this.time = time;
        this.seasonID = 2024;
        this.dayID = 1;
    }

    @Override
    public String toString() {
        return "lift ride info: skierid, resortid, liftid, time, season, dayid " + this.skierID + ' ' +
                this.resortID + ' ' + this.liftID + ' ' + this.time + ' ' + this.seasonID + ' ' + this.dayID;
    }

    public int getSkierID() {
        return this.skierID;
    }

    public int getResortID() {
        return this.resortID;
    }

    public int getLiftID() {
        return this.liftID;
    }

    public int getSeasonID() {
        return this.seasonID;
    }

    public int getDayID() {
        return this.dayID;
    }

    public int getTime() {
        return this.time;
    }
}
