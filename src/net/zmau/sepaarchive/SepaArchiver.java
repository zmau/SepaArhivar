package net.zmau.sepaarchive;

import net.zmau.sepaarchive.datastructures.HourDataItem;
import net.zmau.sepaarchive.datastructures.Observation;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SepaArchiver {

    public static final String SITE_ADDRESS = "http://www.amskv.sepa.gov.rs/android/pregledpodataka.php?stanica=%d&fbclid=IwAR2ezkHiUXsKCwPJAtzWZotzEPBsPjzh5HUw6HUuM5UkreVMbYUOx-eFEeY" ;
    private static final String CONNECTION_STRING = "jdbc:sqlserver://localhost;integratedSecurity=true;databaseName=";
    private static final String PRODUCTION_DATABASE_NAME = "sepa";
    private static final String TEST_DATABASE_NAME = "sepa_test";
    public static final String PRODUCTION_SPREADSHEET_ID = "1d-OPrhCoqKUSCu9wXbOIfWmcw-sFh1SWru26sJFQpZw";
    public static final String TEST_SPREADSHEET_ID = "13HUUuESx75JWRNeUU5FC_r3WkYeOiUviP92i7wqPtZQ";

    private static boolean inTestMode = false;

    public static void main(String[] args) {
        SepaArchiver archiver = new SepaArchiver();

        archiver.archiveDailyData();
        //archiver.archiveMonthlyData();
    }

    public SepaArchiver(){
    }

    public static String getConnectionString(){
        return inTestMode ? CONNECTION_STRING + TEST_DATABASE_NAME : CONNECTION_STRING + PRODUCTION_DATABASE_NAME;
    }
    public static String getSpreadsheetId(){
        return inTestMode ? TEST_SPREADSHEET_ID : PRODUCTION_SPREADSHEET_ID;
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
        SepaReader reader = new SepaReader();
        try {
            DBUtil.loadPoisonMap();
            ResultSet stationSet = DBUtil.execQuery("select sepaId, name from station where following = 1");
            SheetWriter sheetWriter = new SheetWriter(inTestMode);
            while (stationSet.next()) {
                int stationToProcessId = stationSet.getInt("sepaId");
                try {
                    // if (stationToProcessId == 13) {
                    List<HourDataItem> hourDataForStation = reader.readDailyData(stationToProcessId);
                    writeDayToDatabase(hourDataForStation, stationToProcessId);
                    sheetWriter.writeTheDay(hourDataForStation, stationSet.getString("name"));
                    //}
                }catch(Exception e){
                        System.out.println(String.format("Error processing station %d : %s - %s", stationToProcessId, e.getClass().toString(), e.getMessage()));
                        e.printStackTrace();
                    }
                }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            reader.quit();
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
            if(hourDataItem.getSO2() != null) {
                Observation oSO2 = new Observation();
                oSO2.time = hourDataItem.getTime();
                oSO2.stationId = stationId;
                oSO2.componentId = 1;
                oSO2.value = hourDataItem.getSO2();
                hourlyObservations.add(oSO2);
            }

            if(hourDataItem.getPM10() != null) {
                Observation oPM10 = new Observation();
                oPM10.time = hourDataItem.getTime();
                oPM10.stationId = stationId;
                oPM10.componentId = 5;
                oPM10.value = hourDataItem.getPM10();
                hourlyObservations.add(oPM10);
            }

            if(hourDataItem.getNO2() != null) {
                Observation oNO2 = new Observation();
                oNO2.time = hourDataItem.getTime();
                oNO2.stationId = stationId;
                oNO2.componentId = 8;
                oNO2.value = hourDataItem.getNO2();
                hourlyObservations.add(oNO2);
            }
            if(hourDataItem.getCO() != null) {
                Observation oCO = new Observation();
                oCO.time = hourDataItem.getTime();
                oCO.stationId = stationId;
                oCO.componentId = 10;
                oCO.value = hourDataItem.getCO();
                hourlyObservations.add(oCO);
            }
            if(hourDataItem.getPM2comma5() != null) {
                Observation oPM2comma5 = new Observation();
                oPM2comma5.time = hourDataItem.getTime();
                oPM2comma5.stationId = stationId;
                oPM2comma5.componentId = 101;
                oPM2comma5.value = hourDataItem.getPM2comma5();
                hourlyObservations.add(oPM2comma5);
            }
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
