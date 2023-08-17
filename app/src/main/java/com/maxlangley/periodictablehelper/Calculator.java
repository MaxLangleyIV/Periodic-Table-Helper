package com.maxlangley.periodictablehelper;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Calculator extends AppCompatActivity {

    private ConstraintLayout mainLayout;
    private TextView output;
    private TextView outputLabel;
    private EditText input;
    private JSONArray jsonArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        mainLayout = findViewById(R.id.mainConstraintLayout);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(R.string.calculator);
        setSupportActionBar(myToolbar);

        jsonArray = getJSONArray(getIntent());
        output = findViewById(R.id.calcOutput);
        outputLabel = findViewById(R.id.output_label);
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
        //Toast.makeText(this, (input.getText().toString().toUpperCase()), Toast.LENGTH_SHORT).show();
        hideKeyboard();

        showCalcResult(calculateMolarMass(parseMolecularFormula(input.getText().toString())));

    }

    public double getElementMassFromJSONArray(String name){
        if (name.length() <= 2){
            for (Object elem : jsonArray){
                JSONObject thisElem = (JSONObject) elem;
                String elemSymbol = (String) thisElem.get("symbol");

                assert elemSymbol != null;
                if (name.equals(elemSymbol)){
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

    public List<List<String>> parseMolecularFormula(String formula){
        List<List<String>> stack = new ArrayList<>();
        List<String> elements = new ArrayList<>();

        int i = 0;
        while (i < formula.length()) {

            if (formula.charAt(i) == '(') {
                stack.add(elements);
                elements = new ArrayList<>();
                i++;
            }

            else if (formula.charAt(i) == ')') {

                i++;
                int multiplier = 1;
                if (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                    multiplier = Character.getNumericValue(formula.charAt(i));
                    i++;
                }
                List<String> poppedElements = elements;
                elements = stack.remove(stack.size() - 1);

                for (String poppedElement : poppedElements) {
                    String[] elementInfo = poppedElement.split(",");
                    String elementSymbol = elementInfo[0];
                    int count = Integer.parseInt(elementInfo[1]);
                    elements.add(elementSymbol + "," + (count * multiplier));
                }
            }

            else {
                Pattern elementPattern = Pattern.compile("([A-Z][a-z]*)(\\d*)");
                Matcher elementMatcher = elementPattern.matcher(formula.substring(i));

                if (elementMatcher.find()) {

                    String elementSymbol = elementMatcher.group(1);
                    String countStr = elementMatcher.group(2);

                    int count = (countStr.isEmpty()) ? 1 : Integer.parseInt(countStr);
                    elements.add(elementSymbol + "," + count);
                    i += elementSymbol.length() + countStr.length();
                }
            }
        }

        List<List<String>> result = new ArrayList<>();
        for (String element : elements) {
            String[] elementInfo = element.split(",");
            List<String> infoList = new ArrayList<>();
            infoList.add(elementInfo[0]);
            infoList.add(elementInfo[1]);
            result.add(infoList);
        }

        return result;
    }

    public double calculateMolarMass(List<List<String>> elements){

        System.out.println(elements);

        double totalMass = 0.0;

        for (List<String> element : elements){
            String thisElem = element.get(0);

            System.out.println(thisElem);
            double multiplier = Double.parseDouble(element.get(1));
            System.out.println(multiplier);
            double elemMass = getElementMassFromJSONArray(thisElem);
            System.out.println(elemMass);

            totalMass += elemMass * multiplier;
        }

        return  totalMass;
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
        DecimalFormat format = new DecimalFormat("#.####");
        String formattedResult = format.format(result);

        //output.setText(String.valueOf(result) + " g/mol");
        output.setText(formattedResult + " g/mol");
        outputLabel.setVisibility(View.VISIBLE);
        output.setVisibility(View.VISIBLE);
    }

    public void clearInput(View view) {
        output.setText("");
        output.setVisibility(View.INVISIBLE);
        outputLabel.setVisibility(View.INVISIBLE);
        input.setText("");
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
    }

}
