package exercise.find.roots;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView success_text_view = findViewById(R.id.SuccessBanner);
        TextView root1_text = findViewById(R.id.root1);
        TextView root2_text = findViewById(R.id.root2);
        TextView time_elapsed_text = findViewById(R.id.timeText);


        Intent intent = getIntent();
        long original_number = intent.getLongExtra("original_number", -1);
        long root1 = intent.getLongExtra("root1", -1);
        long root2 = intent.getLongExtra("root2", -1);
        float time_in_seconds = intent.getFloatExtra("time_in_seconds", -1f);

        success_text_view.setText(success_text_view.getText() + "\nnumber is: " + Long.toString(original_number));
        root1_text.setText(root1_text.getText() + Long.toString(root1));
        root2_text.setText(root2_text.getText() + Long.toString(root2));
        time_elapsed_text.setText(time_elapsed_text.getText() + Float.toString(time_in_seconds) + " Seconds");
    }


}