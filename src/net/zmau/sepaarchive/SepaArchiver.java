package net.zmau.sepaarchive;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import net.zmau.sepaarchive.dal.Station;
import net.zmau.sepaarchive.datastructures.HourDataItem;
import net.zmau.sepaarchive.datastructures.Observation;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SepaArchiver {

    public static void main(String[] args) {
        SepaArchiver archiver = new SepaArchiver();

        archiver.archiveDailyData();
        //archiver.archiveMonthlyData();
    }

    public SepaArchiver(){
    }

    public void archiveMonthlyData(){
        try {
            LocalDateTime time0 = LocalDateTime.now();
            System.out.println("starting at " + time0.toLocalTime());
            MonthArchiver archiver = new MonthArchiver();
            archiver.archiveData();
            LocalDateTime time1 = LocalDateTime.now();
            Duration duration = Duration.between(time0, time1);
            System.out.println("finished at " + time1.toLocalTime() + "; " + duration.getSeconds() + " seconds");
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void archiveDailyData(){
        try {
            SepaReader reader = new SepaReader();
            List<HourDataItem> hourData = parsedData(reader.readDailyData());
            writeDayToDatabase(hourData, 70);
            SheetWriter writer = new SheetWriter();
            writer.writeTheDay(hourData);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private List<HourDataItem> parsedData(String cellularData){
        List<HourDataItem> result = new ArrayList<>();
        String[] rows = cellularData.split("\n");
        for(int i = 6; i < rows.length; i++){
            HourDataItem observation = parsedLine(rows[i]);
            if(observation != null)
                result.add(observation);
        }
        return result;
    }
    private HourDataItem parsedLine(String line){
        String[] cell = line.split(" ");
        if (cell.length < 7)
            return null;

        HourDataItem result = new HourDataItem();
        result.setTime(cell[0] + " " + cell[1]);
        if(cell.length > 2)
            result.setSO2(cell[2]);
        if(cell.length > 3)
            result.setPM10(cell[3]);
        if(cell.length > 4)
            result.setNO2(cell[4]);
        if(cell.length > 5)
            result.setCO(cell[5]);
        if(cell.length > 6)
            result.setPM2comma5(cell[6]);
        return result;
    }

    private void writeDayToDatabase(List<HourDataItem> dailyData, int stationId){
        for(HourDataItem hourDataItem : dailyData){
            List<Observation> hourlyObservations = new ArrayList<>();
            Observation oSO2 = new Observation();
            oSO2.time = hourDataItem.getTime();
            oSO2.stationId = stationId;
            oSO2.componentId = 1;
            oSO2.value = hourDataItem.getSO2();
            hourlyObservations.add(oSO2);

            Observation oPM10 = new Observation();
            oPM10.time = hourDataItem.getTime();
            oPM10.stationId = stationId;
            oPM10.componentId = 5;
            oPM10.value = hourDataItem.getPM10();
            hourlyObservations.add(oPM10);

            Observation oNO2 = new Observation();
            oNO2.time = hourDataItem.getTime();
            oNO2.stationId = stationId;
            oNO2.componentId = 8;
            oNO2.value = hourDataItem.getNO2();
            hourlyObservations.add(oNO2);

            Observation oCO = new Observation();
            oCO.time = hourDataItem.getTime();
            oCO.stationId = stationId;
            oCO.componentId = 10;
            oCO.value = hourDataItem.getCO();
            hourlyObservations.add(oCO);

            Observation oPM2comma5 = new Observation();
            oPM2comma5.time = hourDataItem.getTime();
            oPM2comma5.stationId = stationId;
            oPM2comma5.componentId = 101;
            oPM2comma5.value = hourDataItem.getSO2();
            hourlyObservations.add(oPM2comma5);

            try {
                DBUtil.insertObservations(hourlyObservations);
            }
            catch (SQLException e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
