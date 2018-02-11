package com.trendyol.pageobjects.web;

import com.trendyol.framework.Helper;
import com.trendyol.framework.PageObject;
import com.trendyol.pageobjects.interfaces.TrendyolBotiqueProductPagePageObjectInterface;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;

public class TrendyolBotiqueProductPageObject extends PageObject implements TrendyolBotiqueProductPagePageObjectInterface {

    final static Logger logger = Logger.getLogger(TrendyolBotiqueProductPageObject.class);
    private String boutiqueId="dynamic-boutiques";
    private String basketListId="myBasketListItem";
    private String productClass="product";
    private String productId="product-id";
    private String basketClass="addtobasket-box";
    private String basketButtonId="addToBasketButton";
    private String basketListItemId="myBasketListItem";
    private String removeItemClass="removeitem";
    private String goBasketClass="goBasket";
    private String removeItemConfirmClass="lighboxcontainer";
    private String productContainerClass="productsContainer";

    public TrendyolBotiqueProductPageObject() throws Exception {
        super();
    }

    public void navigateTo() throws Exception {

        Helper.clickObjectByIdWithTag(boutiqueId,"a",0,browser);

    }

    public void openProductDetail() throws Exception {

        Helper.mouseMove(browser.findElement(By.id(basketListId)),browser);
        WebElement element=browser.findElements(By.className(productClass)).get(0).findElement(By.tagName("a"));
        //This is because geckodriver throws exception for click
        element.sendKeys(Keys.RETURN);
    }

    public String addToBasket() throws Exception {

        String hiddenProductId=browser.findElement(By.id(productId)).getAttribute("value");
        WebElement element=browser.findElement(By.className(basketClass)).findElements(By.tagName("button")).get(0);

        element.sendKeys(Keys.RETURN);  //This is because geckodriver throws exception for click

        for(int i=0;;i++) {
            element = browser.findElement(By.className(basketClass)).findElements(By.tagName("a")).get(i);

            if (element.getAttribute("class").equals("") ) {
                element.sendKeys(Keys.RETURN);  //This is because geckodriver throws exception for click
                break;
            }
        }
        Helper.clickObjectById(basketButtonId,browser);

        return hiddenProductId;
    }

    public void goToBasket() throws Exception {

        Helper.mouseMove(browser.findElement(By.id(basketListItemId)),browser);
        Helper.clickObjectByClassName(goBasketClass,browser);
    }

    public void removeFromBasket() throws Exception {
        Helper.clickObjectByClassName(removeItemClass,browser);
        Helper.clickObjectByClassdWithTag(removeItemConfirmClass,"a",0,browser);
    }

    public String getProductId() throws Exception {

        return browser.findElement(By.className(productContainerClass)).findElements(By.tagName("a")).get(0).getAttribute("href");
    }
}
