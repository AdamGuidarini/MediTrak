package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);
        db.testDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public void onMyMedicationsClick(MenuItem item)
    {
    }

    public void onAddMedicationClick(MenuItem item)
    {
        Intent intent = new Intent(this, addMedication.class);
        startActivity(intent);
    }

    public void onEditMedicationClick(MenuItem item)
    {
    }

    public void onSettingsClick(MenuItem item)
    {
    }
}