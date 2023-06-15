package projects.medicationtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import kotlin.Pair;
import projects.medicationtracker.Fragments.MyMedicationsFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.SimpleClasses.Medication;
import projects.medicationtracker.Views.StandardCardView;

public class MyMedications extends AppCompatActivity {
    DBHelper db = new DBHelper(this);

    /**
     * Creates MyMedications
     *
     * @param savedInstanceState Saved instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_medications);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.my_medications));

        String you = getString(R.string.you);

        if (db.numberOfRows() == 0)
            return;
        else {
            TextView noMeds = findViewById(R.id.noMyMeds);
            noMeds.setVisibility(View.GONE);
            ScrollView scrollMyMeds = findViewById(R.id.scrollMyMeds);
            scrollMyMeds.setVisibility(View.VISIBLE);
        }

        final Spinner nameSpinner = findViewById(R.id.nameSpinner);
        final LinearLayout myMedsLayout = findViewById(R.id.medLayout);

        ArrayList<String> patientNames = db.getPatients();
        ArrayList<Pair<String, ArrayList<Medication>>> allMeds = new ArrayList<>();

        for (String patient : patientNames) {
            ArrayList<Medication> meds = db.getMedicationsForPatient(patient).stream().filter(
                    m -> m.getChild() == null
            ).collect(Collectors.toCollection(ArrayList::new));

            if (meds.size() > 0) {
                allMeds.add(new Pair<>(patient, meds));
            }
        }

        if (allMeds.size() == 1) {
            ArrayList<Medication> patientMeds = db.getMedicationsForPatient(allMeds.get(0).getFirst());

            for (Medication medication : patientMeds) {
                if (medication.getChild() != null) continue;

                createMyMedCards(medication, myMedsLayout);
            }
        } else if (allMeds.size() > 1) {
            String[] patients = (String[]) allMeds.stream().map(Pair::getFirst).toArray();

            if (allMeds.stream().allMatch(m -> m.getFirst().equals("ME!"))) {
                allMeds = allMeds.stream().map(m -> {
                    if (m.getFirst().equals("ME!")) {
                        m = new Pair<>(you, m.getSecond());
                    }

                    return m;
                }).collect(Collectors.toCollection(ArrayList::new));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, patients);
            nameSpinner.setAdapter(adapter);

            nameSpinner.setVisibility(View.VISIBLE);

            if (Arrays.asList(patients).contains(you))
                nameSpinner.setSelection(adapter.getPosition(you));

            final ArrayList<Pair<String, ArrayList<Medication>>> allMedsClone = (ArrayList<Pair<String, ArrayList<Medication>>>) allMeds.clone();
            nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    myMedsLayout.removeAllViews();

                    String selected = adapterView.getSelectedItem().toString();
                    final String patient = selected.equals(you) ? "ME!" : selected;

                    Medication[] patientMeds = (Medication[]) allMedsClone.stream().filter(
                            m -> m.getFirst().equals(patient)
                    ).collect(Collectors.toCollection(ArrayList::new)).stream().map(
                            Pair::getSecond
                    ).toArray();

                    for (Medication medication : patientMeds) {
                        if (medication.getChild() != null) continue;

                        createMyMedCards(medication, myMedsLayout);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
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
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    /**
     * Creates a CardView containing all information on a Medication
     *
     * @param medication The Medication whose details will be displayed.
     * @param baseLayout The LinearLayout in which to place the card
     */
    private void createMyMedCards(Medication medication, LinearLayout baseLayout) {
        StandardCardView thisMedCard = new StandardCardView(this);
        FragmentContainerView thisMedLayout = new FragmentContainerView(this);
        Bundle bundle = new Bundle();

        baseLayout.addView(thisMedCard);
        thisMedCard.addView(thisMedLayout);

        thisMedLayout.setId((int) medication.getId());

        bundle.putParcelable("Medication", medication);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add((int) medication.getId(), MyMedicationsFragment.class, bundle)
                .commit();
    }
}