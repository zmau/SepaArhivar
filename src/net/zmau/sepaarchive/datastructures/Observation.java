package net.zmau.sepaarchive.datastructures;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Observation {
    public LocalDateTime time;
    public int stationId;
    public int componentId;
    public float value;

    public Observation(){

    }
    public Observation(String time, int stationId, int componentId, Float value){
        this.time = LocalDateTime.now()
                .withMonth (Integer.parseInt(time.substring(3, 5)))
                .withDayOfMonth (Integer.parseInt(time.substring(0, 2)))
                .withHour(Integer.parseInt(time.substring(7, 9)))
                .withMinute(0).withSecond(0).withNano(0);

        this.stationId = stationId;
        this.componentId = componentId;
        this.value = value;
    }
    public String insertFieldsCSV(){
        return String.format("('%s', %d, %d, %f)", time.toString().replace("T", " "), stationId, componentId, value);
    }
}
