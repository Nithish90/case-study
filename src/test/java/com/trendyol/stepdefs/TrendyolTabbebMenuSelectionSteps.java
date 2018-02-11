package com.trendyol.stepdefs;

import com.trendyol.framework.FunctionWeb;
import com.trendyol.pageobjects.interfaces.TrendyolTabbedMenuPagePageObjectInterface;
import com.trendyol.pageobjects.web.TrendyolTabbedMenuPageObject;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;

public class TrendyolTabbebMenuSelectionSteps {
    TrendyolTabbedMenuPagePageObjectInterface menuPages;

    public TrendyolTabbebMenuSelectionSteps() throws Exception {
        menuPages=(TrendyolTabbedMenuPagePageObjectInterface) new TrendyolTabbedMenuPageObject();
    }
    @Then("^Page should be loaded properly$")
    public void pageShouldBeLoadedProperly() throws Throwable {
        assertTrue(FunctionWeb.waitForPageLoaded());

    }

    @When("^I open a menu (\\d+) category$")
    public void iOpenAMenuCategory(int no) throws Throwable {
        menuPages.setTabNo(no);
        menuPages.navigateTo();
        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
        FunctionWeb.validateInvalidImages();
    }
}
