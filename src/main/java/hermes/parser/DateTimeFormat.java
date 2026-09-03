package hermes.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import hermes.HermesException;

/**
 * List of the various DateFormat and DateTimeFormat Hermes can accept.
 *
 * <P>This names the type of Date and DateTimeFormat users might input and converts
 * the String into a DateTimeFormatter.
 *
 * <P>A boolean variable called isDateOnly to differentiate between DateFormat and DateTimeFormat.
 */
public enum DateTimeFormat {

    ISO_DATE("yyyy-MM-dd", true),
    SPACED_ISO_DATE("yyyy MM dd", true),
    SLASHED_DATE("d/M/yyyy", true),
    NAMED_MONTH_DATE("d MMM yyyy", true),
    MONTH_FIRST_DATE("MMM d yyyy", true),
    ISO_DATE_TIME("yyyy-MM-dd HHmm", false),
    ISO_DATE_TIME_WITH_COLON("yyyy-MM-dd HH:mm", false),
    SLASHED_ISO_DATE_TIME("yyyy/MM/dd HHmm", false),
    SLASHED_ISO_DATE_TIME_WITH_COLON("yyyy/MM/dd HH:mm", false),
    SLASHED_DATE_TIME("d/M/yyyy HHmm", false),
    SLASHED_DATE_TIME_WITH_COLON("d/M/yyyy HH:mm", false),
    SLASHED_DATE_TIME_12_HOUR("d/M/yyyy h:mma", false),
    DASHED_DATE_TIME("d-M-yyyy HHmm", false),
    DASHED_DATE_TIME_WITH_COLON("d-M-yyyy HH:mm", false),
    NAMED_MONTH_DATE_TIME("d MMM yyyy HHmm", false),
    NAMED_MONTH_DATE_TIME_WITH_COLON("d MMM yyyy HH:mm", false),
    NAMED_MONTH_DATE_TIME_12_HOUR("d MMM yyyy h:mma", false),
    MONTH_FIRST_DATE_TIME("MMM d yyyy HHmm", false),
    MONTH_FIRST_DATE_TIME_WITH_COLON("MMM d yyyy HH:mm", false);

    private final DateTimeFormatter formatter;
    private final boolean isDateOnly;

    DateTimeFormat(String format, boolean isDateOnly) {
        this.formatter = DateTimeFormatter.ofPattern(format);
        this.isDateOnly = isDateOnly;
    }

    /**
     * Analyses a Date Time String and attempts to parse the value with an appropriate formatter.
     *
     * @param dateTime String of the date (and time) inputted by the users
     * @return the appropriate DateTimeFormatter if present else returns null
     */
    private static DateTimeFormat findMatchingFormat(String dateTime) {

        for (DateTimeFormat format : DateTimeFormat.values()) {
            if (matchesFormat(dateTime, format.formatter, format.isDateOnly)) {
                return format;
            }
        }
        return null;
    }

    /**
     * Helper function to check if the formatter is able to parse the date (and time) String input
     * from the user.
     *
     * @param dateTime String of the date (and time) the user inputted
     * @param formatter A DateTimeFormatter that will be used to attempt to parse the dateTime
     * @param isDateOnly Determines if LocalDate.parse or LocalDateTime.parse is used
     * @return true if format is able to parse the dateTime
     */
    private static boolean matchesFormat(String dateTime, DateTimeFormatter formatter, boolean isDateOnly) {
        try {
            if (isDateOnly) {
                LocalDate.parse(dateTime, formatter);
            } else {
                LocalDateTime.parse(dateTime, formatter);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public boolean isDateOnly() {
        return isDateOnly;
    }

    /**
     * Parses the date (and time) the user typed and converts it into a LocalDateTime.
     *
     * @param dateTime Date (and time) the user typed.
     * @return The input parsed as a LocalDateTime.
     * @throws HermesException If the input is not in a recognised format.
     */
    public static LocalDateTime parseDateTime(String dateTime) throws HermesException {
        DateTimeFormat format = DateTimeFormat.findMatchingFormat(dateTime);
        if (format == null) {
            throw new HermesException(" The date-time format " + dateTime
                    + " is not recognised, try formatting it as dd MM yyyy HHmm instead.");
        }
        DateTimeFormatter formatter = format.getFormatter();
        return format.isDateOnly
                ? LocalDate.parse(dateTime, formatter).atStartOfDay()
                : LocalDateTime.parse(dateTime, formatter);
    }


}
