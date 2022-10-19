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
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;

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

    private TextInputLayout medicationNameInputLayout;
    private EditText medNameInput;
    private SwitchMaterial aliasSwitch;
    private TextInputLayout aliasInputLayout;
    private EditText aliasInput;
    private TextInputLayout dosageAmountInputLayout;
    private EditText dosageAmountInput;
    private TextInputLayout dosageUnitsInputLayout;
    private EditText dosageUnitsInput;

    private MaterialAutoCompleteTextView frequencyDropDown;
    private TextInputLayout numberOfTimersPerDayLayout;
    private TextInputEditText customFreqStartDate;
    private TextInputEditText customFreqMedTime;
    private TextInputEditText customFreqMTakenEveryEnter;
    private MaterialAutoCompleteTextView customFreqTimeUnitEnter;

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

        medication = medId != -1 ? db.getMedication(medId) : new Medication();

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
                {
                    patientNameInputLayout.setVisibility(View.GONE);
                }
            }
            else
            {
                patientNameInputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setMedNameAndDosageCard()
    {
        medicationNameInputLayout = rootView.findViewById(R.id.medicationNameInputLayout);
        medNameInput = rootView.findViewById(R.id.medicationName);
        aliasSwitch = rootView.findViewById(R.id.aliasSwitch);
        aliasInput = rootView.findViewById(R.id.enterAlias);
        aliasInputLayout = rootView.findViewById(R.id.aliasInputLayout);
        dosageAmountInputLayout = rootView.findViewById(R.id.dosageAmountInputLayout);
        dosageAmountInput = rootView.findViewById(R.id.dosageAmount);
        dosageUnitsInputLayout = rootView.findViewById(R.id.dosageUnitsInputLayout);
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

                    dosageAmountInputLayout.setErrorEnabled(false);
                }
                catch (Exception e)
                {
                    if (!dosageAmountInput.getText().toString().isEmpty())
                    {
                        dosageAmountInputLayout.setError("Provided value is too big");
                    }
                }
            }
        });

        aliasSwitch.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (aliasInputLayout.getVisibility() == View.GONE)
            {
                aliasInputLayout.setVisibility(View.VISIBLE);
            }
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
        numberOfTimersPerDayLayout = rootView.findViewById(R.id.numberOfTimersPerDayLayout);


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
                    numberOfTimersPerDayLayout.setError("Provided value is too big");

                    return;
                }

                if (days > 50)
                {
                    numberOfTimersPerDayLayout.setError("Provided value is too big");

                    return;
                }
                else
                {
                    numberOfTimersPerDayLayout.setErrorEnabled(false);
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
        customFreqStartDate = rootView.findViewById(R.id.CustomFreqMedStart);
        customFreqMedTime = rootView.findViewById(R.id.CustomFreqMedTime);
        customFreqMTakenEveryEnter = rootView.findViewById(R.id.CustomFreqMTakenEveryEnter);
        customFreqTimeUnitEnter = rootView.findViewById(R.id.CustomFreqTimeUnitEnter);
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
                    {
                        customFreqMTakenEveryEnter.setError("Provided value is too big");
                    }
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

    private void setSaveButton()
    {
        saveButton = rootView.findViewById(R.id.saveButton);

        saveButton.setOnClickListener((view ->
        {
            saveMedication();
        }));
    }

    private void saveMedication()
    {
        if (isNameCardValid() && isMedNameAndDosageCardValid() && isFrequencyCardValid())
        {
            Toast.makeText(rootView.getContext(), "This med can be saved", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(rootView.getContext(), "This med cannot be saved", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNameCardValid()
    {
        String patientName;

        patientNameInputLayout.setErrorEnabled(false);

        if (meButton.isChecked())
        {
            medication.setPatientName("ME!");

            return true;
        }

        patientName = patientNameInput.getText().toString();

        if (!patientName.isEmpty() && !patientName.equals("ME!"))
        {
            medication.setPatientName(patientName);

            return true;
        }
        else if (patientName.equals("ME!"))
        {
            patientNameInputLayout.setError("Provided name is invalid");
        }
        else
        {
            patientNameInputLayout.setError("Please provide a name");
        }

        return false;
    }

    private boolean isMedNameAndDosageCardValid()
    {
        boolean isValid = true;

        medicationNameInputLayout.setErrorEnabled(false);
        dosageUnitsInputLayout.setErrorEnabled(false);

        if (medNameInput.getText().toString().isEmpty())
        {
            medicationNameInputLayout.setError("Please enter a name for this medication");
            isValid = false;
        }
        else
        {
            medication.setMedName(medNameInput.getText().toString());
        }

        if (aliasSwitch.isChecked() && !aliasInput.getText().toString().isEmpty())
        {
            medication.setAlias(aliasInput.getText().toString());
        }

        if (
                (dosageAmountInputLayout.getError() == null ||
                intIsParsable(dosageAmountInput.getText().toString())) &&
                !dosageAmountInput.getText().toString().isEmpty()
        )
        {
            medication.setMedDosage(Integer.parseInt(dosageAmountInput.getText().toString()));

            dosageAmountInputLayout.setErrorEnabled(false);
        }
        else
        {
            isValid = false;

            if (dosageAmountInput.getText().toString().isEmpty())
            {
                dosageAmountInputLayout.setError("Please enter a dosage");
            }
        }

        if (dosageUnitsInput.getText().toString().isEmpty())
        {
            dosageUnitsInputLayout.setError("Please enter the units for this medication");
        }
        else
        {
            isValid = false;

            medication.setMedDosageUnits(dosageUnitsInput.getText().toString());
        }

        return isValid;
    }

    private boolean intIsParsable(String intToParse)
    {
        try
        {
            Integer.parseInt(intToParse);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean isFrequencyCardValid()
    {
        return false;
    }
}