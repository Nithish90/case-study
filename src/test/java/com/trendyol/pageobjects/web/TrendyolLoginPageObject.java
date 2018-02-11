package com.trendyol.pageobjects.web;

import com.trendyol.framework.Context;
import com.trendyol.framework.FunctionWeb;
import com.trendyol.framework.Helper;
import com.trendyol.framework.PageObject;
import com.trendyol.pageobjects.interfaces.TrendyolLoginPagePageObjectInterface;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class TrendyolLoginPageObject extends PageObject implements TrendyolLoginPagePageObjectInterface {

    final static Logger logger = Logger.getLogger(TrendyolLoginPageObject.class);
    private String popupClass="fancybox-skin";
    private String emailId="email";
    private String passwordId="password";
    private String loginContainerClass="login-container";
    private String loginPanelClass="login-panel-container";
    private String loginSubmitId="loginSubmit";
    private String userEmailClass="user-email";
    private String logoutClass="account-link-container";

    public TrendyolLoginPageObject() throws Exception {
        super();
    }

    public void navigateTo() throws Exception {
        browser.manage().deleteAllCookies();
        browser.get(Context.getInstance().getEnvironment().properties.get("customerBaseUrl"));
        if(browser.findElements(By.className(popupClass)).size()>0)
            Helper.clickObjectByClassdWithTag(popupClass,"a",2,browser);
    }

    public void login( String email, String password) throws Exception {

        Helper.mouseMove(browser.findElement(By.className(loginContainerClass)),browser);
        Helper.clickObjectByClassdWithTag(loginPanelClass,"div",0,browser);
        Helper.setObjectById(emailId,email,browser);
        Helper.clickObjectById(passwordId,browser);//Added for IE bug
        Helper.setObjectById(passwordId,password,browser);
        Helper.clickObjectById(loginSubmitId,browser);
    }

    public boolean isLoggedIn(String email) throws Exception{

        Helper.mouseMove(browser.findElement(By.className(loginContainerClass)),browser);

        return Helper.getTextByClass(userEmailClass,browser).equals(email);
    }

    public void logout() throws Exception{
        Helper.mouseMove(browser.findElement(By.className(loginContainerClass)),browser);
        browser.findElements(By.className(logoutClass)).get(7).click();
    }
}


