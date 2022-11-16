package edu.uci.ics.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */

    // change host to AWS instance IP address and domain to our link
    private final String host = "52.12.152.71";
    private final String port = "8080";
    private final String domain = "cs-122b-group-37";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("login.success", response);
                    System.out.println(response);

                    // User has correct Login information
                    if (response.equalsIgnoreCase("{\"status\":\"success\"}")) {
                        //Complete and destroy login activity once successful
                        finish();
                        // initialize the activity(page)/destination
                        Intent MovieListPage = new Intent(LoginActivity.this, MovieListActivity.class);
                        // activate the list page.
                        startActivity(MovieListPage);
                    }
                    // User has incorrect Login information
                    else {
                        message.setText(response);
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                    message.setText("Unable to Login. Please Try again");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}