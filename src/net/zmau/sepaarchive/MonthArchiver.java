package net.zmau.sepaarchive;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.zmau.sepaarchive.datastructures.Observation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonthArchiver {
    private static String MONTHLY_URL_TEMPLATE = "http://www.amskv.sepa.gov.rs/konektori/pregled_tabela_uporedni.php?stanice[]=%s&komponente[]=%s&periodi[]=dana30&agregacija[]=1";
    private Connection con;

    private String stationCSV, componentCSV;
    private List<Integer> stationList, componentList;

    public MonthArchiver() throws SQLException{
        stationList = new ArrayList<Integer>();
        componentList = new ArrayList<>();
    }

    public void archiveData() throws SQLException{
        readStationsAndComponents();
        readMonthlyData();
    }
    // station 11-20 2020-11-12 puklo
    private void readStationsAndComponents() throws SQLException{
            String SQL = "SELECT sepaId FROM station where following = 1 and sepaId between 70 and 79 order by sepaid";
            ResultSet rs = DBUtil.execQuery(SQL);

            StringBuilder stations = new StringBuilder();
            while (rs.next()) {
                stations.append("," + rs.getString("sepaId"));
                stationList.add(rs.getInt("sepaId"));
            }
            stationCSV = stations.substring(1);

            SQL = "SELECT sepaId FROM component order by sepaid";
            rs = DBUtil.execQuery(SQL);

            StringBuilder components = new StringBuilder();
            while (rs.next()) {
                components.append("," + rs.getString("sepaId"));
                componentList.add(rs.getInt("sepaId"));
            }
            componentCSV = components.substring(1);
    }

    private void readMonthlyData(){
        StringBuilder batchInsert = new StringBuilder( "insert into observation values ");
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        try {
            String monthDataURL = String.format(MONTHLY_URL_TEMPLATE, stationCSV, componentCSV);
            System.out.println(monthDataURL);
            driver.get(monthDataURL);
            WebElement table = driver.findElement(By.id("pregledtabela"));

            List<WebElement> rows = table.findElements(By.tagName("tr"));
            int counter = 0;
            int currentProcessedDate = -1;
            for (int i = 0; i < rows.size(); i++) {
                List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
                if (cells.size() > 0) {
                    LocalDateTime time = LocalDateTime.parse(cells.get(0).getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if(time.getDayOfMonth() != currentProcessedDate){
                        currentProcessedDate = time.getDayOfMonth();
                        System.out.println(LocalDateTime.now().toLocalTime().toString().substring(0, 8) + " : processing " + time.toLocalDate());
                    }
                    for (int si = 0; si < stationList.size(); si++) {
                        for (int ci = 0; ci < componentList.size(); ci++) {
                            String observationText = cells.get(ci*stationList.size() + si + 1).getText();
                            if(!observationText.isEmpty()) {
                                Observation observation = new Observation();
                                observation.time = time;
                                observation.componentId = componentList.get(ci);
                                observation.stationId = stationList.get(si);
                                observation.value = Float.parseFloat(observationText);
                                batchInsert.append(observation.insertFieldsCSV() + ",");
                                counter++;
                                if(counter == 1000){
                                    String insertScript = batchInsert.substring(0, batchInsert.length()-1);
                                    System.out.println(insertScript);
                                    DBUtil.execSQL(insertScript);
                                    counter = 0;
                                    batchInsert = new StringBuilder( "insert into observation values ");
                                }
                            }
                        }

                    }
                }
            }
            //System.out.println(counter + "  observations");
            String insertScript = batchInsert.substring(0, batchInsert.length()-1);
            System.out.println(insertScript);
            DBUtil.execSQL(insertScript);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            driver.quit();
        }

    }
}
