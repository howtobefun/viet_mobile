package com.example.doan.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;

    private String category;    // Work / Study / Personal / Other
    private long createdAt;     // millis
    private boolean isPinned;   // ghim hay không

    @ColumnInfo(name = "color")
    private int color;          // màu card

    private long reminderTime;  // millis, 0 = không nhắc

    public Note(String title, String content, String category,
                long createdAt, boolean isPinned, int color, long reminderTime) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.isPinned = isPinned;
        this.color = color;
        this.reminderTime = reminderTime;
    }

    // Getter + Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }
}
