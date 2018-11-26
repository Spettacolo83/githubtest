package it.stefanorussello.githubtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    AutoCompleteTextView txtUsername;
    EditText txtPassword;
    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = findViewById(R.id.username);
        txtPassword = findViewById(R.id.password);
        btnSignIn = findViewById(R.id.sign_in_button);

        String strUsername = getIntent().getStringExtra("username");
        if (strUsername != null) {
            txtUsername.setText(strUsername);
        }
        String strPassword = getIntent().getStringExtra("password");
        if (strPassword != null) {
            txtPassword.setText(strPassword);
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("username", txtUsername.getText().toString());
                returnIntent.putExtra("password", txtPassword.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_item:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("username", "");
                returnIntent.putExtra("password", "");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

