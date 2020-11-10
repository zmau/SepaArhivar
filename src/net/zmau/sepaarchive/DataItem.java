package net.zmau.sepaarchive;

public abstract class DataItem {
    protected java.util.Calendar time;
    protected Float SO2;
    protected Float PM10;
    protected Float CO;
    protected Float NO2;
    protected Float PM2comma5;

    public java.util.Calendar getTime(){
        return time;
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
}
