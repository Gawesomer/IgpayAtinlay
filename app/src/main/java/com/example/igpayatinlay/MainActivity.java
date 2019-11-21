package com.example.igpayatinlay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInputBox();
    }

    private void initInputBox() {
        EditText inputBox = findViewById(R.id.inputTextBox);
        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setOutputBox(s.toString());
            }
        });
    }

    private void setOutputBox(String input) {
        TextView outputBox = findViewById(R.id.outputTextBox);
        outputBox.setText(input);
    }
}
