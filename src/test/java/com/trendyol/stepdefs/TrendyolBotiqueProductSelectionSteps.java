package com.trendyol.stepdefs;

import com.trendyol.framework.FunctionWeb;
import com.trendyol.pageobjects.interfaces.TrendyolBotiqueProductPagePageObjectInterface;
import com.trendyol.pageobjects.web.TrendyolBotiqueProductPageObject;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;

public class TrendyolBotiqueProductSelectionSteps {

    TrendyolBotiqueProductPagePageObjectInterface botiqueAndProductPage;
    String productId;

    public TrendyolBotiqueProductSelectionSteps() throws Exception {
        botiqueAndProductPage = (TrendyolBotiqueProductPagePageObjectInterface) new TrendyolBotiqueProductPageObject();
    }

    @And("^I add a product into the basket$")
    public void iAddAProductIntoTheBasket() throws Throwable{
        FunctionWeb.sleep(2000);
        productId=botiqueAndProductPage.addToBasket();
        botiqueAndProductPage.goToBasket();
    }

    @Then("^Product should be in the basket$")
    public void productShouldBeInTheBasket() throws Throwable {

        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
        String productInBasket=botiqueAndProductPage.getProductId();
        botiqueAndProductPage.removeFromBasket();

        assertTrue(productInBasket.contains(productId));
    }

    @And("^I open product detail$")
    public void iOpenProductDetail() throws Throwable {
        botiqueAndProductPage.navigateTo();
        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
        FunctionWeb.validateInvalidImages();
        botiqueAndProductPage.openProductDetail();
        if(!FunctionWeb.waitForPageLoaded())
            Assert.fail();
    }
}
