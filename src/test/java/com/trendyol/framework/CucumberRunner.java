package com.trendyol.framework;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.*;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class CucumberRunner {

    boolean success = true;

    Context context;

    int maxThreads = 1;

    public static String featuresLocation = "target" + File.separator + "test-classes"
            + File.separator + "cucumber" + File.separator + "features";

    List<Process> processes;
    List<String> featureFiles;

    Map<Process, Integer> processMap = new HashMap<Process, Integer>();
    int lastProcessID = 0;

    public static void main(String[] args) throws Throwable {
        if (System.getProperty("skipTestFramework") == null) {
            new CucumberRunner();
        } else {
            System.out.println("\n\n**************************************************************");
            System.out.println("* skipTestFramework flag is set - skipping integration tests *");
            System.out.println("**************************************************************\n\n");
        }
    }

    public CucumberRunner() throws Throwable {

        // Obtain test run info and display
        context = Context.getInstance();
        maxThreads = context.getMaxThreads();

        // Build feature files list
        buildFeatureFilesList();

        // Prepare exploded files if necessary
        if (maxThreads > 1) {
            preprocessFeatureFiles();
            buildFeatureFilesList();
        }

        // Display test run parameters to user
        System.out.println("\n\n===========================================");
        System.out.println("Test execution parameters");
        System.out.println("===========================================");
        System.out.println("Max threads: " + maxThreads);
        System.out.println("Environment: " + context.getEnvironment().id);
        System.out.println("Browser:     "
                + context.getBrowserType().toString());

        System.out.println("Features:    " + featureFiles.size());
        System.out.println("Context:     "
                + context.getClass().getCanonicalName());
        System.out.println("Working Dir: " + context.getWorkingDirectory());
        System.out.println("===========================================\n\n");

        // Save context to maintain consistency with forked processes
        // TODO: Find a better mechanism for this which preserves current
        // properties file
        context.save();

        // Extract any required resources from the test framework
        System.out.println("Extracting resources from the test framework");
        extractResources(context.getBrowserType());
        System.out.println("Resource extraction complete. Launching tests.\n");

        // Make magic happen
        manageThreads();

        //Exit nicely
        FunctionWeb.shutDown();
        //System.exit(0);
    }

    public static void extractResourceToFile(String resource, String path) {
        extractResourceToFile(resource, path, false);
    }

    public static void extractResourceToFile(String resource, String path,
                                             boolean makeExecutable) {

        System.out.println("===> Extracting " + resource + " to " + path);

        BufferedInputStream reader = new BufferedInputStream(Thread
                .currentThread().getContextClassLoader()
                .getResourceAsStream(resource));

        try {

            // First check that required directory structure exists and create
            // if required
            File f = new File(path).getParentFile();

            if (!f.exists()) {
                f.mkdirs();
            }

            // Write resource to file
            BufferedOutputStream writer = new BufferedOutputStream(
                    new FileOutputStream(path));

            while (reader.available() > 0) {
                byte[] buffer = new byte[reader.available()];
                reader.read(buffer);
                writer.write(buffer);
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (makeExecutable) {
            // This will only work on Linux and Mac
            try {
                String cmd = "chmod +x " + path;
                System.out.println("===> " + cmd);
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void extractResources(Context.Browser browserType) {

        // Windows-specific resources
        if (System.getProperty("os.name").toLowerCase().contains("win")) {

            if(browserType.equals(Context.Browser.CHROME))
                extractResourceToFile("binaries/windows/chromedriver/chromedriver.exe",
                    "target" + File.separator + "test-classes" + File.separator
                            + "binaries" + File.separator + "chromedriver"
                            + File.separator + "chromedriver.exe");
            else if(browserType.equals(Context.Browser.FIREFOX))
                extractResourceToFile("binaries/windows/firefoxdriver/geckodriver.exe",
                        "target" + File.separator + "test-classes" + File.separator
                                + "binaries" + File.separator + "firefoxdriver"
                                + File.separator + "geckodriver.exe");
            else if(browserType.equals(Context.Browser.IE))
                extractResourceToFile("binaries/windows/iedriver/IEDriverServer.exe",
                        "target" + File.separator + "test-classes" + File.separator
                                + "binaries" + File.separator + "iedriver"
                                + File.separator + "IEDriverServer.exe");
        }

        // Mac-specific resources
        else if (System.getProperty("os.name").toLowerCase().contains("mac os")) {

            if(browserType.equals(Context.Browser.CHROME))
                extractResourceToFile("binaries/mac/chromedriver/chromedriver", "target"
                    + File.separator + "test-classes" + File.separator
                    + "binaries" + File.separator + "chromedriver"
                    + File.separator + "chromedriver", true);
            else if(browserType.equals(Context.Browser.FIREFOX))
                extractResourceToFile("binaries/mac/firefoxdriver/geckodriver", "target"
                        + File.separator + "test-classes" + File.separator
                        + "binaries" + File.separator + "firefoxdriver"
                        + File.separator + "geckodriver", true);
        } else {
            // Assume Linux OS
            if(browserType.equals(Context.Browser.CHROME))
                extractResourceToFile("binaries/linux/chromedriver/chromedriver", "target"
                    + File.separator + "test-classes" + File.separator
                    + "binaries" + File.separator + "chromedriver"
                    + File.separator + "chromedriver", true);
            else if(browserType.equals(Context.Browser.FIREFOX))
                extractResourceToFile("binaries/linux/firefoxdriver/geckodriver", "target"
                        + File.separator + "test-classes" + File.separator
                        + "binaries" + File.separator + "firefoxdriver"
                        + File.separator + "geckodriver", true);
        }

    }

    private void buildFeatureFilesList() throws Throwable {

        featureFiles = new ArrayList<String>();

        // Recursively list all files in featuresLocation
        File featuresDirectory = new File(
                context.getAbsolutePath(featuresLocation));
        Collection<File> allFiles = null;

        try {
            System.out.println("Looking for feature files in "
                    + featuresDirectory.getAbsolutePath());
            allFiles = org.apache.commons.io.FileUtils.listFiles(featuresDirectory,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            // Filter out files which are not feature files
            for (File f : allFiles) {
                if (f.getAbsolutePath().endsWith(".feature")) {
                    featureFiles.add(f.getAbsolutePath());
                }
            }
        } catch (IllegalArgumentException iae) {

            if (iae.getMessage().contains("is not a directory")) {
                throw new Exception(
                        "\n*****\n* Error: No feature files found.  Please place your feature files in a sub-directory of "
                                + featuresDirectory.getAbsolutePath()
                                + "\n*****\n");
            }
        }
    }

    private void killAllProcesses() {
        for (Process p : processes) {
            p.destroy();
        }
    }

    private void singleThreadExecution() throws Throwable {

        long startTime = System.currentTimeMillis();

        System.out.println("Running all features in "
                + context.getAbsolutePath(featuresLocation));

        List<String> args = new ArrayList<String>();
        args.add(context.getAbsolutePath(featuresLocation));
        args.add("--glue");
        args.add("com.trendyol.stepdefs");

        String resultsPostfix = Long.toString(System.currentTimeMillis() + (long) (Math.random() * 10000));
        args.add("--plugin");
        args.add("junit:"
                + context.getAbsolutePath("shippable/testresults/results-" + resultsPostfix + ".xml"));

        cucumber.runtime.Runtime runtime = null;

        try {
            // Main.main(args.toArray(new String[0]));

            // In the case of single-threaded execution, we re-implement code
            // from cucumber.api.cli.Main.main(String) because that method
            // currently uses System.exit() to terminate in this is not always
            // desirable for us. The following code is copied directly from
            // https://github.com/cucumber/cucumber-jvm/blob/master/core/src/main/java/cucumber/api/cli/Main.java
            // but the System.exit() command is not used.

            // IMPORTANT NOTE: Check this code when upgrading to new versions of
            // Cucumber

            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();

            MultiLoader resourceLoader = new MultiLoader(classLoader);

            ResourceLoaderClassFinder resourceLoaderClassFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

            RuntimeOptions runtimeOptions = new RuntimeOptions(Env.INSTANCE, args);
            runtime = new cucumber.runtime.Runtime(
                    resourceLoader, resourceLoaderClassFinder, classLoader, runtimeOptions);
            //runtime.writeStepdefsJson();
            runtime.run();

            // Since, we do not use System.exit() now, we need to run shutdown
            // hooks manually
            context.runShutdownHooks();

        } catch (CucumberException ce) {
            if (ce.getMessage().contains("None of the features at")) {
                System.out
                        .println("None of the features matched the filters provided.");
            } else {
                ce.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.out.println("\n\n===========================================");
        System.out.println("Total System Test Duration: "
                + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        System.out.println("===========================================\n\n");


        // Check exit status
        if (runtime.exitStatus() != 0) {

            List<Throwable> errors = runtime.getErrors();
            for (Throwable error : errors) {
                //throw error;
                error.printStackTrace();
            }
        }


    }

    private void manageThreads() throws Throwable {

        if (maxThreads == 1) {
            singleThreadExecution();
        } else {
            multiThreadExecution();

            if (!success) {
                throw new Exception("Cucumber errors detected.");
            }
        }
    }

    /**
     * Processes existing feature files and creates a multitude of smaller files in order to maximise
     * parallelisability.  Currently, we can only parallelise by pushing individual feature files onto
     * a thread for processing.  So we need to create smaller feature files to take advantage.
     */
    public void preprocessFeatureFiles() {

        int scenarioCount = 0;
        for (String featureFile : featureFiles) {
            int fileCount = 0;
            try {
                //Read in feature file
                String featureFileString = FileUtils.readFileContentsAsString(featureFile);

                Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
                GherkinDocument gherkinDocument = parser.parse(featureFileString, new TokenMatcher());

                //Process and write each scenario to a separate file
                String featureName = gherkinDocument.getFeature().getName();
                List<ScenarioDefinition> scenarios = gherkinDocument.getFeature().getChildren();

                for (ScenarioDefinition scenario : scenarios) {

                    ScenarioOutline scenarioOutline = null;
                    boolean isScenarioOutline = scenario instanceof ScenarioOutline;

                    StringBuffer sb = new StringBuffer();
                    sb.append("Feature: " + featureName);
                    sb.append("\n\n");

                    if (isScenarioOutline) {

                        //Multiply instances by examples and replace parameters with concrete values
                        Examples examples = scenarioOutline.getExamples().get(0);

                        List<TableRow> tableBody = examples.getTableBody();
                        List<TableCell> paramNames = examples.getTableHeader().getCells();
                        for (TableRow row : tableBody) {
                            String concreteScenario = sb.toString();
                            for (int i = 0; i < paramNames.size(); i++) {
                                String paramName = paramNames.get(i).getValue();
                                String paramValue = row.getCells().get(i).getValue();
                                concreteScenario = concreteScenario.replace("<" + paramName + ">", paramValue);
                            }
                            writeFeatureToFile(concreteScenario);
                        }

                    } else {
                        writeFeatureToFile(sb.toString());
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //Delete original feature files
        for (String originalFeatureFile : featureFiles) {
            (new File(originalFeatureFile)).delete();
        }

    }


    public String writeFeatureToFile(String feature) {

        File f = new File(featuresLocation);
        if (!f.exists()) {
            f.mkdirs();
        }

        String fileLocation = featuresLocation + File.separator + generateRandomString(8) + ".feature";

        try {
            FileUtils.saveStringInFile(fileLocation, feature);
        } catch (Exception e) {
            e.printStackTrace();
            fileLocation = null;
        }

        return fileLocation;
    }

    public static String generateRandomString(int length) {
        StringBuffer result = new StringBuffer();
        for (int i=0; i<length; i++) {
            result.append((char) randomInt(65,90));
        }

        return result.toString();
    }

    public static int randomInt(int min, int max) {

        return new Random().nextInt((max - min) + 1) + min;
    }

    public void multiThreadExecution() {

        //Assumption: preprocessFeatureFiles() has been called by this point.

        long startTime = System.currentTimeMillis();

        // Install a shutdown hook to kill all processes in case of a forced
        // shutdown
        context.addShutdownHook(new Thread() {
            public void run() {
                killAllProcesses();
            }
        });

        processes = new ArrayList<Process>();

        while (!featureFiles.isEmpty()) {

            // Check if a free thread exists
            if (processes.size() < maxThreads) {
                startProcess(featureFiles.get(0));

                try {
                    // Allow 0.25 seconds before starting the next process in order
                    // to mitigate risk of multiple processes trying to start a
                    // browser at exactly the same time
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                featureFiles.remove(0);
            } else {

                // Wait for a free thread
                try {
                    Thread.sleep(500);
                    for (Process process : processes) {

                        try {
                            int exitValue = process.exitValue();

                            // Check if there were any failures
                            success = success & (exitValue == 0);

                            dumpProcessOutput(process);
                            processes.remove(process);
                            break;
                        } catch (IllegalThreadStateException itse) {
                            // Thread is still alive
                        }

                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        // Wait for all threads to finish
        while (!processes.isEmpty()) {
            try {
                Thread.sleep(1000);
                for (Process process : processes) {
                    try {
                        int exitValue = process.exitValue();

                        // Check if there were any failures and update build
                        // success flag
                        success = success & (exitValue == 0);

                        dumpProcessOutput(process);
                        processes.remove(process);
                        break;
                    } catch (IllegalThreadStateException itse) {
                        // Thread is still alive
                    }

                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        System.out.println("\n\n===========================================");
        System.out.println("Total System Test Duration: "
                + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        System.out.println("===========================================\n\n");

    }

    private void startProcess(String featureFile) {

        lastProcessID++;
        int processID = lastProcessID;

        featureFile = new File(featureFile).getAbsolutePath();

        System.out.println("Running " + featureFile + " (Process ID: "
                + lastProcessID + ")");

        // Build Classpath

        StringBuffer sb = new StringBuffer();

        URL[] urls = ((URLClassLoader) (Thread.currentThread()
                .getContextClassLoader())).getURLs();

        for (URL url : urls) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }

            try {
                sb.append(new File(url.toURI()).getAbsolutePath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        String classpath = sb.toString();

        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin"
                + separator + "java";

        List<String> args = new ArrayList<String>();

        String resultsPostfix = Long.toString(System.currentTimeMillis() + (long) (Math.random() * 10000));
        args.add("--plugin");
        args.add("junit:"
                + context.getAbsolutePath("shippable/testresults/results-" + resultsPostfix + ".xml"));

        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process;

        try {
            process = processBuilder.start();
            processMap.put(process, new Integer(processID));
            processes.add(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dumpProcessOutput(Process p) {
        System.out.println("\n\n===========================================");
        System.out.println("Process output stream dump for process with ID "
                + processMap.get(p));
        System.out.println("===========================================");
        System.out.println(getProcessOutput(p));
        System.out.println("===========================================");
        System.out.println("Process error stream dump for process with ID "
                + processMap.get(p));
        System.out.println("===========================================");
        System.out.println(getProcessErrors(p));
        System.out.println("===========================================\n\n");

    }

    public String getProcessOutput(Process p) {

        StringBuffer result = new StringBuffer();

        InputStream is = p.getInputStream();

        try {
            while (is.available() > 0) {
                result.append((char) is.read());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    public String getProcessErrors(Process p) {

        StringBuffer result = new StringBuffer();

        InputStream is = p.getErrorStream();

        try {
            while (is.available() > 0) {
                result.append((char) is.read());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();

    }
}
