package exercise.find.roots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiverForSuccess = null;
    // add any other fields to the activity as you want
    private BroadcastReceiver broadcastReceiverForTimeout = null;
    private boolean busy_with_calculation = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ProgressBar progressBar = findViewById(R.id.progressBar);
        EditText editTextUserInput = findViewById(R.id.editTextInputNumber);
        Button buttonCalculateRoots = findViewById(R.id.buttonCalculateRoots);

        // set initial UI:
        progressBar.setVisibility(View.GONE); // hide progress
        editTextUserInput.setText(""); // cleanup text in edit-text
        editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
        buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)

        // set listener on the input written by the keyboard to the edit-text
        editTextUserInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                // text did change
                String newText = editTextUserInput.getText().toString();
                if (isNumeric(newText) && !busy_with_calculation) {
                    buttonCalculateRoots.setEnabled(true); // set button as enabled
                }
                else{
                    buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)
                }
            }
        });

        // set click-listener to the button
        buttonCalculateRoots.setOnClickListener(v -> {
            Intent intentToOpenService = new Intent(MainActivity.this, CalculateRootsService.class);
            String userInputString = editTextUserInput.getText().toString();
            // check that `userInputString` is a number. handle bad input. convert `userInputString` to long
            if (isNumeric(userInputString)) {
                long userInputLong = Long.parseLong(userInputString);
                intentToOpenService.putExtra("number_for_service", userInputLong);
                startService(intentToOpenService);
                // set vars
                busy_with_calculation = true;
                // set UI:
                progressBar.setVisibility(View.VISIBLE); // hide progress
//              editTextUserInput.setText(""); // cleanup text in edit-text
                editTextUserInput.setEnabled(false); // set edit-text as disabled (user can input text)
                buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)
            }
        });

        // register a broadcast-receiver to handle action "found_roots"
        broadcastReceiverForSuccess = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent incomingIntent) {
                if (incomingIntent == null || !incomingIntent.getAction().equals("found_roots"))
                    return;
                // success finding roots!

                // change vars
                busy_with_calculation = false;
                // change the UI
                progressBar.setVisibility(View.GONE); // hide progress
                editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
                buttonCalculateRoots.setEnabled(true); // (user can click)

                // open result activity
                openSuccessActivity(incomingIntent.getLongExtra("original_number", -1),
                        incomingIntent.getLongExtra("root1", -1),
                        incomingIntent.getLongExtra("root2", -1),
                        incomingIntent.getFloatExtra("time_till_success", -1f));
            }
        };
        this.registerReceiver(broadcastReceiverForSuccess, new IntentFilter("found_roots"));

        // register a broadcast-receiver to handle action "stopped_calculations"
        broadcastReceiverForTimeout = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent incomingIntent) {
                if (incomingIntent == null || !incomingIntent.getAction().equals("stopped_calculations"))
                    return;
                // failed finding roots (timeout)
                // change vars
                busy_with_calculation = false;
                Toast.makeText(MainActivity.this,
                        "calculation aborted after "+incomingIntent.getFloatExtra("time_until_give_up_seconds", -1)+" seconds",
                        Toast.LENGTH_SHORT).show();
                // change ui elements
                progressBar.setVisibility(View.GONE); // hide progress
                editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
                buttonCalculateRoots.setEnabled(true); // (user can click)

            }
        };
        registerReceiver(broadcastReceiverForTimeout, new IntentFilter("stopped_calculations"));


    }

    private void openSuccessActivity(long original_num, long root1, long root2, float time){
        Intent intent  = new Intent(this, ResultActivity.class);
        intent.putExtra("original_number", original_num);
        intent.putExtra("root1", root1);
        intent.putExtra("root2", root2);
        intent.putExtra("time_in_seconds", time);
        startActivity(intent);
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long l = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove ALL broadcast receivers we registered earlier in onCreate().
        //  to remove a registered receiver, call method `this.unregisterReceiver(<receiver-to-remove>)`
        this.unregisterReceiver(broadcastReceiverForSuccess);
        this.unregisterReceiver(broadcastReceiverForTimeout);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // put relevant data into bundle as you see fit
        // save UI
        primes_calc_state state = new primes_calc_state();
        state.button_enabled = findViewById(R.id.buttonCalculateRoots).isEnabled();
        state.editTextUserInput_enabled = findViewById(R.id.editTextInputNumber).isEnabled();
        state.editTextUserInput_content = ((TextView) findViewById(R.id.editTextInputNumber)).getText().toString();
        state.progressBar_visible = findViewById(R.id.progressBar).getVisibility();
        // set initial UI:
        outState.putSerializable("primes_calc_state", state);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // load data from bundle and set screen state (see spec below)
        Serializable prev_state = savedInstanceState.getSerializable("primes_calc_state");
        if (!(prev_state instanceof primes_calc_state)) {
            return; // ignore
        }
        primes_calc_state casted = (primes_calc_state) prev_state;

        ProgressBar progressBar = findViewById(R.id.progressBar);
        EditText editTextUserInput = findViewById(R.id.editTextInputNumber);
        Button buttonCalculateRoots = findViewById(R.id.buttonCalculateRoots);
        // set to prev state
        progressBar.setVisibility(casted.progressBar_visible);
        editTextUserInput.setEnabled(casted.editTextUserInput_enabled);
        editTextUserInput.setText(casted.editTextUserInput_content);
        buttonCalculateRoots.setEnabled(casted.button_enabled);
    }

    private static class primes_calc_state implements Serializable {
        // just save history
        int progressBar_visible;
        boolean editTextUserInput_enabled;
        String editTextUserInput_content;
        boolean button_enabled;
    }
}



/*
the spec is:

upon launch, Activity starts out "clean":
* progress-bar is hidden
* "input" edit-text has no input and it is enabled
* "calculate roots" button is disabled




the button behavior is:
* when there is no valid-number as an input in the edit-text, button is disabled
* when we triggered a calculation and still didn't get any result, button is disabled
* otherwise (valid number && not calculating anything in the BG), button is enabled




the edit-text behavior is:
* when there is a calculation in the BG, edit-text is disabled (user can't input anything)
* otherwise (not calculating anything in the BG), edit-text is enabled (user can tap to open the keyboard and add input)




the progress behavior is:
* when there is a calculation in the BG, progress is showing
* otherwise (not calculating anything in the BG), progress is hidden




when "calculate roots" button is clicked:
* change states for the progress, edit-text and button as needed, so user can't interact with the screen




when calculation is complete successfully:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* open a new "success" screen showing the following data:
  - the original input number
  - 2 roots combining this number (e.g. if the input was 99 then you can show "99=9*11" or "99=3*33"
  - calculation time in seconds




when calculation is aborted as it took too much time:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* show a toast "calculation aborted after X seconds"






upon screen rotation (saveState && loadState) the new screen should show exactly the same state as the old screen. this means:
* edit-text shows the same input
* edit-text is disabled/enabled based on current "is waiting for calculation?" state
* progress is showing/hidden based on current "is waiting for calculation?" state
* button is enabled/disabled based on current "is waiting for calculation?" state && there is a valid number in the edit-text input


 */