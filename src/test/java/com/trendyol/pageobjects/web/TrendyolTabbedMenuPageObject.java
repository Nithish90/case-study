package com.trendyol.pageobjects.web;

import com.trendyol.framework.Helper;
import com.trendyol.framework.PageObject;
import com.trendyol.pageobjects.interfaces.TrendyolTabbedMenuPagePageObjectInterface;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class TrendyolTabbedMenuPageObject  extends PageObject implements TrendyolTabbedMenuPagePageObjectInterface {

    final static Logger logger = Logger.getLogger(TrendyolTabbedMenuPageObject.class);
    private String menuItemIdId="item";
    private String myBasketListItemId="myBasketListItem";

    public TrendyolTabbedMenuPageObject() throws Exception {
        super();
    }

    public void navigateTo() throws Exception {

        Helper.clickObjectById(menuItemIdId,browser);

        Helper.mouseMove(browser.findElement(By.id(myBasketListItemId)),browser);
    }

    public void setTabNo(int no) throws Exception {

        menuItemIdId=menuItemIdId+no;

        if(logger.isInfoEnabled())
            logger.info("For menu number: "+no);
    }
}
