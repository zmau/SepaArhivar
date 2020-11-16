package net.zmau.sepaarchive;

import net.zmau.sepaarchive.datastructures.Observation;

import java.sql.*;
import java.util.List;

public class DBUtil {
    private static final String CONNECTION_STRING = "jdbc:sqlserver://localhost;integratedSecurity=true;databaseName=sepa";

    public static Connection getConnection(){
        try {
            return DriverManager.getConnection(CONNECTION_STRING);
        }
        catch (SQLException e){
            return null;
        }
    }

    public static void execSQL(String query) throws SQLException{
        Statement s = getConnection().createStatement();
        s.executeUpdate(query);
    }

    public static void insertObservations(List<Observation> observationList) throws SQLException{
        StringBuilder batchInsert = new StringBuilder( "insert into observation values ");
        for(Observation observation : observationList){
            batchInsert.append(observation.insertFieldsCSV() + ",");
        }
        String insertScript = batchInsert.substring(0, batchInsert.length()-1);
        execSQL(insertScript);
    }
}
