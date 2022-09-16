package projects.medicationtracker;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

public class CardCreator
{
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
