package net.zmau.sepaarchive;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.zmau.sepaarchive.datastructures.Observation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SepaReader {

    private final static String SITE_ADDRESS = "http://www.amskv.sepa.gov.rs/android/pregledpodataka.php?stanica=70&fbclid=IwAR2ezkHiUXsKCwPJAtzWZotzEPBsPjzh5HUw6HUuM5UkreVMbYUOx-eFEeY" ;

    public String readDailyData(){
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        try {
          driver.get(SITE_ADDRESS);
            JavascriptExecutor js = (JavascriptExecutor)driver;
            js.executeScript("ucitajgrafikon('satni24h','TabTop1');");
            WebElement table = driver.findElement(By.id("pregledtabela"));
            return table.getText();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
           driver.quit();
        }
    }


}
