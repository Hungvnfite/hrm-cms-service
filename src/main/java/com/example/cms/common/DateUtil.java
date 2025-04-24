package com.example.cms.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DateUtil {
    public static final String YYYY_MM_DD_HYPHEN = "yyyy-MM-dd";
    public static final String FULL_DATE = "HH:mm dd/MM/yyyy";
    public static final String dd_MM_yyyy_WITH_SLASH = "dd/MM/yyyy";
    public static final String dd_MM_yyyy_HH_mm_WITH_SLASH = "dd/MM/yyyy HH:mm";
    public static final String yyyy_MM_dd_HH_mm_SS = "yyyy-MM-dd HH:mm:ss";

    public static Date convert(String input, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String format(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception ex) {
            return Constant.SPECIAL_CHAR.EMPTY;
        }
    }

    // format date từ FE để truy vấn trong mongoDB, áp dụng với lấy ra bản ghi trong khoảng ngày
    public static String buildDateRegex(LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        // Lấy tháng từ fromDate (Ví dụ: "Mar")
        String month = fromDate.format(formatter);

        // Lấy năm từ fromDate
        int year = fromDate.getYear();

        // Lấy danh sách ngày từ fromDate đến toDate
        String daysPattern = IntStream.rangeClosed(fromDate.getDayOfMonth(), toDate.getDayOfMonth())
                .mapToObj(day -> String.format("%02d", day)) // Chuyển về dạng "09", "10", ...
                .collect(Collectors.joining("|")); // Kết hợp thành "09|10|11|..."

        // Tạo regex hoàn chỉnh
        return ".*" + month + " (" + daysPattern + ") .* " + year;
    }

    public static String genCreatedAt(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // Timezone của Việt Nam (GMT+7)
        return sdf.format(Objects.requireNonNullElseGet(timestamp, Date::new));
    }

//    public static long calculateDaysBetweenInclusive(String startDateStr, String endDateStr) throws Exception {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//        Date startDate = sdf.parse(startDateStr);
//        Date endDate = sdf.parse(endDateStr);
//
//        long diffInMillies = endDate.getTime() - startDate.getTime();
//
//        // Cộng thêm 1 để tính cả ngày đầu và ngày cuối
//        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
//    }

    public static long calculateDaysBetweenInclusive(String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // ChronoUnit.DAYS.between() không bao gồm ngày cuối, nên cộng thêm 1
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}
