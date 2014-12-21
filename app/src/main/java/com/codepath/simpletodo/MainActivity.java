package com.codepath.simpletodo;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private class TodoItem {
        private int id;
        private String body;
        private int priority;

        public TodoItem(String body, int priority) {
            super();
            this.body = body;
            this.priority = priority;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    private class TodoItemDatabase extends SQLiteOpenHelper {

        // All Static variables
        // Database Version
        private static final int DATABASE_VERSION = 3;

        // Database Name
        private static final String DATABASE_NAME = "todoListDatabase";

        // Todo table name
        private static final String TABLE_TODO = "todo_items";

        // Todo Table Columns names
        private static final String KEY_ID = "id";
        private static final String KEY_BODY = "body";
        //private static final String KEY_PRIORITY = "priority";

        public TodoItemDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        // Creating our initial tables
        // These is where we need to write create table statements.
        // This is called when database is created.
        @Override
         public void onCreate(SQLiteDatabase db) {
            // Construct a table for todo items

            String CREATE_TODO_TABLE = "CREATE TABLE " + TABLE_TODO + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BODY + " TEXT," + ")";
                    //+ KEY_PRIORITY + " INTEGER" + ")";
            db.execSQL(CREATE_TODO_TABLE);
         }

        // Upgrading the database between versions
        // This method is called when database is upgraded like modifying the table structure,
        // adding constraints to database, etc
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion == 1) {
                // Wipe older tables if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
                // Create tables again
                onCreate(db);
            }
        }

        // Insert record into the database
        public void addTodoItem(TodoItem item) {
            // Open database connection
            SQLiteDatabase db = this.getWritableDatabase();
            // Define values for each field
            ContentValues values = new ContentValues();
            values.put(KEY_BODY, item.getBody());
            //values.put(KEY_PRIORITY, item.getPriority());
            // Insert Row
            db.insert(TABLE_TODO, null, values);
            db.close(); // Closing database connection
        }

        // Returns a single todo item by id
        public TodoItem getTodoItem(int id) {
            // Open database for reading
            SQLiteDatabase db = this.getReadableDatabase();
            // Construct and execute query
            Cursor cursor = db.query(TABLE_TODO,  // TABLE
                    new String[] { KEY_ID, KEY_BODY}, //KEY_PRIORITY }, // SELECT
                    KEY_ID + "= ?", new String[] { String.valueOf(id) },  // WHERE, ARGS
                    null, null, null, null); // GROUP BY, HAVING, ORDER BY
            if (cursor != null)
                cursor.moveToFirst();
            // Load result into model object
            TodoItem item = new TodoItem(cursor.getString(1), cursor.getInt(2));
            item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
            // return todo item
            return item;
        }

        public List<TodoItem> getAllTodoItems() {
            List<TodoItem> todoItems = new ArrayList<TodoItem>();
            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_TODO;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    TodoItem item = new TodoItem(cursor.getString(1), cursor.getInt(2));
                    item.setId(cursor.getInt(0));
                    // Adding todo item to list
                    todoItems.add(item);
                } while (cursor.moveToNext());
            }

            // return todo list
            return todoItems;
        }

        public int getTodoItemCount() {
            String countQuery = "SELECT  * FROM " + TABLE_TODO;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            cursor.close();
            // return count
            return cursor.getCount();
        }

        public int updateTodoItem(TodoItem item) {
            // Open database for writing
            SQLiteDatabase db = this.getWritableDatabase();
            // Setup fields to update
            ContentValues values = new ContentValues();
            values.put(KEY_BODY, item.getBody());
            //values.put(KEY_ID,item.getId());
            //values.put(KEY_PRIORITY, item.getPriority());
            // Updating row
            int result = db.update(TABLE_TODO, values, KEY_ID + " = ?",
                    new String[] { String.valueOf(item.getId()) });
            // Close the database
            db.close();
            return result;
        }

        public void deleteTodoItem(TodoItem item) {
            // Open database for writing
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete the record with the specified id
            db.delete( TABLE_TODO, KEY_ID + " = ?",
                       new String[] { String.valueOf(item.getId()) });
            // Close the database
            db.close();
        }

        public void deleteAll() {

            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_TODO,null,null);
            db.close();
        }

    }

    public TodoItem todoitem;
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;
    EditText etNewItem;
    private final int REQUEST_CODE = 20;
    public int edit_position;
    public int current_count = 0;

    private TodoItemDatabase db;

    SQLiteDatabase db1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvItems = (ListView) findViewById(R.id.listView);
        items = new ArrayList<String>();
        itemsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        lvItems.setAdapter(itemsAdapter);

        db = new TodoItemDatabase(this);
        readItems();
        //db.deleteAll();
        setupListViewListener();
    }

    private void readItems() {

       TodoItemDatabase db = new TodoItemDatabase(this);
       List<TodoItem> item = db.getAllTodoItems();
        // Print out properties
        for (TodoItem ti : item) {
            items.add(ti.getBody());
       }

       return;
    }

    private  void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        // Delete from Database
                        delete_from_db(position);

                        items.remove(position);

                        itemsAdapter.notifyDataSetChanged();
                        return true;
                    }
                }
        );

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                String selectedFromList =(lvItems.getItemAtPosition(position).toString());
                edit_position = position;
                i.putExtra("itemName",selectedFromList);
                startActivityForResult(i, REQUEST_CODE);

            }
        });
    }

    private void delete_from_db(int position) {

        position++;

        TodoItem todoitem = new TodoItem("",position);
        todoitem.setId(position);
        todoitem.setBody("");
        db.deleteTodoItem(todoitem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {

        etNewItem = (EditText) findViewById(R.id.editText);
        String itemText = etNewItem.getText().toString();
        itemsAdapter.add(itemText);
        etNewItem.setText("");

        // Add item Database
        int count = 0;
        List<TodoItem> items = db.getAllTodoItems();
        // Print out properties
        for (TodoItem ti : items) {
            String log = "Id: " + ti.getId() + " , Body: " + ti.getBody() +
                    " , Priority: " + ti.getPriority();
            // Writing Todo Items to log
            Log.d("Name: ", log);
            count++;
        }

        db.addTodoItem(new TodoItem(itemText, count));

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            String name = data.getExtras().getString("itemName");
            // Toast the name to display temporarily on screen
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
            items.set(edit_position,name);

            //edit_position++;

            List<TodoItem> items = db.getAllTodoItems();
            TodoItem new_item;

            edit_position++;

            // Print out properties
            for (TodoItem ti : items) {
                if(ti.id == edit_position)
                    db.updateTodoItem(ti);
            }
            //TodoItem todoitem = new TodoItem(name,edit_position);
            //todoitem.setId(edit_position);
            //todoitem.setPriority(edit_position);
            //todoitem.setBody(name);

            //TodoItemDatabase todoItemDatabase = new TodoItemDatabase(this);
            //db.updateTodoItem(todoitem);

            itemsAdapter.notifyDataSetChanged();
        }
    }

}
