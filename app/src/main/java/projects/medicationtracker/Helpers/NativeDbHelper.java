package projects.medicationtracker.Helpers;

public class NativeDbHelper {
    static {
        System.loadLibrary("medicationtracker");
    }

    public NativeDbHelper() {}

    public native void dbCreate(String dbPath);
    public native void dbUpdate(String dbPath);
    public native boolean dbExporter(String databaseName, String exportDirectory, String[] ignoredTables);
    public native boolean dbImporter(String dbPath, String importPath, String[] ignoredTables);
}
