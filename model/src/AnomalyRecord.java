import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
public class AnomalyRecord {
    private int AnomalyID;
    private int DroneID;
    private Instant timestamp;
    private String type;
    private String Details;

    public AnomalyRecord(int AnomalyID,
                         int DroneID, Instant timestamp, String type, String Details) {

        this.AnomalyID = AnomalyID;
        this.DroneID = DroneID;
        this.timestamp = timestamp;
        this.type = type;
        this.Details = Details;
    }

    public int getAnomalyID() {
        return AnomalyID;
    }

    public int getDroneID() {
        return DroneID;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return Details;
    }

    public String toCSVRow() {

        return "";
    }

}
