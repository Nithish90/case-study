package com.trendyol.pageobjects.interfaces;

import com.trendyol.framework.PageObjectInterface;

public interface TrendyolLoginPagePageObjectInterface extends PageObjectInterface {

     void login( String email, String password) throws Exception;
     boolean isLoggedIn (String email) throws Exception;
     void logout() throws Exception;
}
