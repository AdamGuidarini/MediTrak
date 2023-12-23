package projects.medicationtracker.Helpers;

public class NativeDbHelper {
    static {
        System.loadLibrary("medicationtracker");
    }

    public NativeDbHelper() {}

    public native boolean dbExporter(String databaseName, String exportDirectory, String[] ignoredTables);
    public native boolean dbImporter(String dbPath, String importPath, String[] ignoredTables);
}
