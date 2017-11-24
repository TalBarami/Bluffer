package Implementation.Tools;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom logger formatter
 */
public class LogFormatter extends Formatter {
    /**
     * Format one line of the log record- include only the message (without time,date etc)
     *
     * @param record the log record line
     * @return A string with the requested format
     */
    public String format(LogRecord record) {
        StringBuffer buffer = new StringBuffer(1000);
        buffer.append(formatMessage(record));
        buffer.append("\n");
        return buffer.toString();
    }
}
