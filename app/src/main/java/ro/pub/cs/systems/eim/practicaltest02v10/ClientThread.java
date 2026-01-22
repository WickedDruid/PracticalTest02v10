package ro.pub.cs.systems.eim.practicaltest02v10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class ClientThread extends Thread {
    private static final String TAG = "ClientThread";

    private final String address;
    private final int port;
    private final String pokemonName;
    private final TextView typesTextView;
    private final TextView abilitiesTextView;
    private final ImageView pokemonImageView;
    private final Handler mainHandler;

    public ClientThread(String address, int port, String pokemonName,
                        TextView typesTextView, TextView abilitiesTextView,
                        ImageView pokemonImageView) {
        this.address = address;
        this.port = port;
        this.pokemonName = pokemonName;
        this.typesTextView = typesTextView;
        this.abilitiesTextView = abilitiesTextView;
        this.pokemonImageView = pokemonImageView;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(address, port);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            writer.println(pokemonName);

            String response = reader.readLine();
            Log.d(TAG, "Received response: " + response);

            if (response != null && !response.startsWith("ERROR:")) {
                String[] parts = response.split("\\|");
                if (parts.length >= 3) {
                    final String types = parts[0];
                    final String abilities = parts[1];
                    final String spriteUrl = parts[2];

                    mainHandler.post(() -> {
                        typesTextView.setText(types);
                        abilitiesTextView.setText(abilities);
                    });

                    if (!spriteUrl.isEmpty()) {
                        loadImage(spriteUrl);
                    }
                }
            } else {
                final String errorMsg = response != null ?
                        response.replace("ERROR:", "") : "Unknown error";
                mainHandler.post(() -> {
                    typesTextView.setText(errorMsg);
                    abilitiesTextView.setText("");
                    pokemonImageView.setImageBitmap(null);
                });
            }

            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Client error: " + e.getMessage());
            mainHandler.post(() -> {
                typesTextView.setText("Error: " + e.getMessage());
                abilitiesTextView.setText("");
                pokemonImageView.setImageBitmap(null);
            });
        }
    }

    private void loadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            final Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
            mainHandler.post(() -> pokemonImageView.setImageBitmap(bitmap));
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
    }
}
