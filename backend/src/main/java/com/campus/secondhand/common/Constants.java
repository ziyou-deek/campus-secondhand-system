package com.campus.secondhand.common;

public final class Constants {
    private Constants() {}

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final int USER_STATUS_DISABLED = 0;
    public static final int USER_STATUS_NORMAL = 1;

    /** 商品：0待审核 1在售 2已下架 3已售出 4审核拒绝 5交易中 */
    public static final int PRODUCT_PENDING = 0;
    public static final int PRODUCT_ON_SALE = 1;
    public static final int PRODUCT_OFF = 2;
    public static final int PRODUCT_SOLD = 3;
    public static final int PRODUCT_REJECTED = 4;
    public static final int PRODUCT_RESERVED = 5;

    /** 订单：0待交易 1交易中 2已完成 3已取消 */
    public static final int ORDER_PENDING = 0;
    public static final int ORDER_TRADING = 1;
    public static final int ORDER_DONE = 2;
    public static final int ORDER_CANCELLED = 3;
}
