package it.polimi.dima.SkitalkAudioServer.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpRequest implements Runnable {

    private String targetURL;
    private String urlParameters;
    private JsonObject res = null;
    private JsonArray jsonArr;


    public HttpRequest(String url, String par){
        targetURL = url;
        urlParameters = par;
    }

    public void run() {
        URL url;
        String response="";
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(targetURL+"?"+urlParameters);

            //System.out.println("Request to: "+targetURL+"?"+urlParameters);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();

            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                response += current;

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        JsonParser parser = new JsonParser();
        jsonArr = (JsonArray) parser.parse(response);
        res = (JsonObject) jsonArr.get(0);

    }

    public JsonObject getResponse(){
        return res;
    }


    public JsonArray getArrayResponse(){
        return jsonArr;
    }

}