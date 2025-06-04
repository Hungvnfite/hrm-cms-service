package com.example.cms.common;

public enum ResponseCode {

    SYSTEM("ERR_001_2000", "Lỗi hệ thống. Vui lòng thử lại sau!"),
    SESSION_WRONG("ERR_001_2001", "Phiên làm việc không hợp lệ. Vui lòng đăng nhập để thực hiện thao tác tiếp theo!"),
    SESSION_EXPIRED("ERR_001_2002", "Phiên làm việc đã hết hạn. Vui lòng thực hiện đăng nhập lại!"),
    TOKEN_INVALID("ERR_001_2003", "Token invalid!"),
    DATA_NOT_FOUND("ERR_001_2004", "Không có dữ liệu!"),

    LG_WRONG_USER("ERR_001_2005", "Sai thông tin tài khoản/ mật khẩu"),

    AUTHOR_NOT_ALLOW("ERR_001_2006", "Không đủ quyền để thực hiện thao tác"),
    ERROR_CANCEL_LEAVE("ERR_001_2007", "Bạn không thể hành động do đơn đã hủy!"),

    //Cấp mật khẩu
    ERROR_REISSUE_LOST_PASSWORD("ERR_001_2008", "Không hợp lệ! Tài khoản không yêu cầu cấp phát mật khẩu mới"),
    DOES_NOT_EXIST_ACCOUNT("ERR_001_2009", "Tài khoản không tồn tại!")
    ;

    private String code;
    private String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
