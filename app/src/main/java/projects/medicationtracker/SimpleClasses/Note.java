package projects.medicationtracker.SimpleClasses;

import java.time.LocalDateTime;

public class Note
{
    private final long noteId;
    private final long medId;
    private String note;
    private final LocalDateTime noteTime;

    public Note(long note_Id, long med_Id, String nt, LocalDateTime time)
    {
        noteId = note_Id;
        medId = med_Id;
        note = nt;
        noteTime = time;
    }

    // Getters and setters
    public long getNoteId()
    {
        return noteId;
    }

    public long getMedId()
    {
        return medId;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public LocalDateTime getNoteTime()
    {
        return noteTime;
    }

}
