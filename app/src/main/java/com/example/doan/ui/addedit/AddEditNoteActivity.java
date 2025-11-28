package com.example.doan.ui.addedit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doan.R;
import com.example.doan.data.Note;
import com.example.doan.data.NoteDao;
import com.example.doan.data.NoteDatabase;
import com.example.doan.reminder.ReminderReceiver;

import java.util.Calendar;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText edtContent;
    private RadioGroup radioCategory;
    private TextView txtReminderInfo;

    private View viewColorYellow, viewColorOrange, viewColorRed,
            viewColorGreen, viewColorBlue, viewColorPurple;

    private View[] colorViews;
    private TextWatcher contentWatcher;
    private StyleSpan titleBoldSpan;
    private AbsoluteSizeSpan titleSizeSpan;
    private NoteDao noteDao;
    private int selectedColor;
    private long reminderTime = 0;

    private int noteId = -1;
    private Note editingNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        noteDao = NoteDatabase.getInstance(this).noteDao();

        edtContent = findViewById(R.id.edtContent);
        radioCategory = findViewById(R.id.radioCategory);
        txtReminderInfo = findViewById(R.id.txtReminderInfo);

        viewColorYellow = findViewById(R.id.viewColorYellow);
        viewColorOrange = findViewById(R.id.viewColorOrange);
        viewColorRed = findViewById(R.id.viewColorRed);
        viewColorGreen = findViewById(R.id.viewColorGreen);
        viewColorBlue = findViewById(R.id.viewColorBlue);
        viewColorPurple = findViewById(R.id.viewColorPurple);

        // màu mặc định
        selectedColor = ContextCompat.getColor(this, R.color.note_yellow);

        setupColorPicker();
        setupNoteEditor();

        Button btnPickReminder = findViewById(R.id.btnPickReminder);
        Button btnSave = findViewById(R.id.btnSave);

        btnPickReminder.setOnClickListener(v -> pickReminder());
        btnSave.setOnClickListener(v -> saveNote());

        // load note đang sửa
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            NoteDatabase.databaseWriteExecutor.execute(() -> {
                editingNote = noteDao.getNoteById(noteId);
                runOnUiThread(this::showEditingNote);
            });
        }
    }

    private void setupColorPicker() {
        colorViews = new View[]{
                viewColorYellow, viewColorOrange, viewColorRed,
                viewColorGreen, viewColorBlue, viewColorPurple
        };

        initColorView(viewColorYellow, R.color.note_yellow);
        initColorView(viewColorOrange, R.color.note_orange);
        initColorView(viewColorRed, R.color.note_red);
        initColorView(viewColorGreen, R.color.note_green);
        initColorView(viewColorBlue, R.color.note_blue);
        initColorView(viewColorPurple, R.color.note_purple);

        View.OnClickListener listener = v -> {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                selectedColor = (int) tag;
                highlightColorSelection(v);
            }
        };

        for (View colorView : colorViews) {
            colorView.setOnClickListener(listener);
        }

        View defaultView = getViewForColor(selectedColor);
        if (defaultView != null) {
            highlightColorSelection(defaultView);
        }
    }

    private void showEditingNote() {
        if (editingNote == null) return;

        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(editingNote.getTitle())) {
            builder.append(editingNote.getTitle());
        }
        if (!TextUtils.isEmpty(editingNote.getContent())) {
            if (builder.length() > 0) builder.append("\n");
            builder.append(editingNote.getContent());
        }
        edtContent.setText(builder.toString());
        edtContent.setSelection(edtContent.getText().length());
        selectedColor = editingNote.getColor();
        reminderTime = editingNote.getReminderTime();
        applyTitleStyling(edtContent.getText());

        txtReminderInfo.setText(reminderTime == 0
                ? "Không nhắc"
                : "Nhắc lúc: " + android.text.format.DateFormat.format(
                "HH:mm dd/MM/yyyy", reminderTime));

        String cat = editingNote.getCategory();
        int idToCheck = R.id.rbOther;
        if ("Work".equals(cat)) idToCheck = R.id.rbWork;
        else if ("Study".equals(cat)) idToCheck = R.id.rbStudy;
        else if ("Personal".equals(cat)) idToCheck = R.id.rbPersonal;

        radioCategory.check(idToCheck);

        View storedColorView = getViewForColor(selectedColor);
        if (storedColorView != null) {
            highlightColorSelection(storedColorView);
        }
    }

    private void pickReminder() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                reminderTime = calendar.getTimeInMillis();
                                txtReminderInfo.setText("Nhắc lúc: "
                                        + android.text.format.DateFormat.format(
                                        "HH:mm dd/MM/yyyy", reminderTime));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private String getSelectedCategory() {
        int id = radioCategory.getCheckedRadioButtonId();
        if (id == R.id.rbWork) return "Work";
        if (id == R.id.rbStudy) return "Study";
        if (id == R.id.rbPersonal) return "Personal";
        return "Other";
    }

    private void saveNote() {
        String rawContent = edtContent.getText().toString();
        if (rawContent.trim().isEmpty()) {
            edtContent.setError("Không được để trống");
            return;
        }

        String[] parts = extractTitleAndBody(rawContent);
        String title = parts[0];
        String content = parts[1];

        String category = getSelectedCategory();
        long now = System.currentTimeMillis();

        NoteDatabase.databaseWriteExecutor.execute(() -> {
            if (noteId == -1) {    // thêm mới
                Note note = new Note(title, content, category, now,
                        false, selectedColor, reminderTime);
                noteDao.insert(note);
            } else {               // sửa
                editingNote.setTitle(title);
                editingNote.setContent(content);
                editingNote.setCategory(category);
                editingNote.setColor(selectedColor);
                editingNote.setReminderTime(reminderTime);
                noteDao.update(editingNote);
            }

            if (reminderTime > 0) {
                scheduleReminder(title, reminderTime);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private String[] extractTitleAndBody(String rawText) {
        if (rawText == null) return new String[]{"", ""};

        String normalized = rawText.replace("\r\n", "\n");
        while (normalized.startsWith("\n")) {
            normalized = normalized.substring(1);
        }

        int breakIndex = normalized.indexOf('\n');
        String title;
        String body;

        if (breakIndex == -1) {
            title = normalized.trim();
            body = "";
        } else {
            title = normalized.substring(0, breakIndex).trim();
            body = normalized.substring(breakIndex + 1).trim();
        }

        if (title.isEmpty() && !TextUtils.isEmpty(body)) {
            String temp = body;
            int nextBreak = temp.indexOf('\n');
            if (nextBreak == -1) {
                title = temp.trim();
                body = "";
            } else {
                title = temp.substring(0, nextBreak).trim();
                body = temp.substring(nextBreak + 1).trim();
            }
        }

        return new String[]{title, body};
    }

    private void setupNoteEditor() {
        contentWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                applyTitleStyling(s);
            }
        };
        edtContent.addTextChangedListener(contentWatcher);
    }

    private void applyTitleStyling(Editable editable) {
        if (editable == null) return;

        boolean watcherDetached = false;
        if (contentWatcher != null) {
            edtContent.removeTextChangedListener(contentWatcher);
            watcherDetached = true;
        }

        boolean isComposing = isImeComposing(editable);
        if (isComposing) {
            if (watcherDetached) {
                edtContent.addTextChangedListener(contentWatcher);
            }
            return;
        }

        clearTitleSpans(editable);

        int titleEnd = findTitleEnd(editable);
        if (titleEnd > 0) {
            titleBoldSpan = new StyleSpan(Typeface.BOLD);
            titleSizeSpan = new AbsoluteSizeSpan(spToPx(20));
            editable.setSpan(titleBoldSpan,
                    0, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.setSpan(titleSizeSpan,
                    0, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (watcherDetached) {
            edtContent.addTextChangedListener(contentWatcher);
        }
    }

    private boolean isImeComposing(Editable editable) {
        int start = BaseInputConnection.getComposingSpanStart(editable);
        int end = BaseInputConnection.getComposingSpanEnd(editable);
        return start != -1 && end != -1 && start != end;
    }

    private void clearTitleSpans(Editable editable) {
        if (titleBoldSpan != null) {
            editable.removeSpan(titleBoldSpan);
            titleBoldSpan = null;
        }
        if (titleSizeSpan != null) {
            editable.removeSpan(titleSizeSpan);
            titleSizeSpan = null;
        }
    }

    private int findTitleEnd(CharSequence text) {
        if (TextUtils.isEmpty(text)) return 0;
        int breakIndex = TextUtils.indexOf(text, '\n');
        return breakIndex == -1 ? text.length() : breakIndex;
    }

    private void scheduleReminder(String title, long timeMillis) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("note_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
        }
    }

    private void initColorView(View view, int colorResId) {
        if (view == null) return;
        int color = ContextCompat.getColor(this, colorResId);
        view.setTag(color);
    }

    private void highlightColorSelection(View selectedView) {
        if (colorViews == null) return;
        for (View colorView : colorViews) {
            if (colorView == null) continue;
            boolean isSelected = colorView == selectedView;
            Object tag = colorView.getTag();
            if (!(tag instanceof Integer)) continue;

            int color = (int) tag;
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(dpToPx(8));
            drawable.setColor(color);
            int strokeWidth = dpToPx(isSelected ? 2 : 1);
            int strokeColor = isSelected
                    ? ContextCompat.getColor(this, R.color.text_primary)
                    : ContextCompat.getColor(this, android.R.color.transparent);
            drawable.setStroke(strokeWidth, strokeColor);
            colorView.setBackground(drawable);

            float scale = isSelected ? 1.1f : 1f;
            colorView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(120)
                    .start();
        }
    }

    private View getViewForColor(int colorValue) {
        if (colorViews == null) return null;
        for (View colorView : colorViews) {
            Object tag = colorView.getTag();
            if (tag instanceof Integer && (int) tag == colorValue) {
                return colorView;
            }
        }
        return null;
    }

    private int dpToPx(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private int spToPx(float sp) {
        return Math.round(sp * getResources().getDisplayMetrics().scaledDensity);
    }
}
