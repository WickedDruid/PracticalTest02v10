package ro.pub.cs.systems.eim.practicaltest02v10;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class CommunicationThread extends Thread {
    private static final String TAG = "CommunicationThread";
    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2/pokemon/";

    private final Socket clientSocket;

    public CommunicationThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String pokemonName = reader.readLine();
            Log.d(TAG, "Received request for pokemon: " + pokemonName);

            if (pokemonName != null && !pokemonName.isEmpty()) {
                String response = fetchPokemonData(pokemonName.toLowerCase().trim());
                writer.println(response);
            } else {
                writer.println("ERROR");
            }

            clientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "CERROR" + e.getMessage());
        }
    }

    private String fetchPokemonData(String pokemonName) {
        HttpURLConnection connection = null;
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(POKEAPI_URL + pokemonName);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(jsonResponse.toString());

                JSONArray typesArray = jsonObject.getJSONArray("types");
                StringBuilder types = new StringBuilder();
                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject typeObj = typesArray.getJSONObject(i);
                    JSONObject type = typeObj.getJSONObject("type");
                    if (i > 0) types.append(", ");
                    types.append(type.getString("name"));
                }

                JSONArray abilitiesArray = jsonObject.getJSONArray("abilities");
                StringBuilder abilities = new StringBuilder();
                for (int i = 0; i < abilitiesArray.length(); i++) {
                    JSONObject abilityObj = abilitiesArray.getJSONObject(i);
                    JSONObject ability = abilityObj.getJSONObject("ability");
                    if (i > 0) abilities.append(", ");
                    abilities.append(ability.getString("name"));
                }

                JSONObject sprites = jsonObject.getJSONObject("sprites");
                String spriteUrl = sprites.optString("front_default", "");

                result.append(types.toString())
                        .append("|")
                        .append(abilities.toString())
                        .append("|")
                        .append(spriteUrl);

            } else {
                result.append("ERROR");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error" + e.getMessage());
            result.append("ERROR:").append(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }
}
