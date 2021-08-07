package com.example.navitate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsernamePasswordEntry extends Fragment {

    private EditText usernameEntry;
    private EditText passwordEntry;
    private EditText titleEntry;
    private RetrofitInterface retrofitInterface;
    private static final String BASE_URL = "http://192.168.0.17:3000";
    private MapsActivity activity;
    private TextView titleView;
    private Button createAccountPostButton;
    private Button loginPostButton;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.username_password_title, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiate retrofit objects
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);


        loginPostButton = (Button) this.getView().findViewById(R.id.loginPostButton);
        loginPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPost();
            }
        });
        createAccountPostButton = (Button) this.getView().findViewById(R.id.createAccountAndPostButton);
        titleEntry = getView().findViewById(R.id.titleEntry);
        activity = (MapsActivity) this.getActivity();
        usernameEntry = (EditText) getView().findViewById(R.id.usernameEntry);
        passwordEntry = getView().findViewById(R.id.password_entry);


        createAccountPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccountPost();
            }
        });



    }

    public void loginPost(){
        String user = usernameEntry.getText().toString();
        String password = passwordEntry.getText().toString();
        String title = titleEntry.getText().toString();
        for (String string : new String[]{user, password, title}){
            if (string.equals("")){
                Toast.makeText(this.getContext(), "Some fields are empty", Toast.LENGTH_LONG).show();
                return;
            }
        }
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("username", user);
        metadata.put("password", password);
        metadata.put("title", title);
        metadata.put("type", "login");
        Call<Void> call = retrofitInterface.postUser(metadata);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(getContext(), "Successfully logged in", Toast.LENGTH_LONG).show();
                    assert activity != null;
                    activity.setMetaData(user, password, title);
                    activity.postAll();
                    getView().setVisibility(View.GONE);
                } else if (response.code() == 201){
                    Toast.makeText(getContext(), "Incorrect Password", Toast.LENGTH_LONG).show();
                } else if (response.code() == 202) {
                    Toast.makeText(getContext(), "No user exists with this username", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void createAccountPost(){
        String user = usernameEntry.getText().toString();
        String password = passwordEntry.getText().toString();
        String title = titleEntry.getText().toString();
        for (String string : new String[]{user, password, title}){
            if (string.equals("")){
                Toast.makeText(this.getContext(), "Some fields are empty", Toast.LENGTH_LONG).show();
                return;
            }
        }
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("username", user);
        metadata.put("password", password);
        metadata.put("title", title);
        metadata.put("type", "newUser");
        Call<Void> call = retrofitInterface.postUser(metadata);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(getContext(), "User created", Toast.LENGTH_LONG).show();
                    assert activity != null;
                    activity.setMetaData(user, password, title);
                    activity.postAll();
                    getView().setVisibility(View.GONE);
                } else if (response.code() == 201){
                    Toast.makeText(getContext(), "Username is taken", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void lockUsernameAndTitle(String username, String password, String title){
        createAccountPostButton.setVisibility(View.GONE);
        loginPostButton.setText(R.string.login);
        titleEntry.setText(title);
        titleEntry.setFocusable(false);
        titleEntry.setEnabled(false);
        titleEntry.setCursorVisible(false);
        titleEntry.setKeyListener(null);
        titleEntry.setBackgroundColor(Color.TRANSPARENT);
        usernameEntry.setText(username);
        usernameEntry.setFocusable(false);
        usernameEntry.setEnabled(false);
        usernameEntry.setCursorVisible(false);
        usernameEntry.setKeyListener(null);
        usernameEntry.setBackgroundColor(Color.TRANSPARENT);
        loginPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEntry.getText().toString().equals(password)){
                    getView().setVisibility(View.GONE);
                    activity.setEditable();
                } else {
                    System.out.println("title: " + title + " ... password: " + password);
                    Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

}
