package com.maxlangley.periodictablehelper;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private JSONArray elementsJSON;
    private ViewGroup mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (LinearLayout) findViewById(R.id.inner_linear_layout);
        elementsJSON = getElementsArray();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(R.string.app_name);
        setSupportActionBar(myToolbar);

        sortAndRefresh(mainLayout, "numericAscending", elementsJSON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "Paused main activity", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "onDestroy main activity", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_1, menu);

        MenuItem searchViewItem = menu.findItem(R.id.menuSearch);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();

        searchView.setQueryHint("Search for an element.");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchAndRefresh(mainLayout, query, elementsJSON);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewGroup layout = findViewById(R.id.inner_linear_layout);

        switch (item.getItemId()){
            case (R.id.alphabeticalNameSortAscending):
                Toast.makeText(this,"Sorted by alphabetically, by name, from A - Z.", Toast.LENGTH_SHORT).show();
                sortAndRefresh(layout, "ascending", elementsJSON);
                break;

            case (R.id.alphabeticalNameSortDescending):
                Toast.makeText(this,"Sorted by alphabetically, by name, from Z - A.", Toast.LENGTH_SHORT).show();
                layout = findViewById(R.id.inner_linear_layout);
                sortAndRefresh(layout, "descending", elementsJSON);
                break;

            case (R.id.atomicNumberSortAscending):
                Toast.makeText(this,"Sorted by numerically, in ascending order.", Toast.LENGTH_SHORT).show();
                layout = findViewById(R.id.inner_linear_layout);
                sortAndRefresh(layout, "numericAscending", elementsJSON);
                break;

            case (R.id.atomicNumberSortDescending):
                Toast.makeText(this,"Sorted by numerically, in descending order.", Toast.LENGTH_SHORT).show();
                layout = findViewById(R.id.inner_linear_layout);
                sortAndRefresh(layout, "numericDescending", elementsJSON);
                break;

            case  (R.id.alphabeticalSymbolSortAscending):
                Toast.makeText(this,"Sorted by alphabetically, by symbol, from A - Z.", Toast.LENGTH_SHORT).show();
                layout = findViewById(R.id.inner_linear_layout);
                sortAndRefresh(layout, "symbolAscending", elementsJSON);
                break;
        }
        return true;
    }

    public String getJSONString(String fileName){
        String json;

        try
        {
            InputStream fileStream = getAssets().open(fileName);
            int size = fileStream.available();
            byte[] buffer = new byte[size];
            fileStream.read(buffer);
            fileStream.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public JSONArray getElementsArray(){
        try
        {
            Object preJSONObj = new JSONParser().parse(getJSONString("periodicTable.json"));
            JSONObject jsonData = (JSONObject) preJSONObj;

            return (JSONArray) jsonData.get("elements");

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void appendElementsAsViews(JSONArray jsonArray, ViewGroup view) {
        int dynamicId = 0;

        for (Object elem : jsonArray) {
            JSONObject element = (JSONObject) elem;
            dynamicId++;

            final TextView mainTextView = new TextView(this);
            final TextView hiddenTextView = new TextView(this);

            //MAIN TEXT STRINGS
            String elementName = (String) element.get("name");
            String elemNumber = String.valueOf(element.get("number"));
            String elemSymbol = (String) element.get("symbol");
            String elemPhase = (String) element.get("phase");
            String elementText = elemNumber
                                + " (" + elemSymbol + ") - "
                                + elementName;

            //SET MAIN TEXT / PROPERTIES
            mainTextView.setId(dynamicId);
            mainTextView.setText(elementText);
            mainTextView.setTextColor(Color.BLACK);
            mainTextView.setGravity(Gravity.CENTER);
            mainTextView.setTextSize(25);
            mainTextView.setPadding(0,50,0,50);
            mainTextView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mainTextView.setBackground(getDrawable(R.drawable.border_basic));
                }
            }
            mainTextView.setOnClickListener(v -> {
                if (hiddenTextView.getVisibility() == View.GONE) {
                    hiddenTextView.setVisibility(View.VISIBLE);
                } else if (hiddenTextView.getVisibility() == View.VISIBLE) {
                    hiddenTextView.setVisibility(View.GONE);
                }
            });
            //HIDDEN TEXT STRINGS
            String atomicMass = String.valueOf(element.get("atomic_mass"));
            String density = String.valueOf(element.get("density"));
            String statsString = "\nSymbol: " + elemSymbol +
                    "<br>Atomic Number: " + elemNumber +
                    "<br>Atomic Mass: " + atomicMass + " amu" +
                    "<br>Density: " + density + " g/cm<sup><small>3</small></sup>" +
                    "<br>Phase: " + elemPhase;

            //SET HIDDEN TEXT
            hiddenTextView.setId(++dynamicId);
            hiddenTextView.setText(Html.fromHtml(statsString));
            hiddenTextView.setTextColor(Color.WHITE);
            hiddenTextView.setBackgroundColor(Color.BLACK);
            hiddenTextView.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
            hiddenTextView.setTextSize(20);
            hiddenTextView.setGravity(Gravity.CENTER);
            hiddenTextView.setPadding(0,10,0,10);
            hiddenTextView.setVisibility(View.GONE);
            hiddenTextView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (hiddenTextView.getVisibility() != View.GONE) {
                        hiddenTextView.setVisibility(View.GONE);
                    }
                }
            });

            //ADD MAIN TEXT AND HIDDEN TEXT
            view.addView(mainTextView);
            view.addView(hiddenTextView);
        }
    }

    public JSONArray sortByNameAscendingJSONArray(JSONArray jsonArray, String type) {
        switch (type) {
            case "ascending": {
                int size = jsonArray.size();

                for (int i = 0; i < size - 1; i++) {
                    int min_idx = i;


                    for (int j = i + 1; j < size; j++) {
                        JSONObject elem1 = (JSONObject) jsonArray.get(min_idx);
                        String name1 = (String) elem1.get("name");
                        JSONObject elem2 = (JSONObject) jsonArray.get(j);
                        String name2 = (String) elem2.get("name");

                        assert name2 != null;
                        assert name1 != null;
                        if (name2.compareTo(name1) < 0) {
                            min_idx = j;
                        }
                    }
                    Object temp = jsonArray.get(min_idx);
                    jsonArray.set(min_idx, jsonArray.get(i));
                    jsonArray.set(i, temp);
                }

                return jsonArray;
            }
            case "descending": {
                JSONArray sortedArray = new JSONArray();
                int size = jsonArray.size();

                for (int i = 0; i < size - 1; i++) {
                    int min_idx = i;


                    for (int j = i + 1; j < size; j++) {
                        JSONObject elem1 = (JSONObject) jsonArray.get(min_idx);
                        String name1 = (String) elem1.get("name");
                        JSONObject elem2 = (JSONObject) jsonArray.get(j);
                        String name2 = (String) elem2.get("name");

                        assert name2 != null;
                        assert name1 != null;
                        if (name2.compareTo(name1) >= 1) {
                            min_idx = j;
                        }
                    }
                    Object temp = jsonArray.get(min_idx);
                    jsonArray.set(min_idx, jsonArray.get(i));
                    jsonArray.set(i, temp);
                }

                return jsonArray;
            }
            case "numericAscending": {
                int size = jsonArray.size();

                for (int i = 0; i < size - 1; i++) {
                    int min_idx = i;


                    for (int j = i + 1; j < size; j++) {
                        JSONObject elem1 = (JSONObject) jsonArray.get(min_idx);
                        Long num1 = (Long) elem1.get("number");
                        JSONObject elem2 = (JSONObject) jsonArray.get(j);
                        Long num2 = (Long) elem2.get("number");

                        assert num2 != null;
                        assert num1 != null;
                        if (num2.compareTo(num1) < 0) {
                            min_idx = j;
                        }
                    }
                    Object temp = jsonArray.get(min_idx);
                    jsonArray.set(min_idx, jsonArray.get(i));
                    jsonArray.set(i, temp);
                }

                return jsonArray;
            }
            case "numericDescending": {
                int size = jsonArray.size();

                for (int i = 0; i < size - 1; i++) {
                    int min_idx = i;


                    for (int j = i + 1; j < size; j++) {
                        JSONObject elem1 = (JSONObject) jsonArray.get(min_idx);
                        Long num1 = (Long) elem1.get("number");
                        JSONObject elem2 = (JSONObject) jsonArray.get(j);
                        Long num2 = (Long) elem2.get("number");

                        assert num2 != null;
                        assert num1 != null;
                        if (num2.compareTo(num1) >= 1) {
                            min_idx = j;
                        }
                    }
                    Object temp = jsonArray.get(min_idx);
                    jsonArray.set(min_idx, jsonArray.get(i));
                    jsonArray.set(i, temp);
                }

                return jsonArray;
            }
            case "symbolAscending": {
                int size = jsonArray.size();

                for (int i = 0; i < size - 1; i++) {
                    int min_idx = i;


                    for (int j = i + 1; j < size; j++) {
                        JSONObject elem1 = (JSONObject) jsonArray.get(min_idx);
                        String symbol = (String) elem1.get("symbol");
                        JSONObject elem2 = (JSONObject) jsonArray.get(j);
                        String symbol2 = (String) elem2.get("symbol");

                        assert symbol2 != null;
                        assert symbol != null;
                        if (symbol2.compareTo(symbol) < 0) {
                            min_idx = j;
                        }
                    }
                    Object temp = jsonArray.get(min_idx);
                    jsonArray.set(min_idx, jsonArray.get(i));
                    jsonArray.set(i, temp);

                }
                return jsonArray;
            }
        }
        return null;
    }

    public void sortAndRefresh(ViewGroup layout, String type, JSONArray jsonData){

        if (layout.getChildCount() > 0){
            layout.removeAllViews();
        }

        appendElementsAsViews(sortByNameAscendingJSONArray(jsonData, type), layout);
    }

    public void searchAndRefresh(ViewGroup layout, String search, JSONArray data) {

        TextView elemTitle = new TextView(this);
        TextView elemDescription = new TextView(this);
        TextView elemStats = new TextView(this);
        String searchUC = search.toUpperCase();

        Button backButton = new Button(this);
        backButton.setText(R.string.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortAndRefresh(mainLayout, "numericAscending", elementsJSON);
            }
        });
        backButton.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        backButton.setGravity(Gravity.CENTER);


        for (Object elem : data) {
                JSONObject element = (JSONObject) elem;
                String name = (String) element.get("name");
                String symbol = (String) element.get("symbol");
                String atomicNum = String.valueOf(element.get("number"));

                if (search.equals(" ")){
                    return;
                }

                else {
                    assert name != null;
                    assert symbol != null;
                    if (name.toUpperCase().compareTo(searchUC) == 0 || symbol.toUpperCase().compareTo(searchUC) == 0)
                    {
                        if (layout.getChildCount() > 0) {
                            layout.removeAllViews();
                        }

                        String atomicMass = String.valueOf(element.get("atomic_mass"));
                        String density = String.valueOf(element.get("density"));
                        String phase = (String) element.get("phase");
                        String summary = (String) element.get("summary");

                        String statsString = "\nSymbol: " + symbol +
                                "<br>Atomic Number: " + atomicNum +
                                "<br>Atomic Mass: " + atomicMass + " amu" +
                                "<br>Density: " + density + " g/cm<sup><small>3</small></sup>" +
                                "<br>Phase: " + phase;

                        elemTitle.setText(name);
                        elemTitle.setTextColor(Color.WHITE);
                        elemTitle.setTextSize(25);
                        elemTitle.setGravity(Gravity.CENTER);
                        elemTitle.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemTitle.setPadding(0, 20, 0, 50);

                        elemStats.setText(Html.fromHtml(statsString));
                        elemStats.setTextSize(20);
                        elemStats.setGravity(Gravity.CENTER);
                        elemStats.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemStats.setPadding(0, 25, 0, 25);

                        elemDescription.setText(summary);
                        elemDescription.setGravity(Gravity.CENTER);
                        elemDescription.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemDescription.setPadding(0, 25, 0, 20);


                        layout.addView(elemTitle);
                        layout.addView(elemStats);
                        layout.addView(elemDescription);
                        layout.addView(backButton);
                        return;

                    }
                    else if (atomicNum.equals(searchUC)) {
                        if (layout.getChildCount() > 0) {
                            layout.removeAllViews();
                        }

                        String atomicMass = String.valueOf(element.get("atomic_mass"));
                        String density = String.valueOf(element.get("density"));
                        String phase = (String) element.get("phase");
                        String summary = (String) element.get("summary");

                        String statsString = "\nSymbol: " + symbol +
                                "<br>Atomic Number: " + atomicNum +
                                "<br>Atomic Mass: " + atomicMass + " amu" +
                                "<br>Density: " + density + " g/cm<sup><small>3</small></sup>" +
                                "<br>Phase: " + phase;

                        elemTitle.setText(name);
                        elemTitle.setTextColor(Color.WHITE);
                        elemTitle.setTextSize(25);
                        elemTitle.setGravity(Gravity.CENTER);
                        elemTitle.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemTitle.setPadding(0, 20, 0, 50);

                        elemStats.setText(Html.fromHtml(statsString));
                        elemStats.setTextSize(20);
                        elemStats.setGravity(Gravity.CENTER);
                        elemStats.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemStats.setPadding(0, 25, 0, 25);

                        elemDescription.setText(summary);
                        elemDescription.setGravity(Gravity.CENTER);
                        elemDescription.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        elemDescription.setPadding(0, 25, 0, 20);


                        layout.addView(elemTitle);
                        layout.addView(elemStats);
                        layout.addView(elemDescription);
                        layout.addView(backButton);
                        return;
                    }
                }
            }

        //IF NOTHING IS FOUND
        elemTitle.setText(R.string.searchFail1);
        elemTitle.setTextColor(Color.WHITE);
        elemTitle.setTextSize(25);
        elemTitle.setGravity(Gravity.CENTER);
        elemTitle.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        elemTitle.setPadding(0, 20, 0, 50);

        if (layout.getChildCount() > 0) {
            layout.removeAllViews();
        }
        layout.addView(elemTitle);
        layout.addView(backButton);

    }

    public void openCalc(MenuItem item) {
        Intent intent = new Intent(this, Calculator.class);
        intent.putExtra("json", elementsJSON.toJSONString());
        startActivity(intent);
    }
}
