package net.zmau.sepaarchive;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class DayData {
    private Calendar date;
    private List<HourDataItem> hourData;

    public DayData(){
        date = new GregorianCalendar();
        hourData = new ArrayList<HourDataItem>();
    }
    public void AddHourData(HourDataItem hourDataItem){
        hourData.add(hourDataItem);
    }
    public DayDataItem getDailyAvgs(){
        DayDataItem dailyAvg = new DayDataItem();
        dailyAvg.setDate(hourData.get(0).getTime());
        dailyAvg.setSO2((float) hourData.stream().mapToDouble(x -> x.getSO2()).average().getAsDouble());
        dailyAvg.setPM10((float) hourData.stream().mapToDouble(x -> x.getPM10()).average().getAsDouble());
        dailyAvg.setNO2((float) hourData.stream().mapToDouble(x -> x.getNO2()).average().getAsDouble());
        dailyAvg.setCO((float) hourData.stream().mapToDouble(x -> x.getCO()).average().getAsDouble());
        dailyAvg.setPM2comma5((float) hourData.stream().mapToDouble(x -> x.getPM2comma5()).average().getAsDouble());
        return dailyAvg;
    }
    public DayDataItem getDailyPeaks(){
        DayDataItem dailyAvg = new DayDataItem();
        dailyAvg.setDate(hourData.get(0).getTime());
        dailyAvg.setSO2((float) hourData.stream().mapToDouble(x -> x.getSO2()).max().getAsDouble());
        dailyAvg.setPM10((float) hourData.stream().mapToDouble(x -> x.getPM10()).max().getAsDouble());
        dailyAvg.setNO2((float) hourData.stream().mapToDouble(x -> x.getNO2()).max().getAsDouble());
        dailyAvg.setCO((float) hourData.stream().mapToDouble(x -> x.getCO()).max().getAsDouble());
        dailyAvg.setPM2comma5((float) hourData.stream().mapToDouble(x -> x.getPM2comma5()).max().getAsDouble());
        return dailyAvg;
    }
}
