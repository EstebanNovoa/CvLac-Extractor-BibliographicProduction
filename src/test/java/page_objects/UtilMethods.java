package page_objects;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.JsonObject;
import data.SQLiteManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UtilMethods {


    private static By by_title_text(String text) {
        return By.xpath("//h3[text()='" + text + "']");
    }




    /**
     * Switch window to new window tab
     * @return the string of original tab
     */
    public static void switchTab(WebDriver driver, boolean switchOriginalTab) {
        try {
            if(switchOriginalTab) {
                Set<String> allTabs = driver.getWindowHandles();
                Iterator<String> iterator = allTabs.iterator();
                if (iterator.hasNext()) {
                    String firstTab = iterator.next();
                    driver.switchTo().window(firstTab);
                    while (iterator.hasNext()) {
                        String otherTab = iterator.next();
                        driver.switchTo().window(otherTab);
                        driver.close();
                    }
                    driver.switchTo().window(firstTab);
                }
            }else{
                Set<String> allTabs = driver.getWindowHandles();
                String originalTab = driver.getWindowHandle();
                for (String tab : allTabs) {
                    if (!tab.equals(originalTab)) {
                        driver.switchTo().window(tab);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Switch window to original window tab
     */
    public void switchOriginalTab(WebDriver driver) {
        try {
            Set<String> allTabs = driver.getWindowHandles();
            String originalTab = driver.getWindowHandle();
            driver.switchTo().window(originalTab);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Evaluate if the title is present
     * @param driver driver
     * @param titleToValidate text to validate
     * @return
     */
    public static boolean getTitlePresent(WebDriver driver,String titleToValidate) {
        try {
            return !driver.findElements(by_title_text(titleToValidate)).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scroll forward the object to be search
     * @param driver driver
     * @param element web element
     */
    public static boolean scrollToElement(WebDriver driver, WebElement element) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            return true;
        } catch (Exception e) {
            System.err.println("Error al hacer scroll hasta el elemento: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extracts structured bibliographic information from an HTML blockquote element
     * using Selenium WebDriver and stores it in a JsonObject from Gson library.
     * @param blockquote WebDriver instance used to navigate the web page.
     * @return A JsonObject containing structured data extracted from the blockquote.
     */
    public static JsonObject extractArticleData(WebElement blockquote, Connection connection, int autorID, String autorFullName) {
        JsonObject bibliographicData = new JsonObject();
        String text = blockquote.getText();
        // Extract author: The first text before the first comma
        String author = text.split(",")[0].trim();
        bibliographicData.addProperty("author", author);
        // Extract title: Captures text inside quotes
        extractData(text, "\"(.*?)\"", "title", bibliographicData);
        // Extract country: Captures the country after ". En:"
        extractData(text, "\\. En:\\s*([^\\n]+)", "country", bibliographicData);
        // Extract ISSN: Captures the ISSN number after "ISSN:"
        extractData(text, "ISSN:\\s*([\\d-]+)", "ISSN", bibliographicData);
        // Extract editorial: Captures editorial details including volume, issue, pages, and year
        extractData(text, "ed:\\s*([^\\n]+)\\n.*?(v\\.\\d+).*?(fasc\\.\\S+).*?p\\.(\\d+\\s*-\\s*\\d+).*?,(\\d+)", "editorial", bibliographicData);
        // Extract DOI: Captures DOI link if present
        extractData(text, "DOI:\\s*(https?://doi\\.org/|)(\\S+)", "DOI", bibliographicData);
        // Extract key_words: Captures keywords after "Palabras:"
        extractData(text, "Palabras:\\s*([^\n]*)", "key_words", bibliographicData);
        // Extract sectors: Captures sector information after "Sectores:"
        extractData(text, "Sectores:\\s*([^\n]*)", "sectors", bibliographicData);
        // Extract areas: Captures area descriptions after "Areas:"
        extractData(text, "Areas:\\s*([^\n]*)", "areas", bibliographicData);
        SQLiteManager.insertBibliographicElement(connection,bibliographicData,1,autorID, autorFullName);
        return bibliographicData;
    }


    /**
     * Extracts structured bibliographic information from an HTML blockquote element
     * using Selenium WebDriver and stores it in a JsonObject from Gson library.
     * @param blockquote WebDriver instance used to navigate the web page.
     * @return A JsonObject containing structured data extracted from the blockquote.
     */
    public static JsonObject extractBookData(WebElement blockquote,Connection connection,int autorID,String autorFullName) {
        JsonObject bibliographicData = new JsonObject();
        String text = blockquote.getText();
        // Extract author
        String author = text.split(",")[0].trim();
        bibliographicData.addProperty("author", author);
        // Extract title
        extractData(text, "\"(.*?)\"", "title", bibliographicData);
        // Extract country
        extractData(text, "En:\\s*([^\\d\\n]+)", "country", bibliographicData);
        // Extract year
        extractData(text, "En:\\s*[^\\d]*?(\\d{4})", "year", bibliographicData);
        // Extract ISBN
        extractData(text, "ISBN:\\s*([\\d-]+)", "ISBN", bibliographicData);
        // Extract editorial
        extractData(text, "ed:\\s*([^\\s]+(?:\\s[^\\s]+)*)", "editorial", bibliographicData);
        // Extract key_words
        extractData(text, "Palabras:\\s*([^\n]*)", "key_words", bibliographicData);
        // Extract sectors
        extractData(text, "Sectores:\\s*([^\n]*)", "sectors", bibliographicData);
        // Extract areas
        extractData(text, "Areas:\\s*([^\n]*)", "areas", bibliographicData);
        System.out.println("Inserting book data into database: " + bibliographicData.toString());
        SQLiteManager.insertBibliographicElement(connection,bibliographicData,2,autorID, autorFullName);
        return bibliographicData;
    }
    /**
     * Extracts structured bibliographic information from an HTML blockquote element
     * using Selenium WebDriver and stores it in a JsonObject from Gson library.
     * @param blockquote WebDriver instance used to navigate the web page.
     * @return A JsonObject containing structured data extracted from the blockquote.
     */
    public static JsonObject extractBookChapterData(WebElement blockquote, Connection  connection,int autorID,String autorFullName) {
        JsonObject bibliographicData = new JsonObject();
        String text = blockquote.getText();
        // Extract author
        extractData(text, "Tipo: Otro capítulo de libro publicado\\s*[\\r\\n]+([A-ZÀ-ÿ\\s]+),", "author", bibliographicData);
        // Extract title
        extractData(text, "\"([^\"]+)\"\\s*([^\\.]+)\\.", "title", bibliographicData);
        // Extract country
        extractData(text, "En:\\s*([A-Za-zÀ-ÿ\\s]+)(?=\\s*\\d{4}|\\s*ISBN|$)", "country", bibliographicData);
        // Extract year
        extractData(text, "(\\d{4})(?!.*\\d{4})", "year", bibliographicData);
        // Extract ISBN
        extractData(text, "ISBN:\\s*([\\d-]+)", "ISBN", bibliographicData);
        // Extract editorial
        extractData(text, "ed:\\s*([^\\s]+(?:\\s[^\\s]+)*)", "editorial", bibliographicData);
        // Extract key_words
        extractData(text, "Palabras:\\s*([^\n]*)", "key_words", bibliographicData);
        // Extract sectors
        extractData(text, "Sectores:\\s*([^\n]*)", "sectors", bibliographicData);
        // Extract areas
        extractData(text, "Areas:\\s*([^\n]*)", "areas", bibliographicData);
        System.out.println("Inserting book chapter data into database: " + bibliographicData.toString());
        SQLiteManager.insertBibliographicElement(connection,bibliographicData,3,autorID,autorFullName);
        return bibliographicData;
    }

    /**
     * Extracts structured bibliographic information from an HTML blockquote element
     * using Selenium WebDriver and stores it in a JsonObject from Gson library.
     * @param blockquote WebDriver instance used to navigate the web page.
     * @return A JsonObject containing structured data extracted from the blockquote.
     */
    public static JsonObject extractScientificNotes(WebElement blockquote,Connection connection,int autorID,String autorFullName) {
        JsonObject bibliographicData = new JsonObject();
        String text = blockquote.getText();
        // Extract author
        extractData(text, "^([A-ZÀ-ÿ\\s,]+)\\s*(?=\")", "author", bibliographicData);
        // Extract title
        extractData(text, ",\\s*\"([^\"]+)\"", "title", bibliographicData);
        // Extract country
        extractData(text, "En:\\s*([A-Za-zÀ-ÿ\\s]+)(?=\\s*\\d{4}|\\s*ISBN|$)", "country", bibliographicData);
        // Extract Publishing Platform
        extractData(text, "\\.\\s*([A-ZÀ-ÿ\\s]+)(?=\\s)", "publishing_platform", bibliographicData);
        // Extract ISNN
        extractData(text, "ISSN:\\s*(\\d{4}-\\d{4})", "ISNN", bibliographicData);
        // Extract editorial
        extractData(text, "ed:\\s*([^\\s]+(?:\\s[^\\s]+)*)", "editorial", bibliographicData);
        // Extract year
        extractData(text, "ed:.*?,(\\d{4})", "year", bibliographicData);
        // Extract key_words
        extractData(text, "Palabras:\\s*([^\n]*)", "key_words", bibliographicData);
        // Extract sectors
        extractData(text, "Sectores:\\s*([^\n]*)", "sectors", bibliographicData);
        // Extract areas
        extractData(text, "Areas:\\s*([^\n]*)", "areas", bibliographicData);
        System.out.println("Inserting Scientific Notes data into database: " + bibliographicData.toString());
        SQLiteManager.insertBibliographicElement(connection,bibliographicData,4,autorID, autorFullName);
        return bibliographicData;
    }

    /**
     * Extracts structured bibliographic information from an HTML blockquote element
     * using Selenium WebDriver and stores it in a JsonObject from Gson library.
     * @param blockquote WebDriver instance used to navigate the web page.
     * @return A JsonObject containing structured data extracted from the blockquote.
     */
    public static JsonObject extractProject(WebElement blockquote,Connection connection,int autorID,String autorFullName) {
        JsonObject bibliographicData = new JsonObject();
        String text = blockquote.getText();
        // Extract project type
        extractData(text, "Tipo de proyecto:\\s*([^\n]+)", "project_type", bibliographicData);
        // Extract project title
        extractData(text, "(?<=\\r?\\n)([^\\r\\n]+)(?=\\r?\\n)", "title", bibliographicData);
        // Extract project summary
        extractData(text, "(?<=Resumen\\r?\\n)([\\s\\S]+)", "summary ", bibliographicData);
        System.out.println("Inserting Project data into database: " + bibliographicData.toString());
        SQLiteManager.insertBibliographicElement(connection,bibliographicData,5,autorID, autorFullName);
        return bibliographicData;
    }

    /**
     * Extracts data using a provided regex string and stores it in a JsonObject.
     *
     * @param text The text to analyze.
     * @param regex The regex string used for pattern matching.
     * @param propertyName The key to store the extracted value in the JsonObject.
     * @param bibliographicData The JsonObject where extracted data will be stored.
     */
    public static void extractData(String text, String regex, String propertyName, JsonObject bibliographicData) {
        Pattern pattern = Pattern.compile(regex); // Initialize Pattern inside the method
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String extractedValue = matcher.group(1).trim();
            bibliographicData.addProperty(propertyName, extractedValue);
        } else {
            bibliographicData.addProperty(propertyName, "[Not specified]");
        }
    }

    /**
     * Extracts the number that appears after the word "de" in the given text.
     * If no valid number is found, the method returns -1.
     *
     * @param text The input string containing the number to extract.
     * @return The extracted number as an integer, or -1 if extraction fails.
     */
    public static double extractNumberRecords(String text) {
        try {
            Pattern pattern = Pattern.compile("de\\s+([\\d,]+)");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String numberFormatted = matcher.group(1).replace(",", "");
                return Double.parseDouble(numberFormatted);
            } else {
                throw new IllegalArgumentException("No valid number found after 'de'.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Takes a screenshot of the current browser window and saves it to the specified evidences directory.
     * The screenshot file will be named with the prefix 'TESTING_' followed by the current date and time.
     * The directory will be created if it does not exist.
     *
     * @param driver The WebDriver instance used for taking the screenshot.
     * @param actionName A descriptive name for the action being performed (used in the filename).
     */
    public static void takeScreenshot(WebDriver driver, String actionName) {
        String evidenceDir = "C:/Automation/evidences/";
        File dir = new File(evidenceDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = evidenceDir + "TESTING_" + actionName + "_" + timestamp + ".png";
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File(fileName);
            FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean isDriverSessionActive(WebDriver driver) {
        if (driver == null) return false;
        try {
            // a lightweight call that will fail if the session is closed
            driver.getWindowHandles();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finalize the test scenario:
     * - Take a final screenshot.
     * - Archive all screenshots that start with "TESTING_" (optionally those for today).
     * - Log archive path to ExtentTest (if provided).
     * - Quit the WebDriver.
     *
     * Returns the path to the created ZIP archive.
     */
    public static String finalizeScenarioAndArchiveScreenshots(WebDriver driver, ExtentTest test) {
        try {
            // take a final screenshot only if the driver session is active
            if (isDriverSessionActive(driver)) {
                String finalShotName = "TESTING_FINAL_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                try {
                    takeScreenshot(driver, finalShotName);
                } catch (Exception e) {
                    if (test != null) test.warning("Failed to take final screenshot: " + e.getMessage());
                }
            } else {
                if (test != null) test.info("Driver session not active. Skipping final screenshot.");
            }

            File evidenceDir = new File("C:/Automation/evidences");
            if (!evidenceDir.exists()) {
                if (test != null) test.info("No evidence directory found at " + evidenceDir.getAbsolutePath());
                // do not attempt to quit an already-closed driver
                if (isDriverSessionActive(driver)) {
                    try { driver.quit(); } catch (Exception ignored) {}
                }
                return null;
            }

            File[] filesToArchive = evidenceDir.listFiles((dir, name) -> name.startsWith("TESTING_"));
            if (filesToArchive == null || filesToArchive.length == 0) {
                if (test != null) test.info("No screenshots found to archive in " + evidenceDir.getAbsolutePath());
                if (isDriverSessionActive(driver)) {
                    try { driver.quit(); } catch (Exception ignored) {}
                }
                return null;
            }

            String zipName = "TESTING_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
            Path zipPath = new File(evidenceDir, zipName).toPath();

            try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipPath)))) {
                byte[] buffer = new byte[4096];
                for (File f : filesToArchive) {
                    if (!f.isFile()) continue;
                    ZipEntry entry = new ZipEntry(f.getName());
                    zos.putNextEntry(entry);
                    try (InputStream is = new BufferedInputStream(Files.newInputStream(f.toPath()))) {
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (Exception e) {
                        if (test != null) test.warning("Failed to add file to zip: " + f.getName() + " - " + e.getMessage());
                    }
                    zos.closeEntry();
                }
            } catch (Exception e) {
                if (test != null) test.fail("Failed to create zip archive: " + e.getMessage());
                return null;
            }

            if (test != null) test.info("Screenshots archived to: " + zipPath.toAbsolutePath().toString());

            // only quit driver if session is active
            if (isDriverSessionActive(driver)) {
                try { driver.quit(); } catch (Exception ignored) {}
            } else {
                if (test != null) test.info("Driver already closed; skipping quit().");
            }

            return zipPath.toAbsolutePath().toString();
        } catch (Exception e) {
            return null;
        }
    }




}