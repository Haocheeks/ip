import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public enum DateTimeFormat {

    DateFormatA("yyyy-MM-dd", true),
    DateFormatB("yyyy MM dd", true),
    DateFormatC("d/M/yyy", true),
    DateFormatD("d MMM yyyy", true),
    DateFormatE("MMM d yyyyy", true),
    DateTimeFormatA("yyyy-MM-dd HHmm", false),
    DateTimeFormatB("yyyy-MM-dd HH:mm", false),
    DateTimeFormatC("yyyy/MM/dd HHmm", false),
    DateTimeFormatD("yyyy/MM/dd HH:mm", false),
    DateTimeFormatE("d/M/yyyy HHmm", false),
    DateTimeFormatF("d/M/yyyy HH:mm", false),
    DateTimeFormatG("d/M/yyyy h:mma", false),
    DateTimeFormatH("d-M-yyyy HHmm", false),
    DateTimeFormatI("d-M-yyyy HH:mm", false),
    DateTimeFormatJ("d MMM yyyy HHmm", false),
    DateTimeFormatK("d MMM yyyy HH:mm", false),
    DateTimeFormatL("d MMM yyyy h:mma", false),
    DateTimeFormatM("MMM d yyyy HHmm", false),
    DateTimeFormatN("MMM d yyyy HH:mm", false);

    private final DateTimeFormatter formatter;
    private final boolean isDateOnly;

    DateTimeFormat(String format, boolean isDateOnly) {
        this.formatter = DateTimeFormatter.ofPattern(format);
        this.isDateOnly = isDateOnly;
    }

    public static DateTimeFormat getFormat(String dateTime) {

        for (DateTimeFormat format : DateTimeFormat.values()) {
            if (matchesFormat(dateTime, format.formatter, format.isDateOnly)) {
                return format;
            }
        }
        return null;
    }

    private static boolean matchesFormat(String dateTime, DateTimeFormatter format, boolean isDateOnly) {
        try {
            if (isDateOnly) {
                LocalDate.parse(dateTime, format);
            } else {
                LocalDateTime.parse(dateTime, format);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
