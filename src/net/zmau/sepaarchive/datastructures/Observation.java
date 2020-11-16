package net.zmau.sepaarchive.datastructures;

import java.time.LocalDateTime;

public class Observation {
    public LocalDateTime time;
    public int stationId;
    public int componentId;
    public float value;

    public String insertFieldsCSV(){
        return String.format("('%s', %d, %d, %f)", time.toString().replace("T", " "), stationId, componentId, value);
    }
}
