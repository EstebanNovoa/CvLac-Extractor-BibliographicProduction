package step_definition;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.sun.org.apache.bcel.internal.Const;
import data.Constant;
import io.cucumber.java8.En;
import org.openqa.selenium.WebDriver;
import page_objects.CvLacHome;
import page_objects.DriverSetUp;

public class ResearcherCVExtractor implements En {

    public static CvLacHome cvLacHome;
    public static WebDriver driver;
    public static ExtentReports extent = new ExtentReports();
    public static ExtentTest test = extent.createTest("CvLAC Scenario");

    public ResearcherCVExtractor() {
        Given("I am in CvLAC home page$", () -> {
            DriverSetUp driverSetUp = new DriverSetUp();
            driverSetUp.openUrl(Constant.MAIN_URL);
            driver = DriverSetUp.getDriver();
            cvLacHome = new CvLacHome(driver, test);
            cvLacHome.verifyHomePage();
        });

        When("^I select CVLac system$", () -> {
            cvLacHome = new CvLacHome(driver, test);
            cvLacHome.selectSearchMethod(Constant.MENU_OPTION_SEARCH);
        });

        Then("I select educational level", () -> {
            cvLacHome = new CvLacHome(driver, test);
            cvLacHome.selectEducationLevel();
        });

        And("I start to extract the researchers cv data", () -> {
            cvLacHome = new CvLacHome(driver, test);
            cvLacHome.setExtractionRecords(cvLacHome.verifyRecords());
            extent.flush(); // Save the report after extraction
        });


    }
}
