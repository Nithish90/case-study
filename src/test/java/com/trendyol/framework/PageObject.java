package com.trendyol.framework;

import org.openqa.selenium.WebDriver;

public abstract class PageObject implements PageObjectInterface {
    protected WebDriver browser;
    protected Context context = Context.getInstance();

    /**
     * Instantiates the page object given a reference to a WebDriver.
     *
     * @param browser
     */
    public PageObject(WebDriver browser) throws Exception {

        if (browser == null) {
            this.browser = FunctionWeb.getBrowser();
        } else {
            this.browser = browser;
        }

    }

    /**
     * Instantiates the page object using a default web browser.
     */
    public PageObject()  throws Exception {
        this(null);
    }

    /**
     * Navigates to the page being represented, ideally using the same method
     * that a user would. <BR>
     * <BR>
     * <b>Note:</b>Should be implemented by every page object.
     *
     */
    public abstract void navigateTo() throws Exception;
}
