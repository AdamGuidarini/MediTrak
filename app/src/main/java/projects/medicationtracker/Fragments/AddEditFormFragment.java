package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEditFormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddEditFormFragment extends Fragment
{
    final public static String MED_ID = "medId";
    private DBHelper db;

    private long medId = -1;
    private Medication medication;
    private View rootView;

    private RadioGroup patientGroup;
    private RadioButton meButton;
    private RadioButton otherButton;
    private TextInputLayout patientNameInputLayout;
    private EditText patientNameInput;

    private EditText medNameInput;
    private SwitchMaterial aliasSwitch;
    private TextInputLayout aliasInputLayout;
    private EditText aliasInput;
    private EditText dosageAmountInput;
    private EditText dosageUnitsInput;

    //TODO add all frequency inputs
    private MaterialAutoCompleteTextView frequencyDropDown;

    public AddEditFormFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param medId ID if medication if editing, -1 if adding
     * @return A new instance of fragment AddEditFormFragment.
     */
    public static AddEditFormFragment newInstance(long medId)
    {
        AddEditFormFragment fragment = new AddEditFormFragment();
        Bundle args = new Bundle();
        args.putLong(MED_ID, medId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            medId = getArguments().getLong(MED_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_add_edit_form, container, false);
        db = new DBHelper(rootView.getContext());

        medication = medId != -1 ? db.getMedication(medId) : null;

        buildViews();

        return rootView;
    }

    private void buildViews()
    {
        setPatientCard();
        setMedNameAndDosageCard();
        setFrequencyCard();
    }

    private void setPatientCard()
    {
        patientGroup = rootView.findViewById(R.id.patientRadioGroup);
        meButton = rootView.findViewById(R.id.patientIsMe);
        otherButton = rootView.findViewById(R.id.patientIsNotMe);
        patientNameInput = rootView.findViewById(R.id.patientNameInput);
        patientNameInputLayout = rootView.findViewById(R.id.patientNameInputLayout);

        if (medId == -1)
        {
            meButton.setChecked(true);
        }
        else if (medication.getPatientName().equals("ME!"))
        {
            meButton.setChecked(true);
        }
        else
        {
            otherButton.setChecked(true);
            patientNameInputLayout.setVisibility(View.VISIBLE);
            patientNameInput.setText(medication.getPatientName());
        }

        patientGroup.setOnCheckedChangeListener((radioGroup, i) ->
        {
            switch (radioGroup.findViewById(i).getId())
            {
                case R.id.patientIsMe:
                    if (patientNameInputLayout.getVisibility() == View.VISIBLE)
                        patientNameInputLayout.setVisibility(View.GONE);
                    break;
                case R.id.patientIsNotMe:
                    patientNameInputLayout.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setMedNameAndDosageCard()
    {
        medNameInput = rootView.findViewById(R.id.medicationName);
        aliasSwitch = rootView.findViewById(R.id.aliasSwitch);
        aliasInput = rootView.findViewById(R.id.enterAlias);
        aliasInputLayout = rootView.findViewById(R.id.aliasInputLayout);
        dosageAmountInput = rootView.findViewById(R.id.dosageAmount);
        dosageUnitsInput = rootView.findViewById(R.id.dosageUnits);

        aliasSwitch.setChecked(medId != -1 && !medication.getAlias().isEmpty());

        aliasSwitch.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (aliasInputLayout.getVisibility() == View.GONE)
                aliasInputLayout.setVisibility(View.VISIBLE);
            else
            {
                aliasInputLayout.setVisibility(View.GONE);
                aliasInput.setText("");
            }
        });
    }

    private void setFrequencyCard()
    {

    }
}