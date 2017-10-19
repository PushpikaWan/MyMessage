package com.example.pushpika.mymessage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomsActivity extends AppCompatActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        gson = new Gson();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        sharedPreferences = getSharedPreferences("rooms", Context.MODE_PRIVATE);

        adapter = new ContactAdapter(this, getContacts());
        adapter.setOnClickListener(new ContactAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                openChatWith(adapter.getContacts().get(position));
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.rooms, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure wants to logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Qiscus.clearUser();
                            sharedPreferences.edit().putString("isLogged","no").apply();
                            sharedPreferences.edit().clear().apply();
                            startActivity(new Intent(RoomsActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewChat(View view) {
        NewChatDialog.newInstance(new NewChatDialog.Listener() {
            @Override
            public void onSubmit(String fname, String lname, String email,String id, String message) {
                Toast.makeText(RoomsActivity.this, message, Toast.LENGTH_LONG).show();
                openChatWith(new Contact(email,fname,lname,id));
            }
        }).show(getSupportFragmentManager(), TAG);
    }

    private void openChatWith(final Contact contact) {
        saveContact(contact);
        //showLoading();
//        Qiscus.buildChatWith(contact.getEmail())
//                .withTitle(contact.getName())
//                .build(this, new Qiscus.ChatActivityBuilderListener() {
//                    @Override
//                    public void onSuccess(Intent intent) {
//                        saveContact(contact);
//                        startActivity(intent);
//                        dismissLoading();
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        if (throwable instanceof HttpException) { //Error response from server
//                            HttpException e = (HttpException) throwable;
//                            try {
//                                String errorMessage = e.response().errorBody().string();
//                                Log.e(TAG, errorMessage);
//                                showError(errorMessage);
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                        } else if (throwable instanceof IOException) { //Error from network
//                            showError("Can not connect to qiscus server!");
//                        } else { //Unknown error
//                            showError("Unexpected error!");
//                        }
//                        throwable.printStackTrace();
//                        dismissLoading();
//                    }
//                });
    }

    private List<Contact> getContacts() {
        String json = sharedPreferences.getString("contacts", "");
        return gson.fromJson(json, new TypeToken<List<Contact>>() {}.getType());
    }

    private void saveContact(Contact contact) {
        List<Contact> contacts = getContacts();
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        if (!contacts.contains(contact)) {
            contacts.add(contact);
            sharedPreferences.edit().putString("contacts", gson.toJson(contacts)).apply();
            updateList(contact);
        }
    }

    private void updateList(Contact contact) {
        if (!adapter.getContacts().contains(contact)) {
            adapter.getContacts().add(contact);
            adapter.notifyDataSetChanged();
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissLoading() {
        progressDialog.dismiss();
    }
}
