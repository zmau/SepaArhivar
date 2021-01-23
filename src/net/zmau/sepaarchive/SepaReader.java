package net.zmau.sepaarchive;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.zmau.sepaarchive.datastructures.HourDataItem;
import net.zmau.sepaarchive.datastructures.Observation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.zmau.sepaarchive.SepaArchiver.DAILY_DATA_URL;
import static net.zmau.sepaarchive.SepaArchiver.MONTHLY_DATA_URL;

public class SepaReader {
    private WebDriver driver;
    private JavascriptExecutor js;
    private LocalDateTime lastProcessedTimestamp, lastAccessedTimestamp;

    public SepaReader(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
    }

    public void setLastProcessedTimestamp(LocalDateTime value){
        lastProcessedTimestamp = value;
    }
    public List<HourDataItem> readDailyData(int stationId) throws Exception {
        String exactURLForStation = String.format(DAILY_DATA_URL, stationId);
        driver.get(exactURLForStation);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("ucitajgrafikon('satni24h','TabTop1');");
        String bodyHTML = driver.findElement(By.tagName("body")).getAttribute("innerHTML");

        List<HourDataItem> result = new ArrayList<>();
        Document body = Jsoup.parse(bodyHTML);
        Element table = body.getElementById("pregledtabela");
        return readTable(table);
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
            /*int spaceIndex = wholeTitle.indexOf(" [");
            if (spaceIndex > 0)
                result.add(wholeTitle.substring(0, spaceIndex));*/
            else result.add(wholeTitle);
        }
        return result;
    }

    public List<HourDataItem> readMonthlyData(int stationId) throws Exception {
        LocalDateTime lastTimestampToProcess = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        String exactURLForStation = String.format(MONTHLY_DATA_URL, stationId);
        driver.get(exactURLForStation);
        WebElement mesecDana = driver.findElement(By.id("q157"));
        js.executeScript("arguments[0].click();", mesecDana);
        js.executeScript("ucitajgrafikon('satni24h','TabTop1');");

        WebElement tabelarni = driver.findElement(By.id("tabelarni"));
        driver.findElement(By.linkText("TABELARNI PRIKAZ")).click();
        TimeUnit.SECONDS.sleep(10);
        List<HourDataItem> result = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, 4);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("pregledtabela_length")));
            Select comboBox = new Select(driver.findElement(By.name("pregledtabela_length")));
            comboBox.selectByValue("100");

            do {
                Document body = Jsoup.parse(tabelarni.getAttribute("innerHTML"));
                Element table = body.getElementById("pregledtabela");
                List<HourDataItem> currentPage = readTable(table);
                if(!currentPage.isEmpty()) {
                    System.out.println(String.format("station %d;   %s", stationId, currentPage.get(0).getTime().toString()));
                    result.addAll(currentPage);
                }
                else System.out.println(String.format("station %d;   %s (already processed)", stationId, lastAccessedTimestamp));
                WebElement nextButton = driver.findElement(By.id("pregledtabela_next"));
                js.executeScript("arguments[0].click();", nextButton);
            } while (lastAccessedTimestamp.isBefore(lastTimestampToProcess));
        }
        catch (TimeoutException e){
            if(e.getMessage().startsWith("Expected condition failed: waiting for visibility of element located by By.name: pregledtabela_length")){
                Document body = Jsoup.parse(tabelarni.getAttribute("innerHTML"));
                WebDriverWait waitForTable = new WebDriverWait(driver, 5);
                waitForTable.until(ExpectedConditions.visibilityOfElementLocated(By.name("pregledtabela")));
                Element table = body.getElementById("pregledtabela");
                List<HourDataItem> currentPage = readTable(table);
                result.addAll(currentPage);
            }
        }
        return result;
    }

    private List<HourDataItem> readTable(Element table) {
        List<HourDataItem> result = new ArrayList<>();
        List<String> componentList = getHeader(table);
        List<Element> rows =  table.getElementsByTag("tr");

        for (int rowNo = 0; rowNo < rows.size(); rowNo++) {
            List<Element> cells = rows.get(rowNo).getElementsByTag("td");
            if (cells.size() > 0) {
                HourDataItem item = new HourDataItem();
                try {
                    item.setTime(cells.get(0).text());
                    lastAccessedTimestamp = item.getTime();
                    if(!item.getTime().isAfter(lastProcessedTimestamp))
                        continue;
                    for (int columnNo = 1; columnNo < cells.size(); columnNo++) {
                        item.setValue(componentList, columnNo, cells.get(columnNo).text());
                    }
                    if (!item.isEmpty())
                        result.add(item);
                }
                catch (Exception e){
                    System.out.println(rows.get(rowNo).html());
                    System.out.println(e.getMessage());
                }
            }
        }
        return result;
    }

    public void quit(){
        driver.quit();
    }

}
