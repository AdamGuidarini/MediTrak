package projects.medicationtracker.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Dose;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Medication medication;
    Dose[] doses;
    String timeFormat;
    String dateFormat;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView scheduledDateLabel;
        TextView takenDateLabel;
        TextView dosageAmount;
        LinearLayout barrier;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            scheduledDateLabel = itemView.findViewById(R.id.scheduled_date);
            takenDateLabel = itemView.findViewById(R.id.taken_date);
            dosageAmount = itemView.findViewById(R.id.dosage);
            barrier = itemView.findViewById(R.id.barrier);
        }
    }

    public HistoryAdapter(String dateFormat, String timeFormat, Medication medication) {
        this.timeFormat = timeFormat;
        this.dateFormat = dateFormat;
        this.medication = medication;

        this.doses = new Dose[]{};

        combineDoses(this.medication);
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
        Medication currentMed = getDoseMed(doses[position], medication);

        LocalDateTime scheduledDateTime = doses[position].getDoseTime();
        LocalDateTime takenDateTime = doses[position].getTimeTaken();

        String scheduleDate = DateTimeFormatter.ofPattern(
                dateFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalDate());
        String scheduleTime = DateTimeFormatter.ofPattern(
                timeFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalTime());

        String takenDate = DateTimeFormatter.ofPattern(
                dateFormat, Locale.getDefault()
        ).format(takenDateTime.toLocalDate());
        String takenTime = DateTimeFormatter.ofPattern(
                timeFormat, Locale.getDefault()
        ).format(takenDateTime.toLocalTime());

        holder.scheduledDateLabel.setText(scheduleDate + "\n" + scheduleTime);
        holder.takenDateLabel.setText(takenDate + "\n" + takenTime);
        holder.dosageAmount.setText(currentMed.getDosage() + " " + currentMed.getDosageUnits());

        if (position == doses.length - 1) {
            holder.barrier.setVisibility(View.GONE);
        } else {
            holder.barrier.setBackgroundColor(holder.scheduledDateLabel.getCurrentTextColor());
        }
    }

    @Override
    public int getItemCount() {
        return doses.length;
    }

    Medication getDoseMed(Dose dose, Medication med) {
        return dose.getMedId() == med.getId() ? med : getDoseMed(dose, med.getChild());
    }

    private void combineDoses(Medication currentMed) {
        this.doses = Stream.concat(
                Arrays.stream(this.doses),
                Arrays.stream(currentMed.getDoses())
        ).toArray(Dose[]::new);

        if (currentMed.getChild() != null) {
            combineDoses(currentMed.getChild());
        }
    }
}
