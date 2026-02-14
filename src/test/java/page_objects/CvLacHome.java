package page_objects;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import data.Constant;
import data.SQLiteManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static page_objects.UtilMethods.finalizeScenarioAndArchiveScreenshots;
import static step_definition.ResearcherCVExtractor.extent;

import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CvLacHome {


    private WebDriver driver;
    private static ExtentTest test;
    public static int currentOptionEducationFormationIndex = 4;
    public static Connection connection;
    public static boolean startHere = false;


    public CvLacHome(WebDriver driver, ExtentTest test) throws SQLException {
        this.driver = driver;
        this.test = test;
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\tebin\\OneDrive\\Desktop\\Database CvExtractor\\CVLACData.db");
    }

    private By by_element_class(String className) {
        return By.xpath("//*[@class='" + className + "']");
    }

    private By by_element_name(String name) {
        return By.xpath("//*[@name='" + name + "']");
    }

    private By by_element_type(String type) {
        return By.xpath("//*[@type='" + type + "']");
    }

    private By by_element_title(String title) {
        return By.xpath("//*[@title='" + title + "']");
    }

    private By by_element_text(String text) {
        return By.xpath("//*[contains(text(),'" + text + "')]");
    }

    private By by_title_text(String text) {
        return By.xpath("//h3[text()='" + text + "']");
    }

    /**
     * Verify if there are the main options are present to verify the home page
     */
    public void verifyHomePage() {
        try {
            assertFalse("There are no home page", driver.findElements(by_element_class("row margin_enlaces")).isEmpty());

        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Select the option to search the reasearchers
     */
    public void selectSearchMethod(String menuOptionText) {
        try {
            List<WebElement> options = driver.findElements(by_element_class("row margin_enlaces"));
            for (WebElement option : options) {
                String optionText = option.findElement(By.tagName("a")).getText();
                if (optionText.contains(menuOptionText)) {
                    // Take screenshot before clicking the menu option
                    UtilMethods.takeScreenshot(driver, "before_click_menuOption_" + menuOptionText.replace(" ", "_"));
                    option.click();
                    break;
                }
            }
            Thread.sleep(5000);
        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Select the option to search the reasearchers
     */
    public void selectEducationLevel() {
        try {
            Select select = new Select(driver.findElement(by_element_name("nivelFormacion")));
            List<WebElement> options = select.getOptions();
            if (currentOptionEducationFormationIndex == 0) {
                currentOptionEducationFormationIndex = 1;
            } else {
                if (currentOptionEducationFormationIndex < options.size()) {
                    currentOptionEducationFormationIndex += 1;
                } else {
                    // Take screenshot before closing the driver
                    UtilMethods.takeScreenshot(driver, "before_click_close_driver");
                    driver.close();
                }
            }
            // Take screenshot before selecting education level
            UtilMethods.takeScreenshot(driver, "before_select_education_level_index_" + currentOptionEducationFormationIndex);
            select.selectByIndex(currentOptionEducationFormationIndex);
            System.out.println("\n\nIndex nivel formacion: " + currentOptionEducationFormationIndex + "\n\n");
            // Take screenshot before clicking submit
            UtilMethods.takeScreenshot(driver, "before_click_submit_education_level");
            driver.findElement(by_element_type("submit")).click();
            Thread.sleep(5000);
//            WebElement inputNombre = driver.findElement(By.name("txtNamesRh"));
//            inputNombre.clear(); // Limpia el campo por si tiene datos previos
//            inputNombre.sendKeys("Juan Sebastian Gonzalez Sanabria");
//            driver.findElement(by_element_type("submit")).click();
//            Thread.sleep(5000);
        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the extraction behavior based on the presence of records in the current section.
     * If there are no records, it triggers a search in another section.
     * If records exist, it extracts the relevant information.
     *
     * @param flag Determines whether the extraction process should be executed.
     */
    public void setExtractionRecords(boolean flag) throws InterruptedException {
        try {
            if (flag) {
                WebElement option = driver.findElement(by_element_class("tbody"));
                List<WebElement> rowList = option.findElements(By.tagName("tr"));
                if (!rowList.isEmpty()) {
                    extractRecordsData();
                    selectEducationLevel();
                    setExtractionRecords(verifyRecords());
                }
            } else {
                selectEducationLevel();
                setExtractionRecords(verifyRecords());
            }
        } catch (Exception | AssertionError e) {
            UtilMethods.switchTab(driver, true);
            Thread.sleep(500);
        }
    }

    /**
     * Select the option to search the reasearchers
     * return true if there is any record, false otherwise
     */
    public boolean verifyRecords() {
        try {
            boolean isThereAnyRecord = false;
            if (driver.findElements(By.id("investigadores_row1")).isEmpty()) { // verify if there is no records
                // Take screenshot before clicking 'volver'
                UtilMethods.takeScreenshot(driver, "before_click_volver_button");
                driver.findElement(by_element_title("volver")).click();
            } else {
                isThereAnyRecord = true;
            }
            return isThereAnyRecord;
        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterate through the list of records displayed on the main page
     */
    public void extractRecordsData() {
        try {
            int numberRecordsEducationLevel = (int) UtilMethods.extractNumberRecords(driver.findElement(By.xpath("//tr[@class='statusBar']/td[1]")).getText()) / 15;

            for (int i = 0; i < numberRecordsEducationLevel; i++) {
                WebElement option = driver.findElement(by_element_class("tbody"));
                List<WebElement> rowList = option.findElements(By.tagName("tr"));
                int indexRow = 0;
                if (i == 0) {
                    startHere = true;
                }
                if (startHere) {
                    for (WebElement researcherRow : rowList) {
                        indexRow++;
                        List<WebElement> columResearchRecord = researcherRow.findElements(By.tagName("td"));
                        // Take screenshot before clicking the researcher name
                        UtilMethods.takeScreenshot(driver, "before_click_researcher_name_row_" + indexRow);
                        columResearchRecord.get(1).findElement(By.tagName("a")).click(); //Click the researcher name
                        Thread.sleep(4000);
                        UtilMethods.switchTab(driver, false);
                        Thread.sleep(5000);
                        WebElement generalData = driver.findElement(by_element_name("datos_generales"));
                        if (isBasicDataShowed()) {
                            JsonObject basicData = getBasicDataCV();
                            if (basicData != null) {
                                JsonArray bibliographicData = getBibliographicProduction(basicData.get("id").getAsInt(), basicData.get("authorsName").getAsString());
                            }

                            UtilMethods.switchTab(driver, true);
                            Thread.sleep(2000);
                        } else {
                            UtilMethods.switchTab(driver, true);
                            Thread.sleep(2000);
                        }
                    }
                }
                WebElement nextPageButton = driver.findElement(By.xpath("//img[@src='/ciencia-war/images/table/nextPage.gif']"));
                // Take screenshot before clicking next page
                UtilMethods.takeScreenshot(driver, "before_click_next_page_button_page_" + (i + 1));
                nextPageButton.click();
                Thread.sleep(3000);
            }
            goBackToSearchPage();
            selectSearchMethod(Constant.MENU_OPTION_SEARCH);
            Thread.sleep(5000);

        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    public void goBackToSearchPage() {
        try {
            String originalWindow = driver.getWindowHandle();
            List<WebElement> mainOption = driver.findElements(By.className("pie-contenedor-imagen"));
            // Take screenshot before clicking the main option to go back
            UtilMethods.takeScreenshot(driver, "before_click_goBackToSearchPage");
            mainOption.get(3).click();
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windowHandles = driver.getWindowHandles();
            for (String handle : windowHandles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            driver.switchTo().window(originalWindow);
            // Take screenshot before closing the original window
            UtilMethods.takeScreenshot(driver, "before_click_close_original_window");
            driver.close();
            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                break;
            }
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Validate the bibliographic productions registered into research cv
     *
     * @return an array list with the titles registered in CV
     */
    public JsonArray getBibliographicProduction(int autorID, String autorFullName) {
        try {
            JsonArray bibliographicProduction = new JsonArray();
            if (UtilMethods.getTitlePresent(driver, "Artículos")) {
                JsonArray articlesObject = new JsonArray();
                UtilMethods.scrollToElement(driver, driver.findElement(by_title_text("Artículos")));
                Thread.sleep(3000);
                WebElement childElement = driver.findElement(by_title_text("Artículos"));
                List<WebElement> parentElement = childElement.findElement(By.xpath("./../../..")).findElements(By.tagName("tr"));
                for (int i = 1; i < parentElement.size(); i += 2) {
                    JsonObject articleObject = new JsonObject();
                    articleObject.add("article section", UtilMethods.extractArticleData(parentElement.get(i + 1).findElement(By.tagName("blockquote")), connection, autorID, autorFullName));
                    articlesObject.add(articleObject);
                }
                Thread.sleep(2000);
                bibliographicProduction.add(articlesObject);
            }
            if (UtilMethods.getTitlePresent(driver, "Libros")) {
                JsonArray booksObject = new JsonArray();
                UtilMethods.scrollToElement(driver, driver.findElement(by_title_text("Libros")));
                Thread.sleep(3000);
                WebElement childElement = driver.findElement(by_title_text("Libros"));
                List<WebElement> parentElement = childElement.findElement(By.xpath("./../../..")).findElements(By.tagName("tr"));
                for (int i = 1; i < parentElement.size() - 1; i += 2) {
                    JsonObject bookObject = new JsonObject();
                    bookObject.add("book section", UtilMethods.extractBookData(parentElement.get(i + 1).findElement(By.tagName("blockquote")), connection, autorID, autorFullName));
                    booksObject.add(bookObject);
                }
                Thread.sleep(2000);
                bibliographicProduction.add(booksObject);
            }
            if (UtilMethods.getTitlePresent(driver, "Capitulos de libro")) {
                JsonArray booksObject = new JsonArray();
                UtilMethods.scrollToElement(driver, driver.findElement(by_title_text("Capitulos de libro")));
                Thread.sleep(3000);
                WebElement childElement = driver.findElement(by_title_text("Capitulos de libro"));
                List<WebElement> parentElement = childElement.findElement(By.xpath("./../../..")).findElements(By.tagName("tr"));
                for (int i = 1; i < parentElement.size(); i++) {
                    JsonObject bookObject = new JsonObject();
                    bookObject.add("chapter book section", UtilMethods.extractBookChapterData(parentElement.get(i).findElement(By.tagName("blockquote")), connection, autorID, autorFullName));
                    booksObject.add(bookObject);
                }
                Thread.sleep(2000);
                bibliographicProduction.add(booksObject);
            }
            if (UtilMethods.getTitlePresent(driver, "Notas científicas")) {
                JsonArray scientificNotes = new JsonArray();
                UtilMethods.scrollToElement(driver, driver.findElement(by_title_text("Notas científicas")));
                Thread.sleep(3000);
                WebElement childElement = driver.findElement(by_title_text("Notas científicas"));
                List<WebElement> parentElement = childElement.findElement(By.xpath("./../../..")).findElements(By.tagName("tr"));
                for (int i = 1; i < parentElement.size(); i += 2) {
                    JsonObject scientificNote = new JsonObject();
                    scientificNote.add("scientific note section", UtilMethods.extractScientificNotes(parentElement.get(i + 1).findElement(By.tagName("blockquote")), connection, autorID, autorFullName));
                    scientificNotes.add(scientificNote);
                }
                bibliographicProduction.add(scientificNotes);
            }
            if (UtilMethods.getTitlePresent(driver, "Proyectos")) {
                JsonArray projects = new JsonArray();
                UtilMethods.scrollToElement(driver, driver.findElement(by_title_text("Proyectos")));
                Thread.sleep(3000);
                WebElement childElement = driver.findElement(by_title_text("Proyectos"));
                List<WebElement> parentElement = childElement.findElement(By.xpath("./../../..")).findElements(By.tagName("tr"));
                for (int i = 0; i < parentElement.size() - 1; i++) {
                    JsonObject project = new JsonObject();
                    project.add("project section", UtilMethods.extractProject(parentElement.get(i + 1).findElement(By.tagName("blockquote")), connection, autorID, autorFullName));
                    projects.add(project);
                }
                bibliographicProduction.add(projects);
            }
            return bibliographicProduction;
        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the basic information from researcher
     *
     * @return A hash map key value with the basic data
     */
    public JsonObject getBasicDataCV() {
        try {
            JsonObject basicData = new JsonObject();
            WebElement table = driver.findElements(By.tagName("tbody")).get(1);
            List<WebElement> rowList = table.findElements(By.tagName("td"));
            if (rowList.size() < 8) {
                return null;
            } else {
                String name = rowList.get(1).getText(); //Full Name
                String authorsName = rowList.get(3).getText();  // Name use in
                String nationality = rowList.get(5).getText();
                String gender = rowList.get(7).getText();
                SQLiteManager.insertResearcher(connection, name, nationality);
                basicData.addProperty("id", SQLiteManager.getCurrentAuthorID());
                basicData.addProperty("name", SQLiteManager.getCurrentAuthorName());
                basicData.addProperty("authorsName", authorsName);
                basicData.addProperty("nationality", nationality);
                basicData.addProperty("gender", gender);
                return basicData;
            }

        } catch (Exception | AssertionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the basic information from researcher
     *
     * @return A hash map key value with the basic data
     */
    public boolean isBasicDataShowed() {
        try {
            WebElement table = driver.findElements(By.tagName("tbody")).get(1);
            List<WebElement> rowList = table.findElements(By.tagName("td"));
            if (rowList.size() < 8) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

    }


    /**
     * Get the number of records present in researchers search page
     *
     * @return number of records found
     */
    public int getResercherNumnberRecords() {
        int numberRecords = 0;
        return 0;

    }


}
