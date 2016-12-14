package com.ar.htw.ar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    public Button encodeButton;
    public Button decodeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerButtonEncode();
        addListenerButtonDecode();
    }
    public void addListenerButtonEncode ()
    {
        encodeButton = (Button)findViewById(R.id.encodeButton);
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EncodeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void addListenerButtonDecode ()
    {
        decodeButton = (Button)findViewById(R.id.decodeButton);
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DecodeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
