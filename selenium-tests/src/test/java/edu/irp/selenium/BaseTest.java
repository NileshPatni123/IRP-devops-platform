package edu.irp.selenium;

import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared set-up. System properties:
 *   base.url   application under test (default http://localhost:8080)
 *   remote.url Selenium Grid / standalone-chrome URL; when empty, local Chrome is used
 *   headless   true (default) or false
 */
public abstract class BaseTest {

    protected static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");

    protected WebDriver driver;
    protected WebDriverWait wait;

    @RegisterExtension
    final ScreenshotOnFailure screenshots = new ScreenshotOnFailure(() -> driver);

    @BeforeEach
    void openBrowser() throws Exception {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1366,900");
        String remote = System.getProperty("remote.url", "");
        driver = remote.isBlank() ? new ChromeDriver(options) : new RemoteWebDriver(new URL(remote), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(BASE_URL);
        waitReady();
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** The page sets body[data-ready=true] when every API call has finished. */
    protected void waitReady() {
        wait.until(ExpectedConditions.attributeToBe(By.tagName("body"), "data-ready", "true"));
    }

    protected void selectRole(String role) {
        new Select(driver.findElement(By.id("role"))).selectByValue(role);
        waitReady();
    }

    protected void search(String text) {
        WebElement box = driver.findElement(By.id("search-input"));
        box.clear();
        box.sendKeys(text);
        driver.findElement(By.id("search-btn")).click();
        waitReady();
    }

    protected void type(String id, String text) {
        WebElement field = driver.findElement(By.id(id));
        field.clear();
        field.sendKeys(text);
    }

    protected String createDraft(String title) {
        selectRole("SUBMITTER");
        type("title", title);
        type("authors", "Selenium Tester");
        type("department", "Quality Assurance");
        type("keywords", "automation, selenium");
        driver.findElement(By.id("save-btn")).click();
        waitReady();
        return title;
    }

    protected WebElement firstRow() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tr.record-row")));
    }

    protected String firstRowStatus() {
        return firstRow().findElement(By.cssSelector(".status")).getAttribute("data-status");
    }

    protected void clickAction(String action) {
        firstRow().findElement(By.cssSelector("button.action[data-action='" + action + "']")).click();
        waitReady();
    }
}
