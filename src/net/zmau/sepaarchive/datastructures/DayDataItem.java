package net.zmau.sepaarchive.datastructures;

import net.zmau.sepaarchive.datastructures.DataItem;

import java.time.LocalDateTime;

public class DayDataItem extends DataItem {

    public void setDate(LocalDateTime time){
        this.time = time.withHour(0);
    }
    public void setSO2(Float value){
        SO2 = value;
    };
    public void setPM10(Float value){
        PM10 = value;
    }
    public void setNO2(Float value){
        NO2 = value;
    }
    public void setCO(Float value){
        CO = value;
    }
    public void setPM2comma5(Float value){
        PM2comma5 = value;
    }

}
