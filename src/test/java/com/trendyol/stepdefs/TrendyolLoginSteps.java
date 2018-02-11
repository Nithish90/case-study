package com.trendyol.stepdefs;

import com.trendyol.framework.Context;
import com.trendyol.framework.FunctionWeb;
import com.trendyol.pageobjects.interfaces.TrendyolLoginPagePageObjectInterface;
import com.trendyol.pageobjects.web.TrendyolLoginPageObject;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;


public class TrendyolLoginSteps {

    TrendyolLoginPagePageObjectInterface loginPage;

    public TrendyolLoginSteps() throws Exception {
        loginPage = (TrendyolLoginPagePageObjectInterface) new TrendyolLoginPageObject();
    }

    @When("^I login to trendyol with valid credentials$")
    public void iLoginToTrendyolWithValidCredentials() throws Throwable {

        String username = Context.getInstance().getEnvironment().properties.get("user");
        String password = Context.getInstance().getEnvironment().properties.get("password");
        loginPage.login(username, password);
        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
    }

    @Given("^I am a trendyol user$")
    public void iAmATrendyolUser() throws Throwable {
        loginPage.navigateTo();
        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
    }

    @Then("^I should be logged in$")
    public void iShouldBeLoggedIn() throws Throwable {
        assertTrue(loginPage.isLoggedIn(Context.getInstance().getEnvironment().properties.get("user")));
    }

    @And("^I should logged out$")
    public void iShouldLoggedOut() throws Throwable {
        loginPage.logout();
    }
}
