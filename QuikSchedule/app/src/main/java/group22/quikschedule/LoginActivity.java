package group22.quikschedule;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatButton bSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bSignIn = (AppCompatButton) findViewById(R.id.btn_signin);
        bSignIn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.btn_signin:
                Intent i = new Intent(this, NavigationDrawerActivity.class);
                startActivity(i);
                break;

        }
    }
}
