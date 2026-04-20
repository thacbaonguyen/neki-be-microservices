package com.thacbao.common.utils;

public class MessageKey {
    // User
    public static final String USER_NOT_LOGIN = "Người dùng chưa đăng nhập";
    public static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
    public static final String USER_CANNOT_REVIEW = "Bạn không có quyền đánh giá sản phẩm này";

    // Product
    public static final String PRODUCT_NOT_FOUND = "Không tìm thấy sản phẩm";
    public static final String PRODUCT_DOESNT_ACTIVE = "Sản phẩm không khả dụng";
    public static final String PRODUCT_NOT_ENOUGH = "Sản phẩm trong kho không đủ";
    public static final String QUANTITY_SO_BIG = "Số lượng không được quá lớn";

    // Others
    public static final String WISH_LIST_NOT_FOUND = "Không tìm thấy wishlist";
    public static final String VARIANT_NOT_FOUND = "Không tìm thấy biến thể của sản phẩm";
    public static final String INVALID_INPUT = "Đầu vào không hợp lệ";

    // Order
    public static final String ORDER_NOT_FOUND = "Không tìm thấy đơn hàng";
    public static final String CART_EMPTY = "Giỏ hàng trống";
    public static final String ORDER_CANNOT_CANCEL = "Không thể hủy đơn hàng này";
    public static final String ORDER_INVALID_STATUS = "Trạng thái đơn hàng không hợp lệ";
    public static final String ORDER_ACCESS_DENIED = "Bạn không có quyền truy cập đơn hàng này";
    public static final String INVALID_STATUS_TRANSITION = "Không thể chuyển sang trạng thái này";
    public static final String PAYMENT_METHOD_NOT_FOUND = "Không thể tìm thấy phương thức thanh toán";
    public static final String DISCOUNT_NOT_FOUND = "Không tìm thấy mã giảm giá";
    public static final String DISCOUNT_EXPIRED = "Mã giảm giá đã hết hạn";
    public static final String DISCOUNT_NOT_STARTED = "Mã giảm giá chưa đến thời gian sử dụng";
    public static final String DISCOUNT_USAGE_LIMIT_REACHED = "Mã giảm giá đã hết lượt sử dụng";
    public static final String DISCOUNT_USER_USAGE_LIMIT_REACHED = "Bạn đã hết lượt sử dụng mã giảm giá này";
    public static final String DISCOUNT_MIN_ORDER_NOT_MET = "Đơn hàng không đủ giá trị tối thiểu để áp dụng mã này";
    public static final String DISCOUNT_INACTIVE = "Mã giảm giá hiện không khả dụng";
    public static final String REVIEW_NOT_FOUND = "Khong tìm thấy đánh giá";
    public static final String CANNOT_UPDATE_REVIEW = "Bạn không thể sửa review này";
}
