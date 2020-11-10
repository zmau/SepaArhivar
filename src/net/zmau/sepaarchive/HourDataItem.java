package net.zmau.sepaarchive;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class HourDataItem extends DataItem {
    public void setTime(String Time){
        time = new GregorianCalendar();
        time.set(Calendar.DATE, Integer.parseInt(Time.substring(0, 1)));
        time.set(Calendar.MONTH, Integer.parseInt(Time.substring(3, 4)));
        time.set(Calendar.YEAR, Calendar.getInstance().getTime().getYear());
        time.set(Calendar.HOUR, Integer.parseInt(Time.substring(7, 8)));
    }
    public void setSO2(String text){
        try {
            SO2 = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            SO2 = null;
        }
    };
    public void setPM10(String text){
        PM10 = Float.parseFloat(text);
    }
    public void setNO2(String text){
        try {
            NO2 = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            NO2 = null;
        }
    }
    public void setCO(String text){
        try {
            CO = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            CO = null;
        }
    }
    public void setPM2comma5(String text){
        try {
            PM2comma5 = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            PM2comma5 = null;
        }
    }
}
