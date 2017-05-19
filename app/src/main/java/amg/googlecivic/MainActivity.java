package amg.googlecivic;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    String key="AIzaSyB35OQoTVJQr9FlkhdKEqkyVasiDiCssC4";
    String mapKey="AIzaSyCX2fGWlHM55Q4HK0plR3qTAasTB-zeliw";
    EditText streetName,cityName,stateName;
    String street1,city1,state1;
    String address;
    double mLat=0,mLng=0;
    String tempPoll="";
    String pollingStreet,pollingCity,pollingState,pollingZip,pollingHours;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Linking Editexts to the address fields

        streetName=(EditText)findViewById(R.id.streetName);
        street1=streetName.getText().toString();
        cityName=(EditText)findViewById(R.id.cityName);
        city1=cityName.getText().toString();
        stateName=(EditText)findViewById(R.id.stateName);
        state1=stateName.getText().toString();

        StringBuilder address1= new StringBuilder();

        address1.append(street1).append(" ").append(city1).append(" ").append(state1);

        try {

            address=URLEncoder.encode(address1.toString(),"UTF-8");


        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

    }

    class DownloadTask extends AsyncTask<String,Void,String> {
        protected void onPreExecute(){


        }

        protected String doInBackground(String... map) {

            try {
                //Converting Location into Lat and Long to initialise Maps activity

                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+map[0]+"&key="+mapKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                    finally{
                        urlConnection.disconnect();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }


        }




         protected void onPostExecute(String response){
             if(response == null)
                response= "Error";


             mapParser(response);

             Intent mapIntent= new Intent(MainActivity.this,MapsActivity.class);

             mapIntent.putExtra("Latitude",mLat);
             mapIntent.putExtra("Longitude",mLng);
             mapIntent.putExtra("poll",tempPoll);

             startActivity(mapIntent);

         }
    }



    class voterInfo extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... address) {


            try {

                URL url = new URL("https://www.googleapis.com/civicinfo/v2/voterinfo?key="+key+"&address="+address[0]+"&electionId=2000");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }




        protected void onPostExecute(String response){
            if(response == null)
                response= "Error";

            jsonParser(response);

        }
    }


public void getInfo(View view){
    streetName=(EditText)findViewById(R.id.streetName);
    street1=streetName.getText().toString();
    cityName=(EditText)findViewById(R.id.cityName);
    city1=cityName.getText().toString();
    stateName=(EditText)findViewById(R.id.stateName);
    state1=stateName.getText().toString();
    ProgressBar loadMap= (ProgressBar)findViewById(R.id.progressBar3);

    loadMap.setVisibility(View.VISIBLE);

        StringBuilder address1 = new StringBuilder();
        address1.append(street1).append(city1).append(state1);
        try {

            address = URLEncoder.encode(address1.toString(), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        voterInfo task1 = new voterInfo();
        task1.execute(address);



}


public void jsonParser(String string) {

    try {
        JSONObject reader = new JSONObject(string);




        JSONArray pollingLocationName = reader.getJSONArray("pollingLocations");
            for (int i = 0; i < pollingLocationName.length(); i++) {
                JSONObject address1 = new JSONObject(pollingLocationName.getString(i));

                pollingStreet = address1.getJSONObject("address").getString("line1");
                pollingCity = address1.getJSONObject("address").getString("city");
                pollingState = address1.getJSONObject("address").getString("state");
                pollingZip = address1.getJSONObject("address").getString("zip");
                pollingHours = address1.getString("pollingHours");
                StringBuilder pollLoc = new StringBuilder();
                pollLoc.append(pollingStreet).append(" ").append(pollingCity).append(" ").append(pollingState);
                tempPoll = pollLoc.toString();
                String pollLocation = URLEncoder.encode(pollLoc.toString(), "UTF-8");

                ProgressBar loadMap= (ProgressBar)findViewById(R.id.progressBar3);

                loadMap.setVisibility(View.INVISIBLE);
                DownloadTask task2 = new DownloadTask();
                task2.execute(pollLocation);

            }


    }catch(JSONException e){
        e.printStackTrace();
        Toast.makeText(getApplicationContext(),"Polling Locations not released yet.",Toast.LENGTH_SHORT).show();
        ProgressBar loadMap= (ProgressBar)findViewById(R.id.progressBar3);

        loadMap.setVisibility(View.INVISIBLE);
    }catch (UnsupportedEncodingException e){
        e.printStackTrace();
    }

}


    public void mapParser(String string){
        try{
            JSONObject mapReader=new JSONObject(string);
            JSONArray mapResult= mapReader.getJSONArray("results");
            for(int a=0;a<mapResult.length();a++) {
                JSONObject geometry = new JSONObject(mapResult.getString(a));

                mLat = geometry.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                mLng = geometry.getJSONObject("geometry").getJSONObject("location").getDouble("lng");




            }

        }catch(JSONException e){
            e.printStackTrace();
        }
    }


}
