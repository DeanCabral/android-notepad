package com.cryotech.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HomeActivity extends AppCompatActivity implements NoteAdapter.RecyclerViewClickListener{

    private EditText searchView;
    private TextView notesCount;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ArrayList<Note> notesList;
    private ArrayAdapter<String> arrayAdapter;
    private AlertDialog.Builder builderInner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notesCount = findViewById(R.id.notes_display);
        searchView = findViewById(R.id.search_view);
        notesCount = findViewById(R.id.notes_display);
        recyclerView = findViewById(R.id.recycler_view);

        notesList = new ArrayList<>();
        adapter = new NoteAdapter(notesList, this, notesCount, this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setNestedScrollingEnabled(false);

        initSearchBar();
        initSortFilter();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSharedPrefs();
        initLoadNotes();
    }

    public void initSharedPrefs()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Preferences.AUTO_SAVE = preferences.getBoolean("autoSave", true);
        Preferences.ALPHA_SORT = preferences.getBoolean("alphaSort", false);
        Preferences.UPDATE_TIME = preferences.getBoolean("updateTime", false);
    }

    private void initLoadNotes()
    {
        recyclerView.setAdapter(null);
        searchView.getText().clear();

        notesList.clear();
        notesList.addAll(Utilities.getAllSavedNotes(this));

        if (Preferences.ALPHA_SORT) sortNotesByAlpha();
        else sortNotesByDate();
        notesCount.setText("Displaying " + notesList.size() + " Note(s)");

        if(notesList != null && notesList.size() > 0) {
            adapter = new NoteAdapter(notesList, this, notesCount, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void sortNotesByAlpha()
    {
        Collections.sort(notesList, new Comparator<Note>() {
            public int compare(Note s1, Note s2) {
                return s1.getTitle().compareTo(s2.getTitle());
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void sortNotesByDate()
    {
        Collections.sort(notesList, new Comparator<Note>() {
            public int compare(Note o1, Note o2) {
                return o2.getDateTimeFormat().compareTo(o1.getDateTimeFormat());
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void initSearchBar()
    {
        searchView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                adapter.getFilter().filter(cs.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {


            }
        });

    }

    private void initSortFilter()
    {
        builderInner = new android.support.v7.app.AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.library_context_menu, R.id.textContext);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(HomeActivity.this, NoteEditorActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
        }
        return true;
    }

    public void onClickSort(View v)
    {

        builderInner.setTitle("Filter Notes");

        arrayAdapter.clear();
        arrayAdapter.add("By Title");
        arrayAdapter.add("By Date");
        arrayAdapter.add("By Time");

        builderInner.setNegativeButton(
                "Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderInner.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String strName = arrayAdapter.getItem(which);

                        if (strName == "By Title")
                        {
                            searchView.setHint("Search Titles (Case Sensitive)");
                            adapter.filterPref = 0;
                        }
                        else if (strName == "By Date")
                        {
                            searchView.setHint("Search Dates (DD/MM/YYYY)");
                            adapter.filterPref = 1;
                        }
                        else if (strName == "By Time")
                        {
                            searchView.setHint("Search Times (HH:MM)");
                            adapter.filterPref = 2;
                        }

                    }
                });
        builderInner.show();

    }

    @Override
    public void ItemClicked(int position) {

        if (notesList.get(position).getLock())
        {
            createUnlockDialog(position, 0);

        } else {
            openNote(position);
        }

    }

    @Override
    public void ItemLongClicked(int position)
    {
        createMenu(position);
    }

    private void openNote(int position)
    {
        notesList = adapter.getList();
        adapter.notifyDataSetChanged();

        String fileName = notesList.get(position).getDateTime() + Utilities.FILE_EXTENSION;
        String lockCode = notesList.get(position).getCode();
        boolean lockState = notesList.get(position).getLock();
        Intent viewNoteIntent = new Intent(getApplicationContext(), NoteEditorActivity.class);
        viewNoteIntent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, fileName);
        viewNoteIntent.putExtra(Utilities.EXTRAS_LOCK_STATE, lockState);
        viewNoteIntent.putExtra(Utilities.EXTRAS_LOCK_CODE, lockCode);
        startActivity(viewNoteIntent);
    }

    private void refreshList(int pos)
    {
        long noteDateTime = notesList.get(pos).getDateTime();
        String noteTitle = notesList.get(pos).getTitle();
        String noteContent = notesList.get(pos).getContent();
        boolean mLock = notesList.get(pos).getLock();
        String mCode = notesList.get(pos).getCode();

        Note note = new Note(noteDateTime, noteTitle, noteContent, mLock, mCode);
        Utilities.saveNote(this, note);

        initLoadNotes();
    }

    private void createMenu(final int pos)
    {
        notesList = adapter.getList();
        adapter.notifyDataSetChanged();

        builderInner.setTitle("Select Option");

        if (notesList.get(pos).getLock())
        {
            arrayAdapter.clear();
            arrayAdapter.add("Edit");
            arrayAdapter.add("Delete");
            arrayAdapter.add("Unlock Note");
        } else {
            arrayAdapter.clear();
            arrayAdapter.add("Edit");
            arrayAdapter.add("Delete");
            arrayAdapter.add("Lock Note");
        }

        builderInner.setNegativeButton(
                "Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderInner.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String strName = arrayAdapter.getItem(which);

                        if (strName == "Edit")
                        {
                            if (notesList.get(pos).getLock()) createUnlockDialog(pos, 0);
                            else openNote(pos);

                        }
                        else if (strName == "Delete")
                        {
                            createDeleteDialog(pos);
                        }
                        else if (strName == "Lock Note")
                        {
                            createSetLockDialog(pos);
                        }
                        else if (strName == "Unlock Note")
                        {
                            createRemoveLockDialog(pos);
                        }


                    }
                });
        builderInner.show();

    }

    private void createDeleteDialog(final int pos)
    {
        android.app.AlertDialog.Builder builderInner = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.MyAlertDialogStyle);
        builderInner.setTitle("Notepad");
        builderInner.setMessage("Are you sure you want to delete this note?");
        builderInner.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        String fileName = notesList.get(pos).getDateTime() + Utilities.FILE_EXTENSION;
                        Note mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), fileName);

                        Utilities.deleteFile(getApplicationContext(), mLoadedNote.getDateTime() + Utilities.FILE_EXTENSION);
                        initLoadNotes();
                    }
                });
        builderInner.setNegativeButton("Cancel", null);
        builderInner.show();
    }

    private void createSetLockDialog(final int pos)
    {
        final android.app.AlertDialog.Builder builderInner = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.MyAlertDialogStyle);
        final android.app.AlertDialog alert = builderInner.create();

        LayoutInflater li = LayoutInflater.from(this);
        View renameView = li.inflate(R.layout.set_pass_dialog, null);

        final EditText passInput = renameView.findViewById(R.id.setPassInput);
        final Button btnSet = renameView.findViewById(R.id.btnSet);
        final Button btnCancel = renameView.findViewById(R.id.btnCancel);

        btnSet.setText("Lock");
        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.getBackground().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.SRC_ATOP);

        btnSet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String passwordInput;

                passwordInput = passInput.getText().toString();

                if (!passwordInput.matches(""))
                {
                    notesList.get(pos).setLock(true);
                    notesList.get(pos).setCode(passwordInput);
                    alert.dismiss();
                    refreshList(pos);
                    Toast.makeText(HomeActivity.this, "Lock Set", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(HomeActivity.this, "Empty Field", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alert.dismiss();

            }
        });

        alert.setView(renameView);
        alert.show();
    }

    private void createRemoveLockDialog(final int pos)
    {
        final android.app.AlertDialog.Builder builderInner = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.MyAlertDialogStyle);
        final android.app.AlertDialog alert = builderInner.create();

        LayoutInflater li = LayoutInflater.from(this);
        View renameView = li.inflate(R.layout.lock_pass_dialog, null);

        final EditText passInput = renameView.findViewById(R.id.passInput);
        final Button btnAccess = renameView.findViewById(R.id.btnAccess);
        final Button btnCancel = renameView.findViewById(R.id.btnCancel);

        btnAccess.setText("Unlock");
        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.getBackground().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.SRC_ATOP);

        btnAccess.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String passwordInput;
                String checkPass;

                passwordInput = passInput.getText().toString();
                checkPass = notesList.get(pos).getCode();

                if (!passwordInput.matches("")) {
                    if (passwordInput.matches(checkPass)) {
                        notesList.get(pos).setLock(false);
                        notesList.get(pos).setCode(null);
                        alert.dismiss();
                        refreshList(pos);
                        Toast.makeText(HomeActivity.this, "Lock Removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Incorrect password credentials", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Empty Field", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alert.dismiss();

            }
        });

        alert.setView(renameView);
        alert.show();
    }

    private void createUnlockDialog(final int pos, final int type)
    {
        final android.app.AlertDialog.Builder builderInner = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.MyAlertDialogStyle);
        final android.app.AlertDialog alert = builderInner.create();

        LayoutInflater li = LayoutInflater.from(this);
        View renameView = li.inflate(R.layout.lock_pass_dialog, null);

        final EditText passInput = renameView.findViewById(R.id.passInput);
        final Button btnAccess = renameView.findViewById(R.id.btnAccess);
        final Button btnCancel = renameView.findViewById(R.id.btnCancel);

        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.getBackground().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.SRC_ATOP);

        btnAccess.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String passwordInput;
                String checkPass;

                passwordInput = passInput.getText().toString();
                checkPass = notesList.get(pos).getCode();

                if (!passwordInput.matches("")) {
                    if (passwordInput.matches(checkPass)) {
                        alert.dismiss();
                        if (type == 0) openNote(pos);
                        else if (type == 1) createDeleteDialog(pos);
                        Toast.makeText(HomeActivity.this, "Password Accepted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Incorrect password credentials", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Empty Field", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alert.dismiss();

            }
        });

        alert.setView(renameView);
        alert.show();

    }
}
