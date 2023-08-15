package com.maxlangley.periodictablehelper;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Calculator extends AppCompatActivity {
    private TextView output;
    private EditText input;
    private JSONArray jsonArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(R.string.calculator);
        setSupportActionBar(myToolbar);

        jsonArray = getJSONArray(getIntent());
        output = findViewById(R.id.calcOutput);
        input = findViewById(R.id.calcTextInput);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.calc_menu, menu);

        return true;
    }


    public JSONArray getJSONArray(Intent intent){
        try {
            return (JSONArray) (new JSONParser().parse(intent.getStringExtra("json")));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onInput(View view) {
        Toast.makeText(this, (input.getText().toString().toUpperCase()), Toast.LENGTH_SHORT).show();
        showCalcResult(calculateAtomicMass(input.getText().toString().toUpperCase()));

    }

    public double getElementMassFromJSONArray(String name){
        if (name.length() <= 2){
            for (Object elem : jsonArray){
                JSONObject thisElem = (JSONObject) elem;
                String elemSymbol = (String) thisElem.get("symbol");

                assert elemSymbol != null;
                if (name.equals(elemSymbol.toUpperCase())){
                    return (double) thisElem.get("atomic_mass");
                }
            }
        }
        else {
            for (Object elem : jsonArray){
                JSONObject thisElem = (JSONObject) elem;
                String elemName = (String) thisElem.get("name");

                assert elemName != null;
                if (name.equals(elemName.toUpperCase())){
                    return (double) thisElem.get("atomic_mass");
                }
            }
        }
        //Toast.makeText(this, name + " not found. Please check your input and re-enter.", Toast.LENGTH_SHORT).show();
        return 0.0;
    }


    public double calculateAtomicMass(String calcInput){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        //String symbols = "+-*/%^()[]";

        String elem;
        String elemCountString;
        double totalMass = 0.0;
        StringBuilder thisElem = new StringBuilder();
        StringBuilder elemCount = new StringBuilder();


        for (int i = 0; i < calcInput.length(); i++) {

            char thisChar = calcInput.charAt(i);
            String currentChar = String.valueOf(thisChar);

            if (numbers.contains(currentChar)) {

                elemCount.append(currentChar);

            }
            else if (alphabet.contains(currentChar.toUpperCase())) {

                thisElem.append(currentChar);

            }

            if (currentChar.equals(" ") || i == calcInput.length()-1) {

                if (elemCount.length() == 0){elemCountString = "1";}
                else {elemCountString = elemCount.toString();}
                elem = thisElem.toString();

                double thisCount = (double) Integer.parseInt(elemCountString);
                double elemMass = getElementMassFromJSONArray(elem);
                totalMass += thisCount * elemMass;
                thisElem.setLength(0);
                elemCount.setLength(0);
            }
        }

        return totalMass;
    }

    public void showCalcResult(double result){
        output.setText(String.valueOf(result) + " g/mol");
    }

    public void clearInput(View view) {
        output.setText("");
        input.setText("");
    }

}
