package projects.medicationtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.time.LocalTime;

public class CardCreator
{
    /**
     * Creates a CardView containing all information on a Medication
     * @param medication The Medication whose details will be displayed.
     * @param baseLayout The LinearLayout in which to place the card
     **************************************************************************/
    public static void createMyMedCards(Medication medication, LinearLayout baseLayout, Activity activity)
    {
        Context context = baseLayout.getContext();
        CardView thisMedCard = new CardView(context);
        LinearLayout thisMedLayout = new LinearLayout(context);
        thisMedLayout.setOrientation(LinearLayout.VERTICAL);
        baseLayout.addView(thisMedCard);

        setCardParams(thisMedCard);

        thisMedCard.addView(thisMedLayout);

        // Add name to thisMedLayout
        TextView name = new TextView(context);
        String nameLabel = "Medication name: " + medication.getMedName();
        TextViewUtils.setTextViewParams(name, nameLabel, thisMedLayout);

        // Add Dosage
        TextView doseInfo = new TextView(context);
        String doseInfoLabel = "Dosage: " + medication.getMedDosage() + " " + medication.getMedDosageUnits();
        TextViewUtils.setTextViewParams(doseInfo, doseInfoLabel, thisMedLayout);

        // Add Frequency
        TextView freq = new TextView(context);
        String freqLabel;

        if (medication.getMedFrequency() == 1440 && (medication.getTimes().length == 1))
        {
            String time = TimeFormatting.localTimeToString(medication.getTimes()[0].toLocalTime());
            freqLabel = "Taken daily at: " + time;
        }
        else if (medication.getMedFrequency() == 1440 && (medication.getTimes().length > 1))
        {
            freqLabel = "Taken daily at: ";

            for (int i = 0; i < medication.getTimes().length; i++)
            {
                LocalTime time = medication.getTimes()[i].toLocalTime();
                freqLabel += TimeFormatting.localTimeToString(time);

                if (i != (medication.getTimes().length - 1))
                    freqLabel += ", ";
            }
        }
        else
            freqLabel = "Taken every: " + TimeFormatting.freqConversion(medication.getMedFrequency());

        TextViewUtils.setTextViewParams(freq, freqLabel, thisMedLayout);

        // Add alias (if exists)
        if (!medication.getAlias().equals(""))
        {
            TextView alias = new TextView(context);
            String aliasLabel = "Alias: " + medication.getAlias();
            TextViewUtils.setTextViewParams(alias, aliasLabel, thisMedLayout);
        }

        // Add start date
        TextView startDate = new TextView(context);
        String startDateLabel = "Taken Since: " + TimeFormatting.localDateToString(medication.getStartDate().toLocalDate());
        TextViewUtils.setTextViewParams(startDate, startDateLabel, thisMedLayout);

        // Add LinearLayout for buttons
        Intent intent = new Intent(context, MedicationNotes.class);
        intent.putExtra("medId", medication.getMedId());
        ButtonManager.createActivityButton("Notes", thisMedLayout, context, intent, activity);

        intent = new Intent(context, EditMedication.class);
        intent.putExtra("medId", medication.getMedId());
        ButtonManager.createActivityButton("Edit", thisMedLayout, context, intent, activity);
    }

    /**
     * Creates a CardView with a note in it
     * @param note The Note in the CardView
     * @param baseLayout The LinearLayout the holds the CardView
     **************************************************************************/
    public static void createNoteCard(Note note, LinearLayout baseLayout)
    {
        Context context = baseLayout.getContext();
        CardView noteCard = new CardView(context);
        LinearLayout cardLayout = new LinearLayout(context);

        cardLayout.setOrientation(LinearLayout.VERTICAL);
        setCardParams(noteCard);

        baseLayout.addView(noteCard);
        noteCard.addView(cardLayout);

        TextView noteText = new TextView(context);
        TextViewUtils.setTextViewParams(noteText, "\"" + note.getNote() + "\"", cardLayout);

        TextView noteDate = new TextView(context);
        String noteDateLabel = "Date: " + TimeFormatting.localDateToString(note.getNoteTime().toLocalDate())
                + " at: " + TimeFormatting.localTimeToString(note.getNoteTime().toLocalTime());
        TextViewUtils.setTextViewParams(noteDate, noteDateLabel, cardLayout);

        noteText.setTag(note);
    }

    /**
     * Sets a CardView's parameters to a set standard
     * @param cardView The CardView whose parameters will be set
     **************************************************************************/
    public static void setCardParams(CardView cardView)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        marginLayoutParams.setMargins(25, 40, 10, 10);
        cardView.requestLayout();
        cardView.setRadius(30);
    }
}
