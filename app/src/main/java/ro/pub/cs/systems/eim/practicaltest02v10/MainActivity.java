package ro.pub.cs.systems.eim.practicaltest02v10;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText serverPortEditText;
    private EditText clientAddressEditText;
    private EditText clientPortEditText;
    private EditText pokemonNameEditText;
    private Button startServerButton;
    private Button getPokemonButton;
    private ImageView pokemonImageView;
    private TextView pokemonTypesTextView;
    private TextView pokemonAbilitiesTextView;

    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        serverPortEditText = findViewById(R.id.server_port_edit_text);
        clientAddressEditText = findViewById(R.id.client_address_edit_text);
        clientPortEditText = findViewById(R.id.client_port_edit_text);
        pokemonNameEditText = findViewById(R.id.pokemon_name_edit_text);
        startServerButton = findViewById(R.id.start_server_button);
        getPokemonButton = findViewById(R.id.get_pokemon_button);
        pokemonImageView = findViewById(R.id.pokemon_image_view);
        pokemonTypesTextView = findViewById(R.id.pokemon_types_text_view);
        pokemonAbilitiesTextView = findViewById(R.id.pokemon_abilities_text_view);
    }

    private void setupListeners() {
        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });

        getPokemonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPokemonInfo();
            }
        });
    }

    private void startServer() {
        String portStr = serverPortEditText.getText().toString().trim();
        if (portStr.isEmpty()) {
            Toast.makeText(this, "Please enter a port number", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serverThread != null && serverThread.isAlive()) {
            Toast.makeText(this, "Server is already running", Toast.LENGTH_SHORT).show();
            return;
        }

        serverThread = new ServerThread(port);
        serverThread.start();
        Toast.makeText(this, "Server started on port " + port, Toast.LENGTH_SHORT).show();
    }

    private void getPokemonInfo() {
        String address = clientAddressEditText.getText().toString().trim();
        String portStr = clientPortEditText.getText().toString().trim();
        String pokemonName = pokemonNameEditText.getText().toString().trim();

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter server address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (portStr.isEmpty()) {
            Toast.makeText(this, "Please enter port number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pokemonName.isEmpty()) {
            Toast.makeText(this, "Please enter pokemon name", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            return;
        }

        ClientThread clientThread = new ClientThread(
                address, port, pokemonName,
                pokemonTypesTextView, pokemonAbilitiesTextView, pokemonImageView
        );
        clientThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.stopServer();
        }
    }
}
