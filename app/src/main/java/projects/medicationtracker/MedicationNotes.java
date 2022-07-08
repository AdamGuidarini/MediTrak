package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MedicationNotes extends AppCompatActivity
{
    final DBHelper db = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_notes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notes");

        setCards();
        setCardListeners();
    }

    /**
     * Creates options menu
     * @param menu The menu bar
     * @return Options menu
     **************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Determines which button was selected
     * @param item Selected menu option
     * @return Selected option
     **************************************************************************/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            Intent intent = new Intent(this, MyMedications.class);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MyMedications if back arrow is pressed
     **************************************************************************/
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MyMedications.class);
        finish();
        startActivity(intent);
    }

    /**
     * Sets CardViews for activity
     **************************************************************************/
    public void setCards()
    {
        long medId = getIntent().getLongExtra("medId", 0);

        ArrayList<Note> notes = db.getNotes(medId);

        if (notes == null)
            return;
        else
        {
            TextView tv = findViewById(R.id.noNotes);
            tv.setVisibility(View.GONE);
            ScrollView scrollNotes = findViewById(R.id.scrollNotes);
            scrollNotes.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < notes.size(); i++)
            CardCreator.createNoteCard(notes.get(i), findViewById(R.id.notesLayout));
    }

    /**
     * Allows the user to create a note
     * @param item The menu button
     **************************************************************************/
    public void onAddNoteClick(MenuItem item)
    {
        long medId = getIntent().getLongExtra("medId", 0);

        AddNoteFragment noteFragment = new AddNoteFragment(this, medId);
        noteFragment.show(getSupportFragmentManager(), "Add Note");
    }

    /**
     * Sets listeners for the cards
     **************************************************************************/
    public void setCardListeners()
    {
        ArrayList<CardView> cardViews = new ArrayList<>();
        LinearLayout noteLayout = findViewById(R.id.notesLayout);

        for (int i = 0; i < noteLayout.getChildCount(); i++)
        {
            View child = noteLayout.getChildAt(i);

            child.getClass().getName();
            CardView.class.getName();
            {
                cardViews.add((CardView)child);
            }
        }

        for (CardView card : cardViews)
        {
            LinearLayout layout = (LinearLayout) card.getChildAt(0);
            TextView noteText = (TextView) layout.getChildAt(0);
            Note note = (Note) noteText.getTag();

            card.setOnClickListener(view ->
            {
                DialogFragment editNote = new EditNoteFragment(note, db);
                editNote.show(getSupportFragmentManager(), null);
            });
        }
    }
}