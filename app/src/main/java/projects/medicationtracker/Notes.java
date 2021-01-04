package projects.medicationtracker;

import java.sql.Timestamp;

public class Notes
{
    private int userId;
    private int medId;
    private String Note;
    private Timestamp noteTime;

    // Getters and setters
    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public int getMedId()
    {
        return medId;
    }

    public void setMedId(int medId)
    {
        this.medId = medId;
    }

    public String getNote()
    {
        return Note;
    }

    public void setNote(String note)
    {
        Note = note;
    }

    public Timestamp getNoteTime()
    {
        return noteTime;
    }

    public void setNoteTime(Timestamp noteTime)
    {
        this.noteTime = noteTime;
    }


}
