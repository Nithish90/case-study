package com.trendyol.framework;

import java.io.*;
import java.util.*;

public class Context extends HashMap {

    /**
     * Stores default values for important properties. The object is populated
     * by the initDefaults() method.
     */
    protected Properties defaults;

    /**
     * The name of the property which will contain the Environment against which
     * test will execute.
     */
    public final String pEnvironment = "env";

    /**
     * A flag indicating whether or not the shutdown hook has been configured.
     * The shutdown hook basically shuts down all browsers and does any required
     * house keeping just before the JVM exits.
     */
    private static boolean shutDownHookConfigured = false;

    /**
     * Instance variable for the singleton pattern.
     */
    private static Context instance = null;

    /**
     * Environment factory
     */
    EnvironmentFactory environmentFactory = EnvironmentFactory.getInstance();


    /**
     * A list of shutdown hooks.
     */
    public List<Thread> shutdownHooks = new ArrayList<Thread>();

    /**
     * A key-value pair properties object which is used to provide a context.
     * This is automatically serialised to testframework.properties at the end of a
     * test run.
     */

    private Properties props = null;

    /**
     * Overrides the singleton instance. This is required for the use of
     * com.trendyol.framework.Context when available.
     *
     * @param context
     *            A replacement context.
     */
    public static void overrideInstance(Context context) {
        instance = context;
    }

    /**
     * The path to the properties file which configures the context.
     */
    private final String propsFile = "testframework.properties";
    public enum Browser {
        CHROME, IE, FIREFOX,  unknown
    };

    /**
     * The name of the property which will contain the name of the browser which
     * will be used during a paritcular test run.
     */
    public final String pBrowser = "browser";

    protected Context() {
        init();
    }

    /**
     * Returns a singleton instance of Context
     *
     * @return A singleton instance of Context.
     */
    public static  Context getInstance() {
        if (instance == null) {

            instance = new Context();
            try {
                instance = (Context) Class.forName(
                        "com.trendyol.framework.Context").newInstance();
            } catch (Exception e) {
            }
        }

        return instance;
    }

    /**
     * Initialises default values of important properties.
     */
    protected void initDefaults() {
        defaults = new Properties();

        // Execution context properties
        defaults.setProperty(pBrowser, Browser.CHROME.toString());
        defaults.setProperty(pEnvironment, EnvironmentFactory.defaultEnvId);
    }

    /**
     * Returns a handle to the environment factory being used by the context.
     * @return  The environment factory.
     */
    public EnvironmentFactory getEnvironmentFactory() {
        return environmentFactory;
    }
    /**
     * Initialises the context by loading properties from disk. If the file does
     * not exist, it is created with default values.
     *
     * @return <code>true</code> if successful, <code>false</code> if not.
     */
    public boolean init() {

        boolean result = true;

        props = new Properties();
        initDefaults();

        try {
            props.load(new FileReader(propsFile));
        } catch (FileNotFoundException fnfe) {

            // Do nothing. File will be saved with default values by the end of
            // the method.

        } catch (IOException ioe) {

            // Unkown IO error. Print stack trace and stop tests.
            ioe.printStackTrace();
            result = false;
        }

        save();

        return result;
    }

    /**
     * Saves the properties to file. Defaults are saved where the property value
     * has not been specified.
     */
    public void save() {

        // First ensure that all default values are explicitly set.
        // This helps users know what's configurable when they examine
        // testframework.properties.

        Enumeration<Object> keys = defaults.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            props.setProperty(key,
                    props.getProperty(key, defaults.getProperty(key)));
        }

        // Save all properties to disk
        try {
            props.store(new FileWriter(propsFile),
                    "Properties configuring the Test Framework.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Retrieves the environment against which this test run is being executed.
     *
     * @return Environment instance
     * @throws Exception
     *             if the environment is not recognised.
     */
    public Environment getEnvironment() throws Exception {

        Environment result = null;

        String sEnv = System.getProperty(pEnvironment);

        if (sEnv == null || sEnv.trim().length() == 0) {
            sEnv = props.getProperty(pEnvironment,
                    defaults.getProperty(pEnvironment));
        }

        result = environmentFactory.getEnvironment(sEnv);

        if (result == null) {
            throw new Exception("Unrecognised test environment: " + sEnv);
        }

        setEnvironment(result);

        return result;
    }

    /**
     * Sets the environment against which tests will be executed.
     *
     * @param env
     *            The environment
     */
    public void setEnvironment(Environment env) {
        props.setProperty(pEnvironment, env.id);
    }

    /**
     * Retrieves the browser on which this test run is being executed.
     *
     * @return Browser (enumeration) instance
     */
    public Browser getBrowserType() {

        Browser result = Browser.unknown;

        String sBrowser = System.getProperty(pBrowser);

        if (sBrowser == null || sBrowser.length() == 0) {
            sBrowser = props.getProperty(pBrowser);
        }

        for (Browser browser : Browser.values()) {
            if (browser.toString().equalsIgnoreCase(sBrowser)) {
                result = browser;
                break;
            }
        }

        // Save property in case it came from system property
        setBrowser(result);

        return result;
    }

    /**
     * Sets the browser in which tests will execute.
     *
     * @param newBrowser
     *            The browser
     */
    public void setBrowser(Browser newBrowser) {
        props.setProperty(pBrowser, newBrowser.toString());
    }

    /**
     * Returns the maximum number of threads to be used by the current test run.
     *
     * @return The maximum number of threads to be used by the current test run.
     */
    public int getMaxThreads() {
        int result = 1;

        try {
            result = Integer.parseInt(System.getProperty("threads", "1"));
        } catch (Exception e) {
            // Do nothing. Simply return default value;
        }

        return result;
    }


    /**
     * Adds a shutdown hook to the java runtime and keeps track of it. Tracking
     * is required so that we can run shutdown hooks manually in the case of
     * single-threaded execution.
     *
     * @param hook
     */
    public void addShutdownHook(Thread hook) {
        Runtime.getRuntime().addShutdownHook(hook);
        shutdownHooks.add(hook);
    }

    /**
     * Executes all shutdown hooks. This is used in single-threaded mode whereby
     * Cucumber's default behaviour of calling System.exit() after a test-run is
     * disabled. Therefore, we must run shutdown hooks manually.
     */
    public void runShutdownHooks() {
        for (Thread hook : shutdownHooks) {
            hook.start();
            // Ensure the hook is not executed again when System.exit() is
            // called.
            Runtime.getRuntime().removeShutdownHook(hook);
        }
    }

    public String getWorkingDirectory() {
        String prop = System.getProperty("workingdir");

        if (prop == null) {
            prop = ".";
        }

        return new File(prop).getAbsolutePath();
    }

    public String getAbsolutePath(String relativePath) {
        return getWorkingDirectory() + File.separator + relativePath;
    }

    /**
     * This is called before a scenario starts so that the context can clean or prepare its state.
     */
    public void onStartScenario() {

    }

    /**
     * This is called after a scenario starts so that the context can clean or prepare its state, do reporting etc.
     */
    public void onEndScenario() {

    }
}
