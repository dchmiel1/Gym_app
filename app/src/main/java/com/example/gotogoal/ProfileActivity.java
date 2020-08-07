package com.example.gotogoal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final TextView weightTextView = (TextView) findViewById(R.id.weightTextView);
        final TextView heightTextView = (TextView) findViewById(R.id.heightTextView);
        final EditText heightEditText = (EditText) findViewById(R.id.heightEditText);
        final EditText weightEditText = (EditText) findViewById(R.id.weightEditText);
        final Button changeValBtn = (Button) findViewById(R.id.changeValBtn);
        final Button saveBtn = (Button) findViewById(R.id.saveBtn);
        final TextView bmiTextView = (TextView) findViewById(R.id.bmiTextView);


        changeValBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeValBtn.setVisibility(View.GONE);
                weightEditText.setVisibility(View.VISIBLE);
                heightEditText.setVisibility(View.VISIBLE);
                weightTextView.setVisibility(View.GONE);
                heightTextView.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!heightEditText.getText().toString().equals("") && !weightEditText.getText().toString().equals("")) {
                    String newWeight = getProperVal(weightEditText.getText().toString());
                    String newHeight = getProperVal(heightEditText.getText().toString());
                    String bmi = Double.parseDouble(newWeight)/(Double.parseDouble(newHeight)*Double.parseDouble(newHeight)/10000) + "";
                    weightTextView.setText(newWeight);
                    heightTextView.setText(newHeight);
                    weightEditText.setText(newWeight);
                    heightEditText.setText(newHeight);
                    bmiTextView.setText(getProperVal(bmi));
                    changeValBtn.setVisibility(View.VISIBLE);
                    weightEditText.setVisibility(View.GONE);
                    heightEditText.setVisibility(View.GONE);
                    weightTextView.setVisibility(View.VISIBLE);
                    heightTextView.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    private String getProperVal(String val){
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
}