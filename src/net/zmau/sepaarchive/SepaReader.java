package net.zmau.sepaarchive;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.zmau.sepaarchive.datastructures.HourDataItem;
import net.zmau.sepaarchive.datastructures.Observation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static net.zmau.sepaarchive.SepaArchiver.SITE_ADDRESS;

public class SepaReader {
    private WebDriver driver;
    private List<Observation> dailyObservationsForStation;

    public SepaReader(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }
    public List<HourDataItem>  readDailyData(int stationId) throws Exception {
        String exactURLForStation = String.format(SITE_ADDRESS, stationId);
        driver.get(exactURLForStation);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("ucitajgrafikon('satni24h','TabTop1');");
        String bodyHTML = driver.findElement(By.tagName("body")).getAttribute("innerHTML");

        List<HourDataItem> result = new ArrayList<>();
        dailyObservationsForStation = new ArrayList<>();
        Document body = Jsoup.parse(bodyHTML);
        Element table = body.getElementById("pregledtabela");
        List<String> componentList = getHeader(table);
        List<Element> rows =  table.getElementsByTag("tr");

        for (int rowNo = 0; rowNo < rows.size(); rowNo++) {
            List<Element> cells = rows.get(rowNo).getElementsByTag("td");
            if (cells.size() > 0) {
                HourDataItem item = new HourDataItem();
                item.setTime(cells.get(0).text());
                /*for(int columnNo = 1; columnNo < cells.size(); columnNo++) {
                    item.setValue(componentList, columnNo, cells.get(columnNo).text());

                    int poisonId = DBUtil.poisonMap.get(componentList.get(columnNo));
                    try {
                        float value = Float.parseFloat(cells.get(columnNo).text());
                        dailyObservationsForStation.add(new Observation(cells.get(0).text(), stationId, poisonId, value));
                    }
                    catch (NumberFormatException e){
                        // nothing; probably value is empty, just skip it, does not matter
                    }
                }*/
                result.add(item);
            }
        }
        return result;
    }

    private List<String> getHeader(Element table){
        Element header = table.getElementsByTag("thead").get(0);
        List<Element> titles = header.getElementsByTag("th");
        List<String> result = new ArrayList<>();
        for(Element title : titles){
            String wholeTitle = title.html();
            int newLineIndex = wholeTitle.indexOf("<br>");
            if(newLineIndex > 0)
                result.add(wholeTitle.substring(0, newLineIndex));
            else result.add(wholeTitle);
        }
        return result;
    }

    public List<Observation> getDailyObservationsForStation(){
        return dailyObservationsForStation;
    }
    public void quit(){
        driver.quit();
    }

}
