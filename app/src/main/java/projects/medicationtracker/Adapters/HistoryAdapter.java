package projects.medicationtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Dose;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Dose[] doses;
    String timeFormat;
    String dateFormat;

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

    public HistoryAdapter(Dose[] doses, String dateFormat, String timeFormat) {
        this.doses = doses;
        this.timeFormat = timeFormat;
        this.dateFormat = dateFormat;
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
        Context context = holder.scheduledDateLabel.getContext();

        LocalDateTime scheduledDateTime = doses[position].getDoseTime();

        String scheduleDate = DateTimeFormatter.ofPattern(
                dateFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalDate());
        String scheduleTime = DateTimeFormatter.ofPattern(
                timeFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalTime());

        String takenDate = DateTimeFormatter.ofPattern(
                dateFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalDate());
        String takenTime = DateTimeFormatter.ofPattern(
                timeFormat, Locale.getDefault()
        ).format(scheduledDateTime.toLocalTime());

        String schedTime = context.getString(
                R.string.scheduled_time,
                scheduleDate,
                scheduleTime
        );
        String timeTaken = context.getString(
                R.string.time_taken_hist,
                takenDate,
                takenTime
        );

        holder.scheduledDateLabel.setText(schedTime);
        holder.takenDateLabel.setText(timeTaken);
    }

    @Override
    public int getItemCount() {
        return doses.length;
    }
}
