package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.ArrayList;

import projects.medicationtracker.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicationScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicationScheduleFragment extends Fragment
{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MED_ID = "medId";
    private static final String DATE_LABEL = "dateLabel";
    private View rootView;

    private long medId;

    public MedicationScheduleFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param medId medication ID.
     * @return A new instance of fragment MedicationScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MedicationScheduleFragment newInstance(Long medId, String dateLabel)
    {
        MedicationScheduleFragment fragment = new MedicationScheduleFragment();
        Bundle args = new Bundle();
        args.putLong(MED_ID, medId);
        args.putString(DATE_LABEL, dateLabel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            medId = getArguments().getLong(MED_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_medication_schedule, container, false);

        return rootView;
    }
}