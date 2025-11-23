package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import projects.medicationtracker.Dialogs.AddNoteDialog;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Utils.TextViewUtils;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.Models.Note;
import projects.medicationtracker.Views.StandardCardView;

public class MedicationNotes extends BaseActivity implements IDialogCloseListener {
    private final DBHelper db = new DBHelper(this);
    private LinearLayout notesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_notes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.notes));

        notesLayout = findViewById(R.id.notesLayout);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        setCards();
    }

    /**
     * Creates options menu
     *
     * @param menu The menu bar
     * @return Options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Determines which button was selected
     *
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MyMedications.class);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MyMedications if back arrow is pressed
     */
    public void handleBackPressed() {
        Intent intent = new Intent(this, MyMedications.class);
        finish();
        startActivity(intent);
    }

    /**
     * Sets CardViews for activity
     */
    public void setCards() {
        long medId = getIntent().getLongExtra("medId", 0);

        ArrayList<Note> notes = db.getNotes(medId);

        if (notes == null)
            return;
        else {
            TextView tv = findViewById(R.id.noNotes);
            tv.setVisibility(View.GONE);
            ScrollView scrollNotes = findViewById(R.id.scrollNotes);
            scrollNotes.setVisibility(View.VISIBLE);
        }

        for (final Note note : notes) {
            createNoteCard(note, notesLayout);
        }
    }

    /**
     * Allows the user to create a note
     *
     * @param item The menu button
     */
    public void onAddNoteClick(MenuItem item) {
        long medId = getIntent().getLongExtra("medId", 0);
        Note note = new Note(-1, medId, null, null);

        AddNoteDialog noteFragment = new AddNoteDialog(note);
        noteFragment.show(getSupportFragmentManager(), getString(R.string.add_note));
    }

    /**
     * Creates a CardView with a note in it
     *
     * @param note       The Note in the CardView
     * @param baseLayout The LinearLayout the holds the CardView
     */
    private void createNoteCard(Note note, LinearLayout baseLayout) {
        Context context = baseLayout.getContext();
        StandardCardView noteCard = new StandardCardView(context);
        LinearLayout cardLayout = new LinearLayout(context);

        cardLayout.setOrientation(LinearLayout.VERTICAL);

        baseLayout.addView(noteCard);
        noteCard.addView(cardLayout);
        noteCard.setOnClickListener(
                view -> {
                    DialogFragment editNote = new AddNoteDialog(note);
                    editNote.show(getSupportFragmentManager(), null);
                }
        );

        TextView noteText = new TextView(context);
        TextViewUtils.setTextViewParams(noteText, note.getNote(), cardLayout);

        TextView noteDate = new TextView(context);

        String date = DateTimeFormatter.ofPattern(
                preferences.getString(DATE_FORMAT),
                Locale.getDefault()
        ).format(note.getNoteTime().toLocalDate());
        String time = DateTimeFormatter.ofPattern(
                preferences.getString(TIME_FORMAT),
                Locale.getDefault()
        ).format(note.getNoteTime());

        String noteDateLabel = getString(
                R.string.note_timestamp,
                date,
                time
        );

        if (note.getModifiedTime() != null) {
            String editDate = DateTimeFormatter.ofPattern(
                    preferences.getString(DATE_FORMAT),
                    Locale.getDefault()
            ).format(note.getNoteTime().toLocalDate());
            String editTime = DateTimeFormatter.ofPattern(
                    preferences.getString(TIME_FORMAT),
                    Locale.getDefault()
            ).format(note.getModifiedTime());

            String editedLabel = getString(
                R.string.note_edit_timestamp,
                editDate,
                editTime
            );

            noteDateLabel += "\n" + editedLabel;
        }

        TextViewUtils.setTextViewParams(noteDate, noteDateLabel, cardLayout);

        noteCard.setTag(note);
    }

    @Override
    public void handleDialogClose(Action action, Object data) {
        Note note = (Note) data;

        switch (action) {
            case ADD:
                note.setNoteId(db.addNote(note.getNote(), note.getMedId()));
                note.setNoteTime(LocalDateTime.now());

                if (notesLayout.getChildCount() == 0) {
                    TextView tv = findViewById(R.id.noNotes);
                    tv.setVisibility(View.GONE);

                    ScrollView scrollNotes = findViewById(R.id.scrollNotes);
                    scrollNotes.setVisibility(View.VISIBLE);
                }

                createNoteCard(note, notesLayout);
                break;
            case EDIT:
                db.updateNote(note);

                notesLayout.removeAllViews();
                setCards();

                break;
            case DELETE:
                db.deleteNote(note);

                for (int i = 0; i < notesLayout.getChildCount(); i++) {
                    Note thisNote = (Note) notesLayout.getChildAt(i).getTag();

                    if (thisNote.getNoteId() == note.getNoteId()) {
                        notesLayout.removeViewAt(i);
                        break;
                    }
                }

                if (notesLayout.getChildCount() == 0) {
                    TextView tv = findViewById(R.id.noNotes);
                    tv.setVisibility(View.VISIBLE);

                    ScrollView scrollNotes = findViewById(R.id.scrollNotes);
                    scrollNotes.setVisibility(View.GONE);
                }
        }
    }
}
