package net.zmau.sepaarchive;

import java.util.Calendar;

public class DayDataItem extends DataItem {

    public void setDate(Calendar time){
        time.set(Calendar.HOUR, 0);
        this.time = time;
    }
    public void setSO2(float value){
        SO2 = value;
    };
    public void setPM10(float value){
        PM10 = value;
    }
    public void setNO2(float value){
        NO2 = value;
    }
    public void setCO(float value){
        CO = value;
    }
    public void setPM2comma5(float value){
        PM2comma5 = value;
    }

}
