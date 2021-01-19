package net.zmau.sepaarchive;

import net.zmau.sepaarchive.datastructures.HourDataItem;
import net.zmau.sepaarchive.datastructures.Observation;

import java.net.UnknownHostException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SepaArchiver {

    public static final String DAILY_DATA_URL = "http://www.amskv.sepa.gov.rs/android/pregledpodataka.php?stanica=%d" ;
    public static final String MONTHLY_DATA_URL = "http://www.amskv.sepa.gov.rs/pregledpodataka.php?stanica=%d" ;
    private static final String CONNECTION_STRING = "jdbc:sqlserver://localhost;integratedSecurity=true;databaseName=";
    private static final String PRODUCTION_DATABASE_NAME = "sepa";
    private static final String TEST_DATABASE_NAME = "sepa_test";
    public static final String PRODUCTION_SPREADSHEET_ID = "1d-OPrhCoqKUSCu9wXbOIfWmcw-sFh1SWru26sJFQpZw";
    public static final String TEST_SPREADSHEET_ID = "13HUUuESx75JWRNeUU5FC_r3WkYeOiUviP92i7wqPtZQ";

    public enum TimelyMode {
        DAILY,
        MONTHLY
    }

    private static boolean inTestMode = true;
    public static TimelyMode timelyMode;
    private Logger logger;


    public static void main(String[] args) {
        if(args.length > 0 && args[0].equals("monthly2")) {
            SepaArchiver archiver = new SepaArchiver(TimelyMode.MONTHLY);
            System.out.println("starting archiver [monthly]" + (inTestMode ? " in test mode" : ""));
            archiver.archiveMonthlyData2();
        }
        else {
            SepaArchiver archiver = new SepaArchiver(TimelyMode.DAILY);
            System.out.println("starting archiver [daily]" + (inTestMode ? " in test mode" : ""));
            archiver.archiveDailyData();
        }
        //archiver.archiveMonthlyData();
    }

    public SepaArchiver(TimelyMode mode){
        logger = LoggerFactory.getLogger(SepaArchiver.class);
        this.timelyMode = mode;
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
            System.out.println("Reading data from web site, in " + (inTestMode ? "TEST MODE" : "PRODUCTION MODE"));
            SheetWriter sheetWriter = null;
            boolean canWriteToSheet = true;
            try {
                sheetWriter = new SheetWriter(inTestMode);
            }
            catch (UnknownHostException e){
                canWriteToSheet = false;
                System.out.println("Can not access oauth2.googleapis.com. Won't write to google sheet.");
            }
            catch (Exception e){
                canWriteToSheet = false;
                System.out.println("Can not write to google sheet : " + e.getMessage());
            }
            while (stationSet.next()) {
                int stationToProcessId = stationSet.getInt("sepaId");
                try {
                    System.out.println(LocalDateTime.now().toLocalTime().toString().substring(0, 8)  + "  reading data for station " + stationSet.getString("name"));
                    List<HourDataItem> hourDataForStation = reader.readDailyData(stationToProcessId);
                    System.out.println("    " + LocalDateTime.now().toLocalTime().toString().substring(0, 8)  + "  writing to database");
                    writeDayToDatabase(hourDataForStation, stationToProcessId);
                    if (canWriteToSheet) {
                        System.out.println("    " + LocalDateTime.now().toLocalTime().toString().substring(0, 8) + "  writing to spreadsheet");
                        sheetWriter.writeTheDay(hourDataForStation, stationSet.getString("name"));
                    }
                    System.out.println("    finished station");
                }
                catch(Exception e){
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

    public void archiveMonthlyData2(){
        try {
            int stationToProcessId = 70;
            SepaReader reader = new SepaReader();
//            SheetWriter sheetWriter = new SheetWriter(inTestMode);
            List<HourDataItem> hourDataForStation = reader.readMonthlyData(stationToProcessId);
            System.out.println(hourDataForStation.size());
            writeDayToDatabase(hourDataForStation, stationToProcessId);
  //          sheetWriter.writeTheDay(hourDataForStation, "Šabac mobilna");

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
                if(!e.getMessage().startsWith("Cannot insert duplicate key row")) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
