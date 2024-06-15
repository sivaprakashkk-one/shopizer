package com.salesmanager.core.model.shipping;

import java.io.Serializable;
import java.math.BigDecimal;

public class CustomPricing implements Serializable {
    private String product;

    private String shippingCost;

    private String unit;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(String shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getShippingCostBigDecimal() {
        return new BigDecimal(shippingCost);
    }

    public BigDecimal getUnitBigDecimal() {
        return new BigDecimal(unit);
    }
}
