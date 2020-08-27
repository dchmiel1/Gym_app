package com.example.gotogoal;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.shawnlin.numberpicker.NumberPicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BodyWeightActivity extends AppCompatActivity {

    enum WhichPicker{ WEIGHT, HEIGHT, SEX, BIRTHDAY};

    private WhichPicker whichPicker;
    private DbHelper dbHelper;
    private SimpleDateFormat dateFormat;
    private BottomNavigationView navigationView;
    private String[] afterDecimal;
    private String[] sexes;
    private String[] heights;
    private String[] years;
    private String[] months;
    private String[] days;
    private int numOfPoints;
    private PointsGraphSeries<DataPoint> pointsGraphSeries;
    private LineGraphSeries<DataPoint> lineGraphSeries;
    private boolean graphVisible;

    //profile items
    private ListView profileListView;
    private ProfileListViewAdapter profileListViewAdapter;
    private RelativeLayout pickersLayout;
    private View darkView;
    private NumberPicker sexPicker;
    private NumberPicker heightPicker;
    private NumberPicker datePicker1;
    private NumberPicker datePicker2;
    private NumberPicker datePicker3;
    private RelativeLayout datePickersLayout;

    //bodyWeight items
    private GraphView weightGraph;
    private ImageButton addWeightImageButton;
    private NumberPicker weightPicker1;
    private NumberPicker weightPicker2;
    private TextView dataPointDetails;
    private TextView notEnoughDataPointsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_weight);

        profileListView = (ListView) findViewById(R.id.profileListView);
        weightGraph = (GraphView) findViewById(R.id.weightGraph);
        navigationView = (BottomNavigationView) findViewById(R.id.profileNavigationBar);
        addWeightImageButton = (ImageButton) findViewById(R.id.addWeightImageButton);
        weightPicker1 = (NumberPicker) findViewById(R.id.bodyWeightPicker1);
        weightPicker2 = (NumberPicker) findViewById(R.id.bodyWeightPicker2);
        sexPicker = (NumberPicker) findViewById(R.id.sexPicker);
        heightPicker = (NumberPicker) findViewById(R.id.heightPicker);
        datePicker1 = (NumberPicker) findViewById(R.id.datePicker1);
        datePicker2 = (NumberPicker) findViewById(R.id.datePicker2);
        datePicker3 = (NumberPicker) findViewById(R.id.datePicker3);
        pickersLayout = (RelativeLayout) findViewById(R.id.pickersLayout);
        darkView = (View) findViewById(R.id.darkView);
        datePickersLayout = (RelativeLayout) findViewById(R.id.datePickersLayout);
        dataPointDetails = (TextView) findViewById(R.id.dataPointDetails);
        notEnoughDataPointsTextView = (TextView) findViewById(R.id.notEnoughDataPointsTextView);

        whichPicker = null;
        datePickersLayout.setVisibility(View.GONE);
        darkView.setVisibility(View.GONE);
        pickersLayout.setVisibility(View.GONE);
        profileListView.setVisibility(View.GONE);
        dbHelper = MainActivity.dbHelper;
        setAdapter();
        profileListView.setAdapter(profileListViewAdapter);
        dateFormat = new SimpleDateFormat("dd.MM");

        weightGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX){
                if(isValueX){
                    return dateFormat.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, false);
                }
            }
        });
        setGraphSeries();

        if(graphVisible){
           notEnoughDataPointsTextView.setVisibility(View.GONE);
           dataPointDetails.setVisibility(View.GONE);
        }
        if(graphVisible) {
            weightGraph.addSeries(lineGraphSeries);
            weightGraph.addSeries(pointsGraphSeries);
            weightGraph.getGridLabelRenderer().setNumHorizontalLabels(numOfPoints);
        }

        setPickers();

        if(graphVisible)
            pointsGraphSeries.setOnDataPointTapListener((series, dataPoint) -> {
                dataPointDetails.setText(dataPoint.getX() + ", "+ dataPoint.getY()+ "");
            });

        navigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.toString().equals("Body weight"))
                showBodyWeight();
            else
                showProfile();
            return true;
        });

        addWeightImageButton.setOnClickListener(view -> {
            if (darkView.getVisibility() == View.GONE) {
                pickersLayout.setVisibility(View.VISIBLE);
                darkView.setVisibility(View.VISIBLE);
                whichPicker = WhichPicker.WEIGHT;
                for(int i = 0; i < navigationView.getMenu().size(); i++)
                    navigationView.getMenu().getItem(i).setEnabled(false);
                weightPicker1.setVisibility(View.VISIBLE);
                weightPicker2.setVisibility(View.VISIBLE);
            }else{
                darkView.setVisibility(View.GONE);
                pickersLayout.setVisibility(View.GONE);
                for(int i = 0; i < navigationView.getMenu().size(); i++)
                    navigationView.getMenu().getItem(i).setEnabled(true);
                switch(whichPicker){
                    case HEIGHT:
                        heightPicker.setVisibility(View.GONE);
                        int newHeight = Integer.parseInt(heights[heightPicker.getValue()-1].substring(0, heights[heightPicker.getValue()].length()-3));
                        System.out.println(newHeight);
                        dbHelper.updateProfileInfo(0, null, newHeight, 0, whichPicker);
                        break;
                    case WEIGHT:
                        weightPicker1.setVisibility(View.GONE);
                        weightPicker2.setVisibility(View.GONE);
                        String newWeight = getProperVal(weightPicker1.getValue() +
                                String.valueOf(afterDecimal[weightPicker2.getValue()-1]).substring(0, String.valueOf(afterDecimal[weightPicker2.getValue()]).length()-2));
                        System.out.println(newWeight);
                        dbHelper.insertWeight(Double.parseDouble(newWeight));
                        dbHelper.updateProfileInfo(0, null, 0, Double.parseDouble(newWeight), whichPicker);
                        weightGraph.removeAllSeries();
                        setGraphSeries();
                        if(graphVisible) {
                            weightGraph.addSeries(lineGraphSeries);
                            weightGraph.addSeries(pointsGraphSeries);
                        }
                        break;
                    case SEX:
                        sexPicker.setVisibility(View.GONE);
                        int newSex = sexPicker.getValue();
                        System.out.println(newSex);
                        dbHelper.updateProfileInfo(newSex, null, 0, 0, whichPicker);
                        break;
                    default:
                        datePickersLayout.setVisibility(View.GONE);
                        String newDayOfBirth = datePicker3.getValue() + "." + months[datePicker2.getValue()-1] + "." + days[datePicker1.getValue()-1];
                        System.out.println(newDayOfBirth);
                        dbHelper.updateProfileInfo(0, newDayOfBirth, 0, 0, whichPicker);
                        break;
                }
                if(navigationView.getSelectedItemId() == R.id.profile_info){
                    addWeightImageButton.setVisibility(View.GONE);
                }
            }
            setAdapter();
            profileListView.setAdapter(profileListViewAdapter);
        });

        profileListView.setOnItemClickListener((adapterView, view, i, l) -> {
            pickersLayout.setVisibility(View.VISIBLE);
            darkView.setVisibility(View.VISIBLE);
            for(int j = 0; j < navigationView.getMenu().size(); j++)
                navigationView.getMenu().getItem(j).setEnabled(false);
            addWeightImageButton.setVisibility(View.VISIBLE);
            switch(i){
                case 0:
                    whichPicker = WhichPicker.SEX;
                    sexPicker.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    whichPicker = WhichPicker.BIRTHDAY;
                    pickersLayout.setVisibility(View.GONE);
                    datePickersLayout.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    whichPicker = WhichPicker.HEIGHT;
                    heightPicker.setVisibility(View.VISIBLE);
                    break;
                default:
                    whichPicker = WhichPicker.WEIGHT;
                    weightPicker1.setVisibility(View.VISIBLE);
                    weightPicker2.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    public static String getProperVal(String val){
        String sVal = Double.parseDouble(val)+"";
        for(int i = 0; i < sVal.length(); i++){
            if(sVal.charAt(i) =='.') {
                if (sVal.charAt(i + 1) == '0' && sVal.length() >= i + 2) {
                    return sVal.substring(0, i);
                } else {
                    if (sVal.length() > i + 2) {
                        return sVal.substring(0, i + 2);
                    }
                }
            }
        }
        return sVal;
    }

    private void setGraphSeries(){
        Cursor c = dbHelper.getWeightData();
        System.out.println(c.getCount());
        if(c.getCount() > 1){
            DataPoint[] dataPoints = new DataPoint[c.getCount()];
            SimpleDateFormat formatOfDateInSql = new SimpleDateFormat("yyyy.MM.dd");
            int i = 0;
            while (c.moveToNext()) {
                try {
                    dataPoints[i] = new DataPoint(formatOfDateInSql.parse(c.getString(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_DATE))), c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ++i;
            }
            numOfPoints = dataPoints.length - 1;
            pointsGraphSeries = new PointsGraphSeries<>(dataPoints);
            lineGraphSeries = new LineGraphSeries<>(dataPoints);
            pointsGraphSeries.setColor(Color.parseColor("#FFFF8800"));
            lineGraphSeries.setColor(Color.parseColor("#FFFF8800"));
            notEnoughDataPointsTextView.setVisibility(View.GONE);
            graphVisible = true;
        }else
            graphVisible = false;
    }

    private void showBodyWeight(){
        profileListView.setVisibility(View.GONE);
        weightGraph.setVisibility(View.VISIBLE);
        addWeightImageButton.setVisibility(View.VISIBLE);
        if(graphVisible) {
            notEnoughDataPointsTextView.setVisibility(View.GONE);
            dataPointDetails.setVisibility(View.GONE);
        }
        else {
            notEnoughDataPointsTextView.setVisibility(View.VISIBLE);
            dataPointDetails.setVisibility(View.VISIBLE);
        }
    }

    private void showProfile(){
        profileListView.setVisibility(View.VISIBLE);
        weightGraph.setVisibility(View.GONE);
        addWeightImageButton.setVisibility(View.GONE);
        notEnoughDataPointsTextView.setVisibility(View.GONE);
        dataPointDetails.setVisibility(View.GONE);
    }

    private void setAdapter(){
        String[] titles = getResources().getStringArray(R.array.profile_list_view_titles);
        String[] units = getResources().getStringArray(R.array.units);
        String[] answers = new String[titles.length];
        Cursor c = dbHelper.getProfileInfo();
        if(c.getCount() > 0) {
            c.moveToNext();
            if (c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_ONE_REP)) == 0)
                answers[0] = "male";
            else
                answers[0] = "female";
            answers[1] = c.getString(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_DATE));
            answers[2] = c.getString(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_REPS));
            answers[3] = c.getString(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED));
        }else{
            for(int i = 0; i < answers.length; i ++)
                answers[i] = "";
        }
        profileListViewAdapter = new ProfileListViewAdapter(this, titles, answers, units);
    }

    private void setPickers(){
        afterDecimal = new String[10];
        sexes = new String[] {"male", "female"};
        years = new String[100];
        heights = new String[150];
        days = new String[31];
        months = new String[12];
        for(int i = 0; i < afterDecimal.length; i ++)
            afterDecimal[i] = String.valueOf(Math.round((i) * 0.1 * 100) / 100.0).substring(1) + " kg";
        for(int i = 0; i < heights.length; i ++)
            heights[i] = i+100 + " cm";
        for(int i = 0; i < years.length; i ++)
            years[i] = String.valueOf(i + 1920);
        for(int i = 0; i < days.length; i++){
            if(i +1 < 10)
                days[i] = "0" + (i+1);
            else
                days[i] = i+1 + "";
        }
        for(int i = 0; i < months.length; i++){
            if(i +1 < 10)
                months[i] = "0" + (i+1);
            else
                months[i] = i+1 + "";
        }
        sexPicker.setDisplayedValues(sexes);
        weightPicker2.setDisplayedValues(afterDecimal);
        heightPicker.setDisplayedValues(heights);
        datePicker3.setDisplayedValues(years);
        datePicker2.setDisplayedValues(months);
        datePicker1.setDisplayedValues(days);
    }
}