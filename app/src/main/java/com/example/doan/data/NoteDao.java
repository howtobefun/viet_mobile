package com.example.doan.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    // Lấy 1 note theo id (dùng cho màn sửa)
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteById(int id);

    // mặc định: ghim ở trên, mới nhất trước
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<Note>> getAllNotes();

    // sort
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<Note>> getNewestNotes();

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt ASC")
    LiveData<List<Note>> getOldestNotes();

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, title COLLATE NOCASE ASC")
    LiveData<List<Note>> getTitleAZ();

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, title COLLATE NOCASE DESC")
    LiveData<List<Note>> getTitleZA();

    // search theo title hoặc content
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' " +
            "OR content LIKE '%' || :keyword || '%' " +
            "ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<Note>> searchNotes(String keyword);

    // filter theo category
    @Query("SELECT * FROM notes WHERE (:category = 'All' OR category = :category) " +
            "ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<Note>> getNotesByCategory(String category);
}
