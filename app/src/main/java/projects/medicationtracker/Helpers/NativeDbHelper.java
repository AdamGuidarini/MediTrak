package projects.medicationtracker.Helpers;

public class NativeDbHelper {
    static {
        System.loadLibrary("medicationtracker");
    }

    private final String dbPath;

    public NativeDbHelper(String databasePath) {
        dbPath = databasePath;
        dbCreate(dbPath);
    }

    public void create() {
        dbCreate(dbPath);
    }

    public void upgrade(int version) {
        dbUpgrade(dbPath, version);
    }

    public boolean dbExport(String exportPath, String[] ignoredTables) {
        return dbExporter(dbPath, exportPath, ignoredTables);
    }

    public boolean dbImport(String importPath, String[] ignoredTables) {
        return dbImporter(dbPath, importPath, ignoredTables);
    }

    private  native void dbCreate(String dbPath);
    private native void dbUpgrade(String dbPath, int version);
    private native boolean dbExporter(String databaseName, String exportDirectory, String[] ignoredTables);
    private native boolean dbImporter(String dbPath, String importPath, String[] ignoredTables);
}
