package com.trendyol.framework;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentFactory {

    protected Map<String, Environment> environments = new HashMap<String, Environment>();

    private static EnvironmentFactory instance = null;

    public static String defaultEnvId = "TestEnvironment";

    String environmentsFileLocation = "target" + File.separator + "test-classes"
            + File.separator + File.separator + "config"
            + File.separator + "/environments.json";

    protected EnvironmentFactory() {
        initEnvironments();
    }

    public static EnvironmentFactory getInstance()  {
        if (instance == null) {
            instance = new EnvironmentFactory();
        }

        return instance;
    }

    public void initEnvironments()  {
        //Read environments config file
        String envsString = null;
        try {
            envsString = FileUtils.readFileContentsAsString(environmentsFileLocation);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Environments envs = new GsonBuilder().create().fromJson(envsString, Environments.class);

        for (Environment env : envs.environments) {
            environments.put(env.id, env);
        }
    }

    public Collection<Environment> getEnvironments() {
        return environments.values();
    }

    /**
     * Retrieves the environment against which this test run is being executed.
     *
     * @return Environment instance
     * @throws Exception
     *             if the environment is not recognised.
     */
    public Environment getEnvironment(String id) throws Exception {
        return environments.get(id);
    }

}
