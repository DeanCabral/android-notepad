package com.cryotech.notepad;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrefsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Preference licence = findPreference("licences");

            /*licence.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(android.preference.Preference preference) {

                    String licence = "Copyright 2017 Dean Cabral\n" +
                            "\n" +
                            "Licensed under the Apache License, Version 2.0 (the \"License\"); " +
                            "you may not use this file except in compliance with the License. You may obtain a copy of the License at\n" +
                            "\n" +
                            "http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS," +
                            " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. " +
                            "See the License for the specific language governing permissions and limitations under the License.";

                    final android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                    builderSingle.setTitle("Licences");
                    builderSingle.setMessage(licence);
                    builderSingle.show();
                   return true;
               }
            });*/
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals("autoSave")) {
                boolean state = sharedPreferences.getBoolean("autoSave", false);

                if (state) {
                    Preferences.AUTO_SAVE = true;
                } else {
                    Preferences.AUTO_SAVE = false;
                }
            }

            if (key.equals("alphaSort")) {
                boolean state = sharedPreferences.getBoolean("autoSave", false);

                if (state) {
                    Preferences.ALPHA_SORT = true;
                } else {
                    Preferences.ALPHA_SORT = false;
                }
            }

            if (key.equals("updateTime")) {
                boolean state = sharedPreferences.getBoolean("updateTime", false);

                if (state) {
                    Preferences.UPDATE_TIME = true;
                } else {
                    Preferences.UPDATE_TIME = false;
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
