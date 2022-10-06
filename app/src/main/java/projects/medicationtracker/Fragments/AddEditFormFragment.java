package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
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

    private MaterialButton saveButton;

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

        if (medId == -1 || (medication != null && medication.getPatientName().equals("ME!")))
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

        if (medId != -1)
        {
            medNameInput.setText(medication.getMedName());
            if (!medication.getAlias().isEmpty())
            {
                aliasInput.setText(medication.getAlias());
            }

            dosageAmountInput.setText(medication.getMedDosage());
            dosageUnitsInput.setText(medication.getMedDosageUnits());
        }
    }

    private void setFrequencyCard()
    {
        RelativeLayout dailyLayout = rootView.findViewById(R.id.dailyMedFrequency);
        LinearLayout multiplePerDay = rootView.findViewById(R.id.multiplePerDayFrequency);
        LinearLayout custom = rootView.findViewById(R.id.customFrequencyLayout);
        ArrayAdapter<String> frequencyOptions;
        ArrayList<String> options = new ArrayList<>();

        frequencyDropDown = rootView.findViewById(R.id.frequencyOptionsDropdown);

        options.add(getString(R.string.multiple_times_per_day));
        options.add(getString(R.string.daily));
        options.add(getString(R.string.custom_frequency));

         frequencyOptions = new ArrayAdapter<>(
                 rootView.getContext(), android.R.layout.simple_dropdown_item_1line, options
         );

        frequencyDropDown.setAdapter(frequencyOptions);

        frequencyDropDown.setOnItemClickListener((adapterView, view, i, l) ->
        {
            switch (i)
            {
                case 0:
                    dailyLayout.setVisibility(View.GONE);
                    custom.setVisibility(View.GONE);

                    multiplePerDay.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    custom.setVisibility(View.GONE);
                    multiplePerDay.setVisibility(View.GONE);

                    dailyLayout.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    dailyLayout.setVisibility(View.GONE);
                    multiplePerDay.setVisibility(View.GONE);

                    custom.setVisibility(View.VISIBLE);
                    break;
            }
        });

        setMultiplePerDayFrequencyViews();
        setDailyFrequencyViews();
        setSaveButton();
    }

    private void setMultiplePerDayFrequencyViews()
    {

    }

    private void setDailyFrequencyViews()
    {
        EditText dailyMedTime = rootView.findViewById(R.id.dailyMedTime);
        EditText dailyMedStartDate = rootView.findViewById(R.id.dailyMedStart);

        dailyMedTime.setShowSoftInputOnFocus(false);
        dailyMedStartDate.setShowSoftInputOnFocus(false);


        dailyMedTime.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment dialogFragment = new TimePickerFragment(dailyMedTime.getId());
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });

        dailyMedStartDate.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment df = new SelectDateFragment(dailyMedStartDate.getId());
                df.show(getParentFragmentManager(), null);
            }
        });

        if (medId != -1)
        {
            dailyMedStartDate.setTag(medication.getStartDate().toLocalDate());
            dailyMedStartDate.setText(
                    TimeFormatting.localDateToString(medication.getStartDate().toLocalDate())
            );

            dailyMedTime.setTag(medication.getStartDate().toLocalTime());
            dailyMedTime.setText(
                    TimeFormatting.localTimeToString(medication.getStartDate().toLocalTime())
            );
        }
    }

    public void setSaveButton()
    {
        saveButton = rootView.findViewById(R.id.saveButton);

        saveButton.setOnClickListener((view ->
        {
            Toast.makeText(rootView.getContext(), "This will save stuff", Toast.LENGTH_LONG).show();
        }));
    }
}