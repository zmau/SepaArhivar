package net.zmau.sepaarchive.datastructures;

import net.zmau.sepaarchive.datastructures.DataItem;

import java.time.LocalDateTime;
import java.util.List;

public class HourDataItem extends DataItem {

    public boolean isAfter(LocalDateTime moment){
        return this.time.isAfter(moment);
    }

    public void setValue(List<String> componentsToFollow, int index, String value){
        switch (componentsToFollow.get(index)){
            case "SO2" : setSO2(value); break;
            case "PM10" : setPM10(value); break;
            case "NO2" : setNO2(value); break;
            case "CO" : setCO(value); break;
            case "PM2.5" : setPM2comma5(value); break;
        }
    }
    public void setTime(String Time){
        time = LocalDateTime.now()
            .withMonth (Integer.parseInt(Time.substring(3, 5)))
            .withDayOfMonth (Integer.parseInt(Time.substring(0, 2)))
            .withHour(Integer.parseInt(Time.substring(7, 9)))
            .withMinute(0).withSecond(0).withNano(0);
    }
    public void setSO2(String text){
        try {
            SO2 = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            SO2 = null;
        }
    }
    public void setPM10(String text){
        try {
            PM10 = Float.parseFloat(text);
        }
        catch (NumberFormatException e){
            PM10 = null;
        }
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
