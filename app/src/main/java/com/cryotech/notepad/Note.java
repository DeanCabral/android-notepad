package com.cryotech.notepad;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Note implements Serializable {

    private long dateTime;
    private String title;
    private String content;
    private boolean lock;
    private String code;

    public Note(long dateTime, String title, String content, boolean lock, String code) {
        this.dateTime = dateTime;
        this.title = title;
        this.content = content;
        this.lock = lock;
        this.code = code;
    }

    public long getDateTime() {
        return dateTime;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean getLock() {
        return lock;
    }

    public String getCode() {
        return code;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDateFormat()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(dateTime));
    }

    public String getTimeFormat()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(dateTime));
    }

    public Date getDateTimeFormat()
    {
        return new Date(dateTime);
    }
}
