package com.luko.networktest;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static String SERVER_URL = "se2-isys.aau.at";
    private static int PORT = 53212;

    private Context context;
    private TextView tvResponse;
    private TextView tvId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvResponse = findViewById(R.id.tvResponse);
        tvId = findViewById(R.id.tvMatrikelnummer);

        context = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends the given Matrikelnummer to the server and returns the result.
     * @param number
     */
    private String SendMatrikelNummer(int number) throws IOException {
        Socket clientSocket = new Socket(SERVER_URL, PORT);

        DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        outStream.writeBytes(Integer.toString(number) + "\n");

        String result = reader.readLine();

        clientSocket.close();

        return result;
    }

    private int AlternatingChecksum(int number) {
        boolean isMinus = true;
        String numberString = Integer.toString(number);
        int result = 0;

        for(int i = 0; i < numberString.length(); i++) {
            int numberAt = Integer.parseInt(String.valueOf(numberString.charAt(i)));

            if(isMinus) {
                result -= numberAt;
            }
            else {
                result += numberAt;
            }

            isMinus = !isMinus;
        }
        return result;
    }

    /**
     * Btn event handler to send the request.
     * @param view
     */
    public void handleSend(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int number = Integer.parseInt(tvId.getText().toString());
                    final String result = SendMatrikelNummer(number);
                    setResponseText(result);
                } catch (NumberFormatException ex) {
                    showToast("Matrikelnummer darf nur Ziffern enthalten.");
                    setResponseText("");
                } catch (IOException e) {
                    showToast("Netzwerkfehler.");
                    setResponseText("");
                }
                catch(Exception ex) {
                    showToast("Fehler");
                    setResponseText("");
                }
            }
        }).start();
    }

    private void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResponseText(final String result)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvResponse = findViewById(R.id.tvResponse);
                tvResponse.setText(result);
            }
        });
    }

    public void handleAlternatingChecksum(View view) {
        int number = Integer.parseInt(tvId.getText().toString());
        int result = AlternatingChecksum(number);
        setResponseText(String.valueOf(result));
    }
}
