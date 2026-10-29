package edu.irp.selenium;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Five critical user journeys for the Institutional Repository Portal. */
class PortalJourneysTest extends BaseTest {

    @Test
    @DisplayName("J1: dashboard loads with seeded records")
    void dashboardLoads() {
        assertEquals("Institutional Repository Portal", driver.findElement(By.tagName("h1")).getText());
        int total = Integer.parseInt(driver.findElement(By.id("stat-total")).getText());
        assertTrue(total >= 6, "Expected at least 6 seeded records but saw " + total);
        assertFalse(driver.findElements(By.cssSelector("tr.record-row")).isEmpty());
    }

    @Test
    @DisplayName("J2: submitter creates a record and it starts as a draft")
    void submitterCreatesDraft() {
        String title = createDraft("Selenium Created Record " + System.nanoTime());
        search(title);
        assertEquals("DRAFT", firstRowStatus());
        assertTrue(firstRow().getText().contains(title));
    }

    @Test
    @DisplayName("J3: search finds a known record and shows a message when nothing matches")
    void searchFindsAndHandlesNoResult() {
        search("crop");
        assertTrue(firstRow().getText().contains("Crop Yield"));
        search("zzz-no-such-record-zzz");
        assertTrue(driver.findElements(By.cssSelector("tr.record-row")).isEmpty());
        assertTrue(driver.findElement(By.id("empty-message")).isDisplayed());
    }

    @Test
    @DisplayName("J4: record travels Draft -> Submitted -> Approved -> Published through three roles")
    void roleBasedWorkflow() {
        String title = createDraft("Workflow Record " + System.nanoTime());
        search(title);
        clickAction("SUBMITTED");
        assertEquals("SUBMITTED", firstRowStatus());

        selectRole("REVIEWER");
        clickAction("APPROVED");
        assertEquals("APPROVED", firstRowStatus());

        selectRole("ADMIN");
        clickAction("PUBLISHED");
        assertEquals("PUBLISHED", firstRowStatus());
        assertTrue(driver.findElements(By.cssSelector("button.action")).isEmpty(),
            "A published record must have no further workflow actions");
    }

    @Test
    @DisplayName("J5: visitors are read-only")
    void visitorIsReadOnly() {
        selectRole("VIEWER");
        assertFalse(driver.findElement(By.id("save-btn")).isEnabled(), "Visitors must not be able to save");
        assertTrue(driver.findElements(By.cssSelector("button.action, button.edit")).isEmpty());
    }
}
