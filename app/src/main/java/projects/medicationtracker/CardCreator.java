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
