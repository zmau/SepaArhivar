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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.zmau.sepaarchive.SepaArchiver.DAILY_DATA_URL;
import static net.zmau.sepaarchive.SepaArchiver.MONTHLY_DATA_URL;
public class SepaReader {
    private WebDriver driver;

    public SepaReader(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }
    public List<HourDataItem>  readDailyData(int stationId) throws Exception {
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

    public List<HourDataItem>  readMonthlyData(int stationId) throws Exception {
        String exactURLForStation = String.format(MONTHLY_DATA_URL, stationId);
        driver.get(exactURLForStation);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement mesecDana = driver.findElement(By.id("q157"));
        js.executeScript("arguments[0].click();", mesecDana);
        js.executeScript("ucitajgrafikon('satni24h','TabTop1');");

        WebElement tabelarni = driver.findElement(By.id("tabelarni"));
        driver.findElement(By.linkText("TABELARNI PRIKAZ")).click();
        TimeUnit.SECONDS.sleep(10);
        List<HourDataItem> result = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("pregledtabela_length")));
            Select comboBox = new Select(driver.findElement(By.name("pregledtabela_length")));
            comboBox.selectByValue("100");

            String previousPageStart = null, thisPageStart = null;
            do {
                previousPageStart = thisPageStart;
                Document body = Jsoup.parse(tabelarni.getAttribute("innerHTML"));
                Element table = body.getElementById("pregledtabela");
                System.out.println(thisPageStart);
                List<HourDataItem> currentPage = readTable(table);
                thisPageStart = currentPage.get(0).getTime().toString();
                result.addAll(currentPage);
                WebElement nextButton = driver.findElement(By.id("pregledtabela_next"));
                js.executeScript("arguments[0].click();", nextButton);
            } while (!thisPageStart.equals(previousPageStart));
        }
        catch (TimeoutException e){
            if(e.getMessage().equals("Expected condition failed: waiting for visibility of element located by By.name: pregledtabela_length (tried for 20 second(s) with 500 milliseconds interval)")){
                Document body = Jsoup.parse(tabelarni.getAttribute("innerHTML"));
                WebDriverWait waitForTable = new WebDriverWait(driver, 20);
                waitForTable.until(ExpectedConditions.visibilityOfElementLocated(By.name("pregledtabela")));
                Element table = body.getElementById("pregledtabela");
                List<HourDataItem> currentPage = readTable(table);
                result.addAll(currentPage);
            }
        }
        return result;
    }
    public void quit(){
        driver.quit();
    }

}
