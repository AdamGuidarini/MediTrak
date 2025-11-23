package projects.medicationtracker.Views;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;

import com.google.android.material.card.MaterialCardView;

public class StandardCardView extends MaterialCardView {
    public StandardCardView(@NonNull Context context) {
        super(context);

        setParams();
    }

    private void setParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
        marginLayoutParams.setMargins(25, 20, 25, 20);
        this.requestLayout();
    }
}
