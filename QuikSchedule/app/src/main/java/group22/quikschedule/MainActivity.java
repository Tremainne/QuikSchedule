package group22.quikschedule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toWebreg(View view){
        setContentView(R.layout.activity_webreg);

        Button btn = (Button) findViewById(R.id.submitWebreg);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieve(v);
            }
        });
    }

    public void retrieve(View view){
        EditText etPID = (EditText) findViewById(R.id.enterPID);
        String pid = etPID.getText().toString();

        EditText etPass = (EditText) findViewById(R.id.enterPassword);
        String pass = etPID.getText().toString();

        new WebregScraping().execute(pid, pass);
    }
}
