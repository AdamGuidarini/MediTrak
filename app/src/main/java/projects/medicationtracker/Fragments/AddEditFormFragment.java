package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

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
    private MaterialAutoCompleteTextView patientNameInput;

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
        ArrayAdapter<String> patientNamesAdapter;
        ArrayList<String> patientNames;
        patientGroup = rootView.findViewById(R.id.patientRadioGroup);
        meButton = rootView.findViewById(R.id.patientIsMe);
        otherButton = rootView.findViewById(R.id.patientIsNotMe);
        patientNameInput = rootView.findViewById(R.id.patientNameInput);
        patientNameInputLayout = rootView.findViewById(R.id.patientNameInputLayout);

        patientNames = db.getPatients();
        patientNames.removeIf(n -> n.equals("ME!"));

         patientNamesAdapter = new ArrayAdapter<>(
                 rootView.getContext(),
                 android.R.layout.simple_dropdown_item_1line,
                 patientNames
         );

         patientNameInput.setAdapter(patientNamesAdapter);

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
            if (meButton.isChecked())
            {
                if (patientNameInputLayout.getVisibility() == View.VISIBLE)
                    patientNameInputLayout.setVisibility(View.GONE);
            }
            else
            {
                patientNameInputLayout.setVisibility(View.VISIBLE);
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

        dosageAmountInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    Integer.parseInt(dosageAmountInput.getText().toString());
                }
                catch (Exception e)
                {
                    if (!dosageAmountInput.getText().toString().isEmpty())
                        dosageAmountInput.setError("Provided value is too big");
                }
            }
        });

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
        setCustomFrequencyViews();
        setSaveButton();
    }

    private void setMultiplePerDayFrequencyViews()
    {
        EditText numberOfTimersPerDay = rootView.findViewById(R.id.numberOfTimersPerDay);
        EditText startDateMultiplePerDay = rootView.findViewById(R.id.startDateMultiplePerDay);
        LinearLayout timesPerDayHolder = rootView.findViewById(R.id.timesPerDayHolder);

        startDateMultiplePerDay.setShowSoftInputOnFocus(false);
        startDateMultiplePerDay.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment datePicker = new SelectDateFragment(startDateMultiplePerDay.getId());
                datePicker.show(getParentFragmentManager(), null);
            }
        });

        numberOfTimersPerDay.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                int days;

                try
                {
                    days = Integer.parseInt(numberOfTimersPerDay.getText().toString());
                }
                catch (Exception e)
                {
                    numberOfTimersPerDay.setError("Provided value is too big");

                    return;
                }

                if (timesPerDayHolder.getChildCount() > days)
                {
                    for (int i = timesPerDayHolder.getChildCount(); i > days; i--)
                    {
                        timesPerDayHolder.removeViewAt(i - 1);
                    }

                    return;
                }
                else
                {
                    days -= timesPerDayHolder.getChildCount();
                }

                for (int ind = 0; ind < days; ind++)
                {
                    TextInputLayout textLayout = new TextInputLayout(new ContextThemeWrapper(rootView.getContext(), R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox_Dense));
                    TextInputEditText timeEntry = new TextInputEditText(textLayout.getContext());

                    textLayout.setHint(getString(R.string.taken_at));
                    textLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    textLayout.setBoxBackgroundColor(ContextCompat.getColor(rootView.getContext(), android.R.color.transparent));
                    textLayout.setBoxCornerRadii(10, 10, 10, 10);
                    textLayout.addView(timeEntry);

                    timeEntry.setId(ind);
                    timeEntry.setShowSoftInputOnFocus(false);
                    timeEntry.setInputType(InputType.TYPE_NULL);

                    timeEntry.setOnFocusChangeListener((view, b) ->
                    {
                        if (b)
                        {
                            DialogFragment dialogFragment = new TimePickerFragment(timeEntry.getId());
                            dialogFragment.show(getParentFragmentManager(), null);
                        }
                    });

                    timesPerDayHolder.addView(textLayout);
                }
            }
        });
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

    private void setCustomFrequencyViews()
    {
        TextInputEditText customFreqStartDate = rootView.findViewById(R.id.CustomFreqMedStart);
        TextInputEditText customFreqMedTime = rootView.findViewById(R.id.CustomFreqMedTime);
        TextInputEditText customFreqMTakenEveryEnter =
                rootView.findViewById(R.id.CustomFreqMTakenEveryEnter);
        MaterialAutoCompleteTextView customFreqTimeUnitEnter =
                rootView.findViewById(R.id.CustomFreqTimeUnitEnter);
        ArrayList<String> timeUnits = new ArrayList<>();
        ArrayAdapter<String> timeUnitsAdapter;

        customFreqMedTime.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment dialogFragment = new TimePickerFragment(customFreqMedTime.getId());
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });

        customFreqStartDate.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment df = new SelectDateFragment(customFreqStartDate.getId());
                df.show(getParentFragmentManager(), null);
            }
        });

        customFreqMTakenEveryEnter.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    Integer.parseInt(customFreqMTakenEveryEnter.getText().toString());
                }
                catch (Exception e)
                {
                    if (!customFreqMTakenEveryEnter.getText().toString().isEmpty())
                        customFreqMTakenEveryEnter.setError("Provided value is too big");
                }
            }
        });

        timeUnits.add("Minutes");
        timeUnits.add("Hours");
        timeUnits.add("Days");
        timeUnits.add("Weeks");

        timeUnitsAdapter = new ArrayAdapter<>(
                rootView.getContext(), android.R.layout.simple_dropdown_item_1line, timeUnits
        );

        customFreqTimeUnitEnter.setAdapter(timeUnitsAdapter);
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