package com.codepath.simpletodo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;


public class EditItemActivity extends ActionBarActivity {

    EditText editTextField;
    public ArrayAdapter<String> itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        String username = getIntent().getStringExtra("itemName");

        editTextField = (EditText) findViewById(R.id.editText2);
        editTextField.setText(username, TextView.BufferType.EDITABLE);
        editTextField.setSelection(editTextField.getText().length());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickItem(View view) {

        // closes the activity and returns to first screen
        EditText etName = (EditText) findViewById(R.id.editText2);
        Intent data = new Intent();
        data.putExtra("itemName", etName.getText().toString());
        setResult(RESULT_OK, data);
        this.finish();

    }


}
