package projects.medicationtracker.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Dose;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Dose dose;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView scheduledDateLabel;
        TextView takenDateLabel;
        TextView dosageAmount;
        TextView dosageUnits;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            scheduledDateLabel = itemView.findViewById(R.id.scheduled_date);
            takenDateLabel = itemView.findViewById(R.id.taken_date);
            dosageAmount = itemView.findViewById(R.id.dosage);
            dosageUnits = itemView.findViewById(R.id.units);
        }
    }

    public HistoryAdapter(Dose dose) {
        this.dose = dose;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
