package projects.medicationtracker.Fragments;

import android.content.Intent;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.MainActivity;
import projects.medicationtracker.MyMedications;
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
    final public static int MINUTES_IN_DAY = 1440;

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

    private TextInputLayout frequencyDropdownLayout;
    private MaterialAutoCompleteTextView frequencyDropDown;
    private TextInputLayout numberOfTimersPerDayLayout;
    private EditText dailyMedTime;
    private EditText dailyMedStartDate;
    private TextInputLayout customFreqStartDateLayout;
    private TextInputEditText customFreqStartDate;
    private TextInputLayout customFreqTakenEveryLayout;
    private TextInputLayout customFreqTimeTakenLayout;
    private TextInputEditText customFreqMedTime;
    private TextInputLayout customFreqTimeUnitLayout;
    private TextInputEditText customFreqMTakenEveryEnter;
    private MaterialAutoCompleteTextView customFreqTimeUnitEnter;
    private EditText startDateMultiplePerDay;
    private EditText numberOfTimersPerDay;
    private int selectedFrequencyTypeIndex = -1;
    private ArrayList<String> timeUnits;

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

        if (medId != -1)
        {
            LocalTime[] times = db.getMedicationTimes(medId);
            LocalDateTime[] dateTimes = new LocalDateTime[times.length];

            medication = db.getMedication(medId);

            for (int i = 0; i < times.length; i++)
            {
                dateTimes[i] = LocalDateTime.of(medication.getStartDate().toLocalDate(), times[i]);
            }

            medication.setTimes(dateTimes);
        }
        else
        {
            medication = new Medication();
        }

        buildViews();

        return rootView;
    }

    /**
     * Builds all views in activity
     */
    private void buildViews()
    {
        setPatientCard();
        setMedNameAndDosageCard();
        setFrequencyCard();
    }

    /**
     * Prepares medication card
     */
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

    /**
     * Prepares input for medication dosage & name card
     */
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
                    Float.parseFloat(dosageAmountInput.getText().toString());

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

            dosageAmountInput.setText(String.valueOf(medication.getMedDosage()));
            dosageUnitsInput.setText(medication.getMedDosageUnits());
        }
    }

    /**
     * Prepares frequency card
     */
    private void setFrequencyCard()
    {
        RelativeLayout dailyLayout = rootView.findViewById(R.id.dailyMedFrequency);
        LinearLayout multiplePerDay = rootView.findViewById(R.id.multiplePerDayFrequency);
        LinearLayout custom = rootView.findViewById(R.id.customFrequencyLayout);
        ArrayAdapter<String> frequencyOptions;
        ArrayList<String> options = new ArrayList<>();

        frequencyDropdownLayout = rootView.findViewById(R.id.frequencyDropdownLayout);
        frequencyDropDown = rootView.findViewById(R.id.frequencyOptionsDropdown);

        frequencyDropDown.setShowSoftInputOnFocus(false);

        options.add(getString(R.string.multiple_times_per_day));
        options.add(getString(R.string.daily));
        options.add(getString(R.string.custom_frequency));

         frequencyOptions = new ArrayAdapter<>(
                 rootView.getContext(), android.R.layout.simple_dropdown_item_1line, options
         );

        frequencyDropDown.setAdapter(frequencyOptions);

        if (medId != -1)
        {
            if (medication.getMedFrequency() == MINUTES_IN_DAY && medication.getTimes().length == 1)
            {
                frequencyDropDown.setText(
                        frequencyDropDown.getAdapter().getItem(1).toString(), false
                );

                selectedFrequencyTypeIndex = 1;

                dailyLayout.setVisibility(View.VISIBLE);
            }
            else if (medication.getTimes().length > 1)
            {
                frequencyDropDown.setText(
                        frequencyDropDown.getAdapter().getItem(0).toString(), false
                );

                selectedFrequencyTypeIndex = 0;

                multiplePerDay.setVisibility(View.VISIBLE);
            }
            else
            {
                frequencyDropDown.setText(
                        frequencyDropDown.getAdapter().getItem(2).toString(), false
                );

                selectedFrequencyTypeIndex = 2;

                custom.setVisibility(View.VISIBLE);
            }
        }

        frequencyDropDown.setOnItemClickListener((adapterView, view, i, l) ->
        {
            frequencyDropdownLayout.setErrorEnabled(false);

            switch (i)
            {
                case 0:
                    dailyLayout.setVisibility(View.GONE);
                    custom.setVisibility(View.GONE);

                    multiplePerDay.setVisibility(View.VISIBLE);

                    selectedFrequencyTypeIndex = 0;
                    break;
                case 1:
                    custom.setVisibility(View.GONE);
                    multiplePerDay.setVisibility(View.GONE);

                    dailyLayout.setVisibility(View.VISIBLE);

                    selectedFrequencyTypeIndex = 1;
                    break;
                case 2:
                    dailyLayout.setVisibility(View.GONE);
                    multiplePerDay.setVisibility(View.GONE);

                    custom.setVisibility(View.VISIBLE);

                    selectedFrequencyTypeIndex = 2;
                    break;
            }
        });

        setMultiplePerDayFrequencyViews();
        setDailyFrequencyViews();
        setCustomFrequencyViews();
        setSaveButton();
    }

    /**
     * Sets UI for multiple per day input
     */
    private void setMultiplePerDayFrequencyViews()
    {
        numberOfTimersPerDay = rootView.findViewById(R.id.numberOfTimersPerDay);
        startDateMultiplePerDay = rootView.findViewById(R.id.startDateMultiplePerDay);
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
                    numberOfTimersPerDayLayout.setError("Provided value cannot exceed 50");

                    return;
                }

                if (days > 50)
                {
                    numberOfTimersPerDayLayout.setError("Provided value cannot exceed 50");

                    return;
                }
                else if (days == 0)
                {
                    numberOfTimersPerDayLayout.setError("Provided value must be greater than 0");

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
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    );

                    params.setMargins(0, 20, 0, 0);
                    textLayout.setLayoutParams(params);

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

        if (medId != -1 && selectedFrequencyTypeIndex == 0)
        {
            LocalDateTime[] medTimes = medication.getTimes();

            numberOfTimersPerDay.setText(String.valueOf(medTimes.length));
            startDateMultiplePerDay.setText(
                    TimeFormatting.localDateToString(medication.getStartDate().toLocalDate())
            );
            startDateMultiplePerDay.setTag(medication.getStartDate().toLocalDate());

            for (int i = 0; i < medTimes.length; i++)
            {
                LocalTime time = medTimes[i].toLocalTime();
                TextInputLayout childLayout = (TextInputLayout) timesPerDayHolder.getChildAt(i);
                EditText timeInput = childLayout.getEditText();

                timeInput.setText(TimeFormatting.localTimeToString(time));
                timeInput.setTag(time);
            }
        }
    }

    /**
     * Sets UI for daily medication input
     */
    private void setDailyFrequencyViews()
    {
        dailyMedTime = rootView.findViewById(R.id.dailyMedTime);
        dailyMedStartDate = rootView.findViewById(R.id.dailyMedStart);

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

        if (medId != -1 && selectedFrequencyTypeIndex == 1)
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

    /**
     * Builds UI for custom frequency
     */
    private void setCustomFrequencyViews()
    {
        ArrayAdapter<String> timeUnitsAdapter;

        customFreqStartDateLayout = rootView.findViewById(R.id.customFreqStartDate);
        customFreqStartDate = rootView.findViewById(R.id.CustomFreqMedStart);
        customFreqMedTime = rootView.findViewById(R.id.CustomFreqMedTime);
        customFreqTimeUnitLayout = rootView.findViewById(R.id.CustomFreqTimeUnitLayout);
        customFreqTakenEveryLayout = rootView.findViewById(R.id.customFreqTakenEveryLayout);
        customFreqTimeTakenLayout = rootView.findViewById(R.id.CustomFreqTimeTakenLayout);
        customFreqMTakenEveryEnter = rootView.findViewById(R.id.CustomFreqMTakenEveryEnter);
        customFreqTimeUnitEnter = rootView.findViewById(R.id.CustomFreqTimeUnitEnter);
        timeUnits = new ArrayList<>();

        customFreqStartDate.setShowSoftInputOnFocus(false);
        customFreqMedTime.setShowSoftInputOnFocus(false);
        customFreqTimeUnitEnter.setShowSoftInputOnFocus(false);

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
                customFreqTakenEveryLayout.setErrorEnabled(false);

                try
                {
                    Integer.parseInt(customFreqMTakenEveryEnter.getText().toString());

                    if (Integer.parseInt(customFreqMTakenEveryEnter.getText().toString()) == 0)
                    {
                        customFreqTakenEveryLayout.setError("Value must be greater than 0");
                    }
                }
                catch (Exception e)
                {
                    if (!customFreqMTakenEveryEnter.getText().toString().isEmpty())
                    {
                        customFreqTakenEveryLayout.setError("Provided value is too big");
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

        if (medId != -1 && selectedFrequencyTypeIndex == 2)
        {
            long freq = medication.getMedFrequency();
            long displayedFreq = 0;
            int index = 0;

            customFreqStartDate.setText(
                    TimeFormatting.localDateToString(medication.getStartDate().toLocalDate())
            );
            customFreqStartDate.setTag(medication.getStartDate().toLocalDate());

            customFreqMedTime.setText(
                    TimeFormatting.localTimeToString(medication.getStartDate().toLocalTime())
            );
            customFreqMedTime.setTag(medication.getStartDate().toLocalTime());

            if (freq % (60 * 24 * 7) == 0)
            {
                index = 3;
                displayedFreq = freq / (60 * 24  * 7);
            }
            else if (freq % (60 * 24) == 0)
            {
                index = 2;
                displayedFreq = freq / (60 * 24);
            }
            else if (freq % 60 == 0)
            {
                index = 1;
                displayedFreq = freq / (60);
            }
            else
            {
                displayedFreq = freq;
            }

            customFreqTimeUnitEnter.setText(
                    customFreqTimeUnitEnter.getAdapter().getItem(index).toString(), false
            );

            customFreqMTakenEveryEnter.setText(String.valueOf(displayedFreq));
        }
    }

    /**
     * Creates an onClickListener for the save button
     */
    private void setSaveButton()
    {
        saveButton = rootView.findViewById(R.id.saveButton);

        saveButton.setOnClickListener((view ->
        {
            saveMedication();
        }));
    }

    /**
     * Saves medication and calls validation methods. Validation methods also assign/update values
     *  of the medication being created/edited.
     */
    private void saveMedication()
    {
        boolean nameCardValid = isNameCardValid(),
                dosageCardValid = isMedNameAndDosageCardValid(),
                frequencyCardValid = isFrequencyCardValid();
        Intent intent;

        if (!(nameCardValid && dosageCardValid && frequencyCardValid))
        {
            return;
        }

        if (medId == -1)
        {
            intent = new Intent(rootView.getContext(), MainActivity.class);

            long id = db.addMedication(
                    medication.getMedName(),
                    medication.getPatientName(),
                    String.valueOf(medication.getMedDosage()),
                    medication.getMedDosageUnits(),
                    TimeFormatting.localDateTimeToString(medication.getStartDate()),
                    (int) medication.getMedFrequency(),
                    medication.getAlias()
            );

            medication.setMedId(id);

            for (LocalDateTime time : medication.getTimes())
            {
                db.addDoseTime(
                        id, TimeFormatting.formatTimeForDB(time.getHour(), time.getMinute())
                );
            }

        }
        else
        {
            intent = new Intent(rootView.getContext(), MyMedications.class);

            db.updateMedication(medication);

            clearExistingNotifications();

        }

        scheduleNotifications();
        getActivity().finish();
        startActivity(intent);
    }

    /**
     * Validates patient name input card
     * @return True if valid
     */
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

    /**
     * Validates medication name and dosage information are valid. Sets error messages for invalid
     *  items.
     * @return True if valid.
     */
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

        if ((dosageAmountInputLayout.getError() == null || floatIsParsable(dosageAmountInput.getText().toString())) && !dosageAmountInput.getText().toString().isEmpty())
        {
            medication.setMedDosage(Float.parseFloat(dosageAmountInput.getText().toString()));

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
            isValid = false;
        }
        else
        {
            medication.setMedDosageUnits(dosageUnitsInput.getText().toString());
        }

        return isValid;
    }

    /**
     * Determines which frequency option is selected and which form to validate.
     * @return False if no option is selected, else the return value of the validation method for
     *  the selected form.
     */
    private boolean isFrequencyCardValid()
    {
        switch (selectedFrequencyTypeIndex)
        {
            case 0:
                return isMultiplePerDayValid();
            case 1:
                return isDailyValid();
            case 2:
                return isCustomFrequencyValid();
            default:
                frequencyDropdownLayout.setError("Please select a frequency type");
        }

        return false;
    }

    /**
     * Determines if the multiple per day card is valid & sets error messages on any invalid items.
     * @return True if valid
     */
    private boolean isMultiplePerDayValid()
    {
        TextInputLayout multiplePerDayStartDateLayout =
                rootView.findViewById(R.id.multiplePerDayStartDateLayout);

        if (!startDateMultiplePerDay.getText().toString().isEmpty()
            && !numberOfTimersPerDay.getText().toString().isEmpty()
            && intIsParsable(numberOfTimersPerDay.getText().toString())
            && numberOfTimersPerDayLayout.getError() == null)
        {
            LinearLayout ll = rootView.findViewById(R.id.timesPerDayHolder);
            LocalDateTime[] times = new LocalDateTime[ll.getChildCount()];
            LocalDateTime start = LocalDateTime.of((LocalDate) startDateMultiplePerDay.getTag(), LocalTime.now());
            int errorCount = 0;

            multiplePerDayStartDateLayout.setErrorEnabled(false);
            numberOfTimersPerDayLayout.setErrorEnabled(false);

            medication.setStartDate(start);
            medication.setMedFrequency(MINUTES_IN_DAY);

            for (int i = 0; i < ll.getChildCount(); i++)
            {
                TextInputLayout childLayout = (TextInputLayout) ll.getChildAt(i);
                EditText time = childLayout.getEditText();

                childLayout.setErrorEnabled(false);

                if (time.getText().toString().isEmpty())
                {
                    childLayout.setError("Please select a time");

                    errorCount++;
                }
                else
                {
                    times[i] = LocalDateTime.of(start.toLocalDate(), (LocalTime) time .getTag());
                }
            }

            if (errorCount > 0)
            {
                return false;
            }

            medication.setTimes(times);

            return true;
        }

        if (startDateMultiplePerDay.getText().toString().isEmpty())
        {
            multiplePerDayStartDateLayout.setError("Please select a start date.");
        }

        if (numberOfTimersPerDay.getText().toString().isEmpty())
        {
            numberOfTimersPerDayLayout.setError(
                    "Please enter the number of times this medication is taken in a day"
            );
        }

        return false;
    }

    /**
     * Determines if the daily medication card is valid and set error messages on any invalid items.
     * @return True if valid.
     */
    private boolean isDailyValid()
    {
        TextInputLayout dailyStartDateLayout = rootView.findViewById(R.id.startDateTaken);
        TextInputLayout timeTakenLayout = rootView.findViewById(R.id.timeTakenLayout);

        dailyStartDateLayout.setErrorEnabled(false);
        timeTakenLayout.setErrorEnabled(false);

        if (!dailyMedStartDate.getText().toString().isEmpty() && !dailyMedTime.getText().toString().isEmpty()) {
            LocalDateTime[] times = {
                LocalDateTime.of(
                    (LocalDate) dailyMedStartDate.getTag(), (LocalTime) dailyMedTime.getTag()
                )
            };

            medication.setStartDate(times[0]);
            medication.setTimes(times);
            medication.setMedFrequency(MINUTES_IN_DAY);

            return true;
        }

        if (dailyMedStartDate.getText().toString().isEmpty())
        {
            dailyStartDateLayout.setError("Please select a start date.");
        }

        if (dailyMedTime.getText().toString().isEmpty())
        {
            timeTakenLayout.setError("Please select a time.");
        }

        return false;
    }

    /**
     * Determines if the contents of the frequency card are valid if custom frequency is selected.
     * Sets error messages on any invalid items.
     * @return True if valid, false if invalid
     */
    private boolean isCustomFrequencyValid()
    {
        boolean allInputsFilled = !(
            Objects.requireNonNull(customFreqStartDate.getText()).toString().isEmpty()
            && Objects.requireNonNull(customFreqMedTime.getText()).toString().isEmpty()
            && Objects.requireNonNull(customFreqMTakenEveryEnter.getText()).toString().isEmpty()
            && customFreqTimeUnitEnter.getText().toString().isEmpty()
            ) && intIsParsable(Objects.requireNonNull(customFreqMTakenEveryEnter.getText()).toString())
        ;

        if (allInputsFilled)
        {

            LocalDate startDate = (LocalDate) customFreqStartDate.getTag();
            LocalTime startTime = (LocalTime) customFreqMedTime.getTag();
            int selectedTimeUnitIndex = timeUnits.indexOf(customFreqTimeUnitEnter.getText().toString());
            int takenEvery = Integer.parseInt(customFreqMTakenEveryEnter.getText().toString());

            customFreqStartDateLayout.setErrorEnabled(false);
            customFreqTimeTakenLayout.setErrorEnabled(false);
            customFreqTimeUnitLayout.setErrorEnabled(false);

            if (customFreqTakenEveryLayout.getError() != null)
            {
                return false;
            }

            medication.setStartDate(LocalDateTime.of(startDate, startTime));

            switch (selectedTimeUnitIndex)
            {
                case 3:
                    takenEvery *= 7;
                case 2:
                    takenEvery *= 24;
                case 1:
                    takenEvery *= 60;
            }

            medication.setMedFrequency(takenEvery);
            medication.setTimes(new LocalDateTime[]{LocalDateTime.of(startDate, startTime)});

            return true;
        }

        if (customFreqStartDate.getText().toString().isEmpty())
        {
            customFreqStartDateLayout.setError("Please select a start date.");
        }

        if (Objects.requireNonNull(customFreqMedTime.getText()).toString().isEmpty())
        {
            customFreqTimeTakenLayout.setError("Please enter a start time.");
        }

        if (customFreqMTakenEveryEnter.getText().toString().isEmpty())
        {
            customFreqTakenEveryLayout.setError(
                    "Please enter how often the medication is taken."
            );
        }

        if (customFreqTimeUnitEnter.getText().toString().isEmpty())
        {
            customFreqTimeUnitLayout.setError("Please select a time unit.");
        }

        return false;
    }

    /**
     * Determines if a string can be parsed to int
     * @param intToParse String to try to convert
     * @return True if the string can be converted, else false
     */
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

    /**
     * Determines if a string can be parsed to float
     * @param floatToParse String to try to convert
     * @return True if the string can be converted, else false
     */
    private boolean floatIsParsable(String floatToParse)
    {
        try
        {
            Float.parseFloat(floatToParse);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Schedules a notification for medication/dose time
     */
    private void scheduleNotifications()
    {
        long[] medicationTimeIds = db.getMedicationTimeIds(medication);

        if (medication.getMedFrequency() == 1440)
        {
            NotificationHelper.scheduleNotification(
                    rootView.getContext(),
                    medication,
                    LocalDateTime.of(LocalDate.now(), medication.getTimes()[0].toLocalTime()),
                    medication.getMedId()
            );
        }
        else
        {
            for (int i = 0; i < medicationTimeIds.length; i++)
            {
                NotificationHelper.scheduleNotification(
                        rootView.getContext(),
                        medication,
                        LocalDateTime.of(
                                medication.getStartDate().toLocalDate(),
                                medication.getTimes()[i].toLocalTime()
                        ),
                        medicationTimeIds[i] * -1
                );
            }
        }
    }

    private void clearExistingNotifications()
    {
        long[] medIds = db.getMedicationTimeIds(medication);

        if (medIds.length == 0)
        {
            NotificationHelper.deletePendingNotification(medication.getMedId(), rootView.getContext());
        }
        else
        {
            for (long id : medIds)
            {
                NotificationHelper.deletePendingNotification(id * -1, rootView.getContext());
            }
        }
    }
}