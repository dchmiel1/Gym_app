package com.example.gotogoal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ExercisesActivity extends AppCompatActivity {

    String exercises[];
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        final ListView listView = (ListView) findViewById(R.id.listView);
        Resources res = getResources();
        int id = getIntent().getExtras().getInt("muscleId");
        switch(id){
            case 0:
                exercises = res.getStringArray(R.array.chest_ex);
                break;
            case 1:
                exercises = res.getStringArray(R.array.back_ex);
                break;
            case 2:
                exercises = res.getStringArray(R.array.shoulders_ex);
                break;
            case 3:
                exercises = res.getStringArray(R.array.biceps_ex);
                break;
            case 4:
                exercises = res.getStringArray(R.array.triceps_ex);
                break;
            case 5:
                exercises = res.getStringArray(R.array.legs_ex);
                break;
            case 6:
                exercises = res.getStringArray(R.array.abs_ex);
                break;
            default:
                exercises = null;
                break;
        }
        dbHelper = MainActivity.dbHelper;

        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.exercise_listview, exercises));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                dbHelper.insertContact(listView.getItemAtPosition(i).toString());
                startActivity(showMainActivityIntent);

            }
        });

    }
}