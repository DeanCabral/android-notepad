package com.cryotech.notepad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class NoteEditorActivity extends AppCompatActivity {

    private EditText mTitle;
    private EditText mContent;
    private String mFileName;
    private boolean mLock;
    private String mCode;
    private Note mLoadedNote = null;
    private boolean mSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Note Editor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle = findViewById(R.id.edit_text_title);
        mContent = findViewById(R.id.edit_text_content);

        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        mLock = getIntent().getBooleanExtra(Utilities.EXTRAS_LOCK_STATE, false);
        mCode = getIntent().getStringExtra(Utilities.EXTRAS_LOCK_CODE);
        mSaved = false;

        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {

                mTitle.setText(mLoadedNote.getTitle());
                mContent.setText(mLoadedNote.getContent());
                saveNote(true);
            }
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSaved = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Preferences.AUTO_SAVE && !mSaved) saveNote(true);
        closeNote();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (Preferences.AUTO_SAVE && !mSaved) saveNote(true);
                closeNote();
                break;
            case R.id.action_save:
                saveNote(false);
                break;
            case R.id.action_delete:
                deleteNote(false);
                break;
        }
        return true;
    }

    private void saveNote(boolean auto)
    {
        mSaved = true;
        Note note;
        String noteTitle = mTitle.getText().toString();
        String noteContent = mContent.getText().toString();

        if (mLoadedNote == null)
        {
            note = new Note(System.currentTimeMillis(), noteTitle, noteContent, mLock, mCode);
        }
        else
        {
            if (Preferences.UPDATE_TIME) {
                deleteNote(true);
                mLoadedNote.setDateTime(System.currentTimeMillis());
            }
            note = new Note(mLoadedNote.getDateTime(), noteTitle, noteContent, mLock, mCode);
        }

        if (!noteTitle.matches(""))
        {
            if (Utilities.saveNote(this, note))
            {
                if (!auto) Toast.makeText(this, "Note Saved", Toast.LENGTH_LONG).show();
            }
            else
            {
                if (!auto) Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (!auto) Toast.makeText(this, "Missing Title", Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteNote(boolean auto)
    {
        if (mLoadedNote == null)
        {
            closeNote();
        }
        else
        {

            if (!auto)
            {
                AlertDialog.Builder builderInner = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                builderInner.setTitle("Notepad");
                builderInner.setMessage("Are you sure you want to delete this note?");
                builderInner.setPositiveButton(
                        "Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                Toast.makeText(NoteEditorActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                Utilities.deleteFile(getApplicationContext(), mLoadedNote.getDateTime() + Utilities.FILE_EXTENSION);
                                closeNote();

                            }
                        });
                builderInner.setNegativeButton("Cancel", null);
                builderInner.show();
            } else {

                Utilities.deleteFile(getApplicationContext(), mLoadedNote.getDateTime() + Utilities.FILE_EXTENSION);
            }

        }
    }

    private void closeNote()
    {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        finish();
    }
}
