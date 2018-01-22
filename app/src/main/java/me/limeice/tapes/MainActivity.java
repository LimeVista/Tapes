package me.limeice.tapes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.limeice.tapesdb.Tapes;

public class MainActivity extends AppCompatActivity {

    private TextView mText;

    private EditText mEdit;

    private static final String KEY = "sample_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tapes.init(this);
        register();
    }

    private void register() {
        mText = findViewById(R.id.main_act_text);
        mEdit = findViewById(R.id.main_act_edit);
        findViewById(R.id.main_act_write_btn).setOnClickListener(v ->
                Tapes.track().write(KEY, mEdit.getText().toString())
        );
        findViewById(R.id.main_act_read_btn).setOnClickListener(v ->
                mText.setText(Tapes.track().read(KEY))
        );
        findViewById(R.id.main_act_clear_btn).setOnClickListener(v -> {
            Tapes.track().clear(KEY);
            boolean exist = Tapes.track().exist(KEY);
            if (!exist)
                showToast("Successful.");
            else
                showToast("failed.");
        });
    }

    private void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
