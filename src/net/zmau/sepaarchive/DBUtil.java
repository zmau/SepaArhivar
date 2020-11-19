package net.zmau.sepaarchive;

import net.zmau.sepaarchive.datastructures.Observation;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

import static net.zmau.sepaarchive.SepaArchiver.getConnectionString;

public class DBUtil {

    public static HashMap<String, Integer> poisonMap;
    public static void loadPoisonMap() throws SQLException{
        poisonMap = new HashMap<>();
        ResultSet poisonSet = execQuery("select name, sepaId from component");
        while (poisonSet.next()){
            String code = poisonSet.getString("name");
            if(code.equals("PM2comma5"))
                code = "PM2.5";
            poisonMap.put(code, poisonSet.getInt("sepaId"));
        }
    }
    public static Connection getConnection(){
        try {
            return DriverManager.getConnection(getConnectionString());
        }
        catch (SQLException e){
            return null;
        }
    }

    public static void execSQL(String query) throws SQLException{
        Statement s = getConnection().createStatement();
        s.executeUpdate(query);
    }

    public static ResultSet execQuery(String query) throws SQLException{
        Statement s = getConnection().createStatement();
        return s.executeQuery(query);
    }

    public static void insertObservations(List<Observation> observationList) throws SQLException{
        if(observationList.isEmpty())
            return;
        StringBuilder batchInsert = new StringBuilder( "insert into observation values ");
        for(Observation observation : observationList){
            batchInsert.append(observation.insertFieldsCSV() + ",");
        }
        String insertScript = batchInsert.substring(0, batchInsert.length()-1);
        execSQL(insertScript);
    }

}
