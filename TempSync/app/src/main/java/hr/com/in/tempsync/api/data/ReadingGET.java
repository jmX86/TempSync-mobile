package hr.com.in.tempsync.api.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;

public class ReadingGET {
    private String value;
    private Timestamp timestamp;

    public ReadingGET(String value, Timestamp timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        ZonedDateTime dateTime = Instant.ofEpochMilli(timestamp.getTime()).atZone(ZoneOffset.systemDefault());
        return dateTime.toLocalDateTime().format(format);
    }

    public long getTimestampInt(){
        return timestamp.getTime();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
