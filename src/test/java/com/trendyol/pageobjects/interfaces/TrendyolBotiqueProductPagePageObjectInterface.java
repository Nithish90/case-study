package com.trendyol.pageobjects.interfaces;

import com.trendyol.framework.PageObjectInterface;

public interface TrendyolBotiqueProductPagePageObjectInterface extends PageObjectInterface {
    void openProductDetail() throws Exception;
    String addToBasket() throws Exception;
    void goToBasket() throws Exception;
    String getProductId() throws Exception;
    void removeFromBasket() throws Exception;
}
