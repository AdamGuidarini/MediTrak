package projects.medicationtracker.SimpleClasses;

import java.time.LocalDateTime;

public class Note {
    private long noteId;
    private long medId;
    private String note;
    private LocalDateTime noteTime;
    private LocalDateTime modifiedTime;

    public Note(long note_Id, long med_Id, String nt, LocalDateTime time) {
        noteId = note_Id;
        medId = med_Id;
        note = nt;
        noteTime = time;
    }

    public Note(long noteId, long medId, String noteText, LocalDateTime createdTime, LocalDateTime modifiedTime) {
        this.noteId = noteId;
        this.medId = medId;
        note = noteText;
        noteTime = createdTime;
        this.modifiedTime = modifiedTime;
    }

    // Getters and setters
    public long getNoteId() {
        return noteId;
    }
    public void setNoteId(long noteId) { this.noteId = noteId; }

    public long getMedId() {
        return medId;
    }
    public void setMedId(long medId) { this.medId = medId; }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getNoteTime() {
        return noteTime;
    }
    public void setNoteTime(LocalDateTime time) { this.noteTime = time; }

    public LocalDateTime getModifiedTime() { return modifiedTime; }

    public void setModifiedTime(LocalDateTime modifiedTime) { this.modifiedTime = modifiedTime; }
}
