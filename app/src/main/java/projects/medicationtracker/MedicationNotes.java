package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
            finish();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     **************************************************************************/
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    /**
     * Sets CardViews for activity
     */
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
}