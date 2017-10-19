package com.example.pushpika.mymessage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.PatternsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

        private EditText fnameField, lnameField, emailField, passwordField, rePasswordFeild;
        private ProgressBar progressBar;

        // Creating Volley RequestQueue.
        RequestQueue requestQueue;

        // Storing server url into String variable.
        String HttpUrl = MainActivity.baseUrl+"register";
        private ProgressDialog progressDialog;
        private SharedPreferences sharedPreferences;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);

            fnameField = (EditText) findViewById(R.id.input_fname);
            lnameField = (EditText) findViewById(R.id.input_lname);
            emailField = (EditText) findViewById(R.id.input_email);
            passwordField = (EditText) findViewById(R.id.input_password);
            rePasswordFeild = (EditText) findViewById(R.id.input_reEnterPassword);

            progressDialog= new ProgressDialog(RegisterActivity.this);


            // Creating Volley newRequestQueue .
            requestQueue = Volley.newRequestQueue(com.example.pushpika.mymessage.RegisterActivity.this);
            sharedPreferences = getSharedPreferences("rooms", Context.MODE_PRIVATE);

        }

    public void login(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
        public void setupUser(View view) {
            final String fname = fnameField.getText().toString();
            final String lname = lnameField.getText().toString();
            final String email = emailField.getText().toString();
            final String password = passwordField.getText().toString();
            final String rePassword = rePasswordFeild.getText().toString();

            if (!password.equals(rePassword)){
                rePasswordFeild.setError("Password mismatch");
                rePasswordFeild.requestFocus();
            }
            else if(fname.isEmpty()) {
                fnameField.setError("Please insert your name!");
                fnameField.requestFocus();
            } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.setError("Please insert a valid email!");
                emailField.requestFocus();
            } else if (password.isEmpty()) {
                passwordField.setError("Please insert your password!");
                passwordField.requestFocus();
            } else {
                progressDialog.setMessage("Loading...");
                progressDialog.show();
                final Intent intent = new Intent(this, MainActivity.class);
                //set volley call
                // Creating string request with post method.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String ServerResponse) {

                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();

                                // Showing response message coming from server.
                                Toast.makeText(com.example.pushpika.mymessage.RegisterActivity.this, ServerResponse, Toast.LENGTH_LONG).show();
                                startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                                // Hiding the progress dialog after all task complete.
                                progressDialog.dismiss();

                                // Showing error message if something goes wrong.
                                Toast.makeText(com.example.pushpika.mymessage.RegisterActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                            }
                        }) {

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() {

                        // Creating Map String Params.
                        Map<String, String> params = new HashMap<String, String>();

                        // Adding All values to Params.
                        //params.put("name", name);
                        params.put("fname",fname);
                        params.put("lname",lname);
                        params.put("email", email);
                        params.put("password", password);

                        return params;
                    }

                };

                // Creating RequestQueue.
                // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                // Adding the StringRequest object into requestQueue.
                requestQueue.add(stringRequest);

            }
        }


        private void showError(String errorMessage) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }

    }