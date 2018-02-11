package com.trendyol.framework;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FunctionWeb {

    public static Context context = Context.getInstance();

    /**
     * Indicates whether or not the current test requires javascript to be
     * enabled
     */
    public static boolean javascriptEnabledExpected = true;

    /**
     * A flag indicating whether or not the shutdown hook has been configured.
     * The shutdown hook basically shuts down all browsers and does any required
     * house keeping just before the JVM exits.
     */
    private static boolean shutDownHookConfigured = false;


    /**
     * Indicates whether or not the current browser has javascript enabled
     */
    public static boolean javascriptEnabledActual = true;

    /**
     * A a static reference to browser which is used by all tests.
     */
    private static WebDriver browser = null;

    private static org.apache.log4j.Logger logger = Logger.getLogger(FunctionWeb.class);

    /**
     * Returns the static reference to the current browser.
     *
     * @return Reference to the browser.
     */
    public static WebDriver getBrowser() throws Exception {

        // Check if current browser characteristics matches test's requirements
        if (browser != null) {
            if (javascriptEnabledActual != javascriptEnabledExpected) {
                shutDown(); // This will force browser to be launched with
                // correct preferences
            }
        }

        if (browser == null) {

            // Sleep for a random period because browsers launching at the same
            // time might affect each other
            // TODO: Find a more efficient way to do this.

            if (context.getMaxThreads() > 1) {
                Random r = new Random();
                int sleepSecs = r.nextInt(10);
                System.out.println("Sleeping for " + sleepSecs
                        + " seconds before launching browser");
                sleep(sleepSecs * 1000);
            }

            Context.Browser whichBrowser = context.getBrowserType();

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(javascriptEnabledExpected);

            caps.setCapability("takesScreenshot", true);

            javascriptEnabledActual = javascriptEnabledExpected;

            if (whichBrowser == Context.Browser.CHROME) {
                browser = launchChrome(caps);
            } else if (whichBrowser == Context.Browser.IE) {
                browser = launchIE(caps);
            } else if (whichBrowser == Context.Browser.FIREFOX) {
                browser = launchFirefox(caps);
            } else {
                throw new RuntimeException("Unkown browser: "
                        + whichBrowser.toString());
            }

            browser.manage().window().maximize(); //setSize(new Dimension(1280, 800));
            browser.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        }

        setShutDownHook();

        return browser;
    }

    /**
     * Launches the Firefox browser and returns a handle to it.
     *
     * @return A handle to the Firefox browser instance that has just been
     * launched.
     */
    protected static WebDriver launchFirefox(DesiredCapabilities caps) {

        String firefoxDriverLocation = null;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            firefoxDriverLocation = ".\\target\\test-classes\\binaries\\firefoxdriver\\geckodriver.exe";
        }

        System.setProperty("webdriver.gecko.driver", firefoxDriverLocation);

        WebDriver result = null;


        result = new FirefoxDriver(caps);
        result.manage().window().maximize();
        return result;
    }

    /**
     * Launches the IE browser and returns a handle to it.
     *
     * @return A handle to the IE browser instance that has just been
     * launched.
     */
    protected static WebDriver launchIE(DesiredCapabilities caps) {

        String ieDriverLocation = null;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            ieDriverLocation = ".\\target\\test-classes\\binaries\\iedriver\\IEDriverServer.exe";
        }

        System.setProperty("webdriver.ie.driver", ieDriverLocation);

        WebDriver result = null;
        caps.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        caps.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING,false);
        caps.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
        caps.setCapability(InternetExplorerDriver.ELEMENT_SCROLL_BEHAVIOR, true);
        caps.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        result = new InternetExplorerDriver(caps);
        result.manage().window().maximize();
        return result;
    }

    /**
     * Launches the Chrome browser and returns a handle to it.
     *
     * @return A handle to the Chrome browser instance that has just been
     * launched.
     */
    public static WebDriver launchChrome(DesiredCapabilities caps) throws Exception {

        String chromeDriverLocation = null;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            chromeDriverLocation = ".\\target\\test-classes\\binaries\\chromedriver\\chromedriver.exe";
        } else if (System.getProperty("os.name").toLowerCase()
                .contains("mac os")) {
            chromeDriverLocation = "target/test-classes/binaries/chromedriver/chromedriver";
        } else {
            // Assume Linux
            chromeDriverLocation = "target/test-classes/binaries/chromedriver/chromedriver";
        }

        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);

        WebDriver result = null;


        result = new ChromeDriver(caps);
        result.manage().window().maximize();
        return result;
    }

    /**
     * Convenience methods for explicit sleeps. Should be used sparingly.
     *
     * @param millis - The amount of miliseconds to sleep.
     */
    public static void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }

    }

    /**
     * Sets up a shutdown hook in order to close all browsers just before the
     * JVM exits.
     */
    private static void setShutDownHook() {

        if (!shutDownHookConfigured) {
            context.addShutdownHook(new Thread() {
                public void run() {
                    shutDown();
                }
            });
            shutDownHookConfigured = true;
        }
    }

    /**
     * Shuts down the current browser and carries out any required housekeeping.
     * This is required for post-testsuite cleanups as well as for scenarios
     * which request their own browser instance instead of reusing a common one.
     */
    public static void shutDown() {
        if (browser != null) {
            browser.quit();
            browser = null;
        }

        // Shut down context
        context.save();
    }

    /**
     * Checks whether the page  is
     * currently loaded in the browser.
     *
     * @return <code>true</code> if loaded, <code>false</code> if not
     */
    public static boolean waitForPageLoaded()
    {
        try {
            FunctionWeb.sleep(1000);
            WebDriverWait wait = new WebDriverWait(browser, 15);
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                }
            });
            return true;
        } catch (Throwable error) {
            return false;
        }
    }

    /**
     * Checks whether the images are
     * in the browser.
     *
     * @return <code>true</code> if loaded, <code>false</code> if not
     */
    public static void validateInvalidImages() {

        WebElement elementError = null;
        try {

            List<WebElement> imagesList = browser.findElements(By.tagName("img"));

            for (WebElement imgElement : imagesList) {
                if (imgElement != null) {
                    elementError = imgElement;
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet(imgElement.getAttribute("src"));
                    HttpResponse response = client.execute(request);
                    // verifying response code HttpStatus should be 200 if not,

                    if (response.getStatusLine().getStatusCode() != 200)
                        logger.error("Image with title: '" + imgElement.getAttribute("title") + "' is missing");
                }
            }

            if (logger.isInfoEnabled())
                logger.info("Total image scanned: " + imagesList.size());

        } catch (Exception e) {
            e.printStackTrace();
            if (elementError != null)
                logger.error("Image with title: '" + elementError.getAttribute("title") + "' has created error");
            logger.error(e.getMessage());
        }
    }
}
