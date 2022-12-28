package com.example.podstawka;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_ITEM = 1;

    private ListView listView;
    private EditText newItemEditText;
    private Button addButton;
    private Button confirmButton;
    private  ArrayList<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static Set<String> selectedItems = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        newItemEditText = findViewById(R.id.newItemEditText);
        addButton = findViewById(R.id.addButton);
        confirmButton = findViewById(R.id.confirmButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Dodajemy elementy do listy
        items.add("997");
        items.add("112");
        items.add("532");
        List<String> nonRemovableItems = Arrays.asList("112");
        SharedPreferences sharedPref = getSharedPreferences("items", MODE_PRIVATE);
        Set<String> selectedItems = sharedPref.getStringSet("items", new HashSet<String>());

        for (int i = 0; i < items.size(); i++) {
            if (selectedItems.contains(items.get(i))) {
                // jeśli element znajduje się w zbiorze, to zaznacz go w ListView
                listView.setItemChecked(i, true);
            }
        }

        Set<String> selectedItems2 = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            if (listView.isItemChecked(i)) {
                selectedItems.add(items.get(i));
            }
        }

// zapisz zbior do SharedPreferences
        SharedPreferences sharedPref2 = getSharedPreferences("selected_items", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("items", selectedItems);
        editor.apply();

        if (items != null) {
            // Pętla po elementach listy
            Iterator<String> iterator = items.iterator();
            while (iterator.hasNext()) {
                String element = iterator.next();
                System.out.println(element);
            }
        }




        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newItem = newItemEditText.getText().toString();
                if (TextUtils.isEmpty(newItem)) {
                    Toast.makeText(ListActivity.this, "Wprowadź tekst", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sprawdź czy nowy element składa się tylko z cyfr
                boolean isNumber = true;
                for (int i = 0; i < newItem.length(); i++) {
                    if (!Character.isDigit(newItem.charAt(i))) {
                        isNumber = false;
                        break;
                    }
                }

                if (isNumber) {
                    items.add(newItem);
                    adapter.notifyDataSetChanged();
                    newItemEditText.setText("");
                } else {
                    Toast.makeText(ListActivity.this, "Nowy element musi składać się tylko z cyfr", Toast.LENGTH_SHORT).show();
                }
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItems.clear();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                for (int i = 0; i < checked.size(); i++) {
                    if (checked.valueAt(i)) {
                        selectedItems.add(items.get(checked.keyAt(i)));
                        //  Toast.makeText(ListActivity.this, "element selec"+selectedItems, Toast.LENGTH_SHORT).show();

                    }
                }

                if (selectedItems.isEmpty()) {
                    Toast.makeText(ListActivity.this, "Nie zaznaczono żadnych elementów", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedItems != null && !selectedItems.isEmpty()) {
                        Iterator<String> iterator = selectedItems.iterator();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("items", new ArrayList<>(selectedItems));
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }}
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Pobierz element do usunięcia

                String item = adapter.getItem(position);
                if (!nonRemovableItems.contains(item)) {
                    // Usun element z listy
                    items.remove(item);
                    adapter.notifyDataSetChanged();}
                else {  // Jeśli należy, to wyświetlamy komunikat o błędzie
                    Toast.makeText(ListActivity.this, "Nie mozna usunąć tego elementu", Toast.LENGTH_SHORT).show();

                }
                return true;
            }
        });
    /*
// Sprawdzamy, czy element, który chcemy usunąć, nie należy do tej listy
    if (!nonRemovableItems.contains(itemToRemove)) {
        // Jeśli nie należy, to możemy go usunąć
        items.remove(itemToRemove);
    } else {
        // Jeśli należy, to wyświetlamy komunikat o błędzie
        Toast.makeText(this, "Nie można usunąć tego elementu", Toast.LENGTH_SHORT).show();
    }*/
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Wywołaj metodę z klasy bazowej
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_ITEM && resultCode == RESULT_OK) {
            items = data.getStringArrayListExtra("items");
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Toast.makeText(ListActivity.this, "Lista została zaktualizowana", Toast.LENGTH_SHORT).show();
        }
    }

    public static List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }
}