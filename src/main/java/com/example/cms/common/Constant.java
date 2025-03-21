package com.example.cms.common;

import java.util.List;

public class Constant {

    public static final String ROOT_PATH = "/api/hrm-cms/";
    public static final String TRANSACTION_ID_KEY = "transactionId";
    public static final String ACCESS_STRING = "access";
    public static final String REFRESH_STRING = "refresh";
    public static final String URL_IMG_AVATAR = "/images/hrm_avt/";
    public static final String URL_IMG_FILE = "/images/hrm_file/";

    public interface IGNORE_URL {
        String LOGIN = "/api/hrm-app/auth";
        String CHECK_ACCESS_TOKEN = "/api/hrm-app/token/check-accessToken";
        List<String> ALL = List.of(LOGIN, CHECK_ACCESS_TOKEN);
        String[] ALL_BPASS_SECURITY = new String[]{"/**"};
    }

    public interface RESPONSE_KEY {
        String RESULT = "RESULT_KEY";
        String DATA = "DATA_KEY";
    }

    public interface STATUS {
        int ACTIVE = 0;
        int IN_ACTIVE = 1;
        boolean IS_DELETED = true;
        boolean IS_UN_DELETED = false;
    }

    public interface SPECIAL_CHAR {
        String SPACE = " ";
        String SEMI_COLON = ";";
        String COMMAS = ",";
        String UNDER_LINE = "_";
        String HYPHEN = "-";
        String SLASH = "/";
        String EMPTY = "";
        String COLON = ":";
        String PERCENT = "%";
    }

    public interface IMAGE_EXTENSION {
        String PNG = "PNG";
        String DOT_PNG = ".PNG";
    }
}
