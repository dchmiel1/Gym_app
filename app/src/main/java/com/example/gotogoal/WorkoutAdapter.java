package com.example.gotogoal;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;

import static android.view.View.VISIBLE;

public class WorkoutAdapter extends BaseAdapter{

    LayoutInflater inflater;
    MainActivity.Structure[] structures;
    Context c;
    DbHelper dbHelper;

    public WorkoutAdapter(Context c, MainActivity.Structure[] structures){
        this.structures = structures;
        this.c = c;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = MainActivity.dbHelper;
    }

    @Override
    public int getCount() {
        return structures.length;
    }

    @Override
    public Object getItem(int i) {
        return structures[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.workout_listview, null);

        TextView exNameTextView = (TextView) v.findViewById(R.id.exNameTextView);
        ListView listView = (ListView) v.findViewById(R.id.setsListView);
        ImageView deleteImageView = (ImageView) v.findViewById(R.id.deleteImageView);
        View clickView = (View) v.findViewById(R.id.clickView);
        clickView.setOnClickListener(view1 -> updateSets(i));
        clickView.setOnLongClickListener(view1 -> showDelete(v, i));

        RelativeLayout.LayoutParams mParam = new RelativeLayout.LayoutParams((int)(975),(int)(148 + 3 + 3 + structures[i].reps.length * 126));
        v.setLayoutParams(mParam);
        listView.setAdapter(new ArrayAdapter<>(c, R.layout.exercise_in_workout_listview, new String[0]));
        deleteImageView.setOnClickListener(view1 -> dbHelper.deleteByDateAndExercise(structures[i].exercise));

        for(int j = 0; j < structures[i].reps.length; j ++)
            listView.addFooterView(newListViewItem(structures[i].reps[j], structures[i].kgs[j]));
        exNameTextView.setText(structures[i].exercise);

        return v;
    }

    private View newListViewItem(String reps, String kgs){
        View v = inflater.inflate(R.layout.exercise_in_workout_listview, null);
        TextView repsTextView = (TextView) v.findViewById(R.id.repsTextView);
        TextView kgsTextView = (TextView) v.findViewById(R.id.kgsTextView);
        repsTextView.setText(BodyWeightActivity.getProperVal(reps));
        kgsTextView.setText(BodyWeightActivity.getProperVal(kgs));
        return v;
    }

    public void updateSets(int i) {
        Intent showRepsAndKgsActivity = new Intent(c, RepsAndKgsActivity.class);
        showRepsAndKgsActivity.putExtra("ex_name", structures[i].exercise);
        c.startActivity(showRepsAndKgsActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean showDelete(View v, int i){
        ((ImageView) v.findViewById(R.id.deleteImageView)).setVisibility(VISIBLE);
        ((View) v.findViewById(R.id.clickView)).setOnClickListener(view -> hideDelete(v, i));
        v.setBackgroundColor(0xFF1E90FF);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void hideDelete(View v, int i){
        ((ImageView) v.findViewById(R.id.deleteImageView)).setVisibility(View.GONE);
        ((View) v.findViewById(R.id.clickView)).setOnClickListener(view -> updateSets(i));
        v.setBackground(c.getResources().getDrawable(R.drawable.border_dark_orange, null));
    }

}
