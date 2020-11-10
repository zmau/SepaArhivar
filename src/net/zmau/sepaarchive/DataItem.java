package net.zmau.sepaarchive;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public abstract class DataItem {
    protected LocalDateTime time;
    protected Float SO2;
    protected Float PM10;
    protected Float CO;
    protected Float NO2;
    protected Float PM2comma5;

    public String getDate(){
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
    public LocalDateTime getTime(){
        return time;
    }
    public int getHour(){
        return time.getHour();
    }
    public Float getSO2(){
        return SO2;
    }
    public Float getPM10(){
        return PM10;
    }
    public Float getNO2(){
        return NO2;
    }
    public Float getCO(){
        return CO;
    }
    public Float getPM2comma5(){
        return PM2comma5;
    }

    public String getSO2AsString(){
        return SO2 == null ? " " : SO2.toString();
    }
    public String getPM10AsString(){
        return PM10 == null ? " " : PM10.toString();
    }
    public String getNO2AsString(){
        return NO2 == null ? " " : NO2.toString();
    }
    public String getCOAsString(){
        return CO == null ? " " : CO.toString();
    }
    public String getPM2comma5AsString(){
        return PM2comma5 == null ? " " : PM2comma5.toString();
    }
}
