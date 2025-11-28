package com.example.doan.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    private static volatile NoteDatabase INSTANCE;

    public abstract NoteDao noteDao();

    // Thread pool để chạy Room không trên main thread
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static NoteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    NoteDatabase.class,
                                    "doan_notes_db"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
