package com.example.doan.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.doan.R;
import com.example.doan.data.Note;
import com.example.doan.data.NoteDao;
import com.example.doan.data.NoteDatabase;
import com.example.doan.ui.addedit.AddEditNoteActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private NoteDao noteDao;
    private NoteAdapter adapter;
    private RecyclerView recyclerView;

    private LiveData<List<Note>> currentLiveData;
    private Note lastDeletedNote;

    private String currentSort = "newest";
    private String currentFilterCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        createNotificationChannel();

        noteDao = NoteDatabase.getInstance(this).noteDao();

        recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddNote);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });

        attachSwipeToDelete();
        loadNotesWithCurrentOptions();
    }

    private void loadNotesWithCurrentOptions() {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        if (!currentFilterCategory.equals("All")) {
            currentLiveData = noteDao.getNotesByCategory(currentFilterCategory);
        } else {
            switch (currentSort) {
                case "oldest":
                    currentLiveData = noteDao.getOldestNotes();
                    break;
                case "az":
                    currentLiveData = noteDao.getTitleAZ();
                    break;
                case "za":
                    currentLiveData = noteDao.getTitleZA();
                    break;
                case "newest":
                default:
                    currentLiveData = noteDao.getNewestNotes();
            }
        }

        currentLiveData.observe(this, notes -> adapter.setNotes(notes));
    }

    private void searchNotes(String keyword) {
        if (currentLiveData != null) currentLiveData.removeObservers(this);
        currentLiveData = noteDao.searchNotes(keyword);
        currentLiveData.observe(this, notes -> adapter.setNotes(notes));
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        lastDeletedNote = adapter.getNoteAt(position);

                        NoteDatabase.databaseWriteExecutor.execute(() ->
                                noteDao.delete(lastDeletedNote));

                        Snackbar.make(recyclerView, "Đã xóa ghi chú", Snackbar.LENGTH_LONG)
                                .setAction("HOÀN TÁC", v -> {
                                    NoteDatabase.databaseWriteExecutor.execute(() ->
                                            noteDao.insert(lastDeletedNote));
                                }).show();
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    // ===== Menu / Search =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm kiếm ghi chú…");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotes(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            View view = findViewById(R.id.action_sort);
            showSortMenu(view);
            return true;
        } else if (id == R.id.action_filter) {
            View view = findViewById(R.id.action_filter);
            showFilterMenu(view);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Mới nhất");
        popup.getMenu().add("Cũ nhất");
        popup.getMenu().add("Tiêu đề A-Z");
        popup.getMenu().add("Tiêu đề Z-A");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Mới nhất")) currentSort = "newest";
            else if (title.equals("Cũ nhất")) currentSort = "oldest";
            else if (title.equals("Tiêu đề A-Z")) currentSort = "az";
            else if (title.equals("Tiêu đề Z-A")) currentSort = "za";

            currentFilterCategory = "All";
            loadNotesWithCurrentOptions();
            return true;
        });
        popup.show();
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("All");
        popup.getMenu().add("Work");
        popup.getMenu().add("Study");
        popup.getMenu().add("Personal");
        popup.getMenu().add("Other");
        popup.setOnMenuItemClickListener(item -> {
            currentFilterCategory = item.getTitle().toString();
            loadNotesWithCurrentOptions();
            return true;
        });
        popup.show();
    }

    // ===== Handler click từ adapter =====
    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(View anchorView, Note note) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add("Ghim/Bỏ ghim");
        popup.getMenu().add("Sửa");
        popup.getMenu().add("Chia sẻ");
        popup.getMenu().add("Xóa");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Ghim/Bỏ ghim")) {
                note.setPinned(!note.isPinned());
                NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.update(note));
            } else if (title.equals("Sửa")) {
                Intent intent = new Intent(this, AddEditNoteActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            } else if (title.equals("Chia sẻ")) {
                shareNote(note);
            } else if (title.equals("Xóa")) {
                lastDeletedNote = note;
                NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.delete(note));
                Snackbar.make(recyclerView, "Đã xóa ghi chú", Snackbar.LENGTH_LONG)
                        .setAction("HOÀN TÁC", v ->
                                NoteDatabase.databaseWriteExecutor.execute(
                                        () -> noteDao.insert(lastDeletedNote)
                                )).show();
            }
            return true;
        });
        popup.show();
    }

    @Override
    public void onPinClick(Note note) {
        note.setPinned(!note.isPinned());
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.update(note));
    }

    private void shareNote(Note note) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        String text = note.getTitle() + "\n\n" + note.getContent();
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ ghi chú"));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "doan_note_channel";
            String name = "Nhắc nhở ghi chú";
            NotificationChannel channel = new NotificationChannel(
                    id, name, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
