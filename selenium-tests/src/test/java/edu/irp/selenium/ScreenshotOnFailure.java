package edu.irp.selenium;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Saves target/screenshots/<test>.png whenever a test fails (runs before the driver is closed). */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    private final Supplier<WebDriver> driver;

    public ScreenshotOnFailure(Supplier<WebDriver> driver) {
        this.driver = driver;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isPresent() && driver.get() instanceof TakesScreenshot shooter) {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            Path file = dir.resolve(context.getRequiredTestMethod().getName() + ".png");
            Files.copy(shooter.getScreenshotAs(OutputType.FILE).toPath(), file, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Failure screenshot saved: " + file.toAbsolutePath());
        }
    }
}
