package com.jorisvanwilligen.amsterdamparkeerapp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;




public class MainActivity extends Activity implements LocationListener {

    protected LocationManager locationManager;                                                                          //Constructor aanmaken om locatie te bepalen.
    protected boolean menu_initialized = false;
    public String url = "";
    double dLatitude = 0, dLongitude = 0;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);                                //Begin met locatie zoeken

    }




    @Override
    public void onLocationChanged(Location location) {
        if (menu_initialized == false) {                                                                                  //Als de locatie bepaald is controlleer of het menu als is geïnitialiseerd

            dLatitude = location.getLatitude();                                                                           //Locatie gegevens opslaan
            dLongitude = location.getLongitude();
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);                                                                          //Url maken met de gegevens

            url = ("http://divvapi.parkshark.nl/apitest.jsp?action=plan&to_lat="+dLatitude+"&to_lon="+dLongitude+"&dd="+day+"&mm=12&yy="+year+"&h="+hour+"&m="+minute+"&dur=2&opt_routes=y&opt_routes_ret=n&opt_am=n&opt_rec=y");
            //url = ("http://www.divvapi.parkshark.nl/rest/plan?to_lat="+dLatitude+"&to_lon="+dLongitude+"&dd="+day+"&mm="+month+"&yy="+year+"&h="+hour+"&m="+minute+"&dur=2&opt_routes=y&opt_routes_ret=n&opt_am=n&opt_rec=y");


            BackTaskGetJSON backTaskGetJSON= new BackTaskGetJSON();
            backTaskGetJSON.execute();                                                                                     //JSON ophalen met method
            TextView laadBericht = (TextView) findViewById(R.id.laadBericht);
            laadBericht.setText("Gegevens Ophalen..");                                                                     //Laadbericht aanpassen
            //laadBericht.setText(url); //debug
            menu_initialized = true;
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }






    private void showResult(String jsonString){

        //Parse Json and show OUTPUT
        try {
            JSONObject root = new JSONObject(jsonString);                                                                   //JSON parsen
            JSONObject result = root.getJSONObject("result");
            JSONArray reccommendations = result.getJSONArray("reccommendations");


            final ArrayList<String> namenLijst = new ArrayList<String>();                                                        //Lists aanmaken met gegevens
            final ArrayList<String> adressenLijst = new ArrayList<String>();
            final ArrayList<String> afstandenLijst = new ArrayList<String>();
            final ArrayList<String> Latitude = new ArrayList<String>();
            final ArrayList<String> Longitude = new ArrayList<String>();
            final ArrayList<String> type = new ArrayList<String>();

            for (int i = 0; i < reccommendations.length(); i++) {

                JSONObject obj=reccommendations.getJSONObject(i);




                namenLijst.add(obj.getString("name"));                                                                       //For loop gebruiken om gegevens in de lijst te krijgen.
                adressenLijst.add(obj.getString("address"));
                afstandenLijst.add("Afstand: " + obj.getDouble("dist_in_meters") / 1000 + " km");
                Latitude.add(obj.getString("lat"));
                Longitude.add(obj.getString("lon"));
                type.add(obj.getString("type"));


            }

            ListAdapter mijnAdapter = new CustomAdapter(this,namenLijst,adressenLijst,afstandenLijst);                       //De custom rows gebruiken om de lijst op te bouwen
            ListView parkeerLijst = (ListView) findViewById(R.id.parkeerLijst);
            parkeerLijst.setAdapter(mijnAdapter);
            final Intent intent = new Intent(this, MapsActivity.class);
            final int beschikbarePlaatsen = reccommendations.length();

            parkeerLijst.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                            intent.putExtra("latitude", Double.parseDouble(Latitude.get(position)));                         //Intent putExtra gebruiken om gegevens door te spelen naar de volgende activity
                            intent.putExtra("longitude", Double.parseDouble(Longitude.get(position)));
                            intent.putExtra("naam", namenLijst.get(position));
                            intent.putExtra("adress", adressenLijst.get(position));
                            intent.putExtra("type", ("Type: "+type.get(position)));
                            intent.putExtra("Beschikbaarheid", beschikbarePlaatsen);
                            startActivity(intent);

                        }
                    }
            );

            ProgressBar laadbalk = (ProgressBar) findViewById(R.id.progressBar);
            laadbalk.setVisibility(View.GONE);
            TextView laadBericht = (TextView) findViewById(R.id.laadBericht);
            laadBericht.setVisibility(View.GONE);                                                                            //Als de lijst is gemaakt, de laadbalk en tekst weghalen

            TextView beschikbaarheidText = (TextView) findViewById(R.id.beschikbaarheidText);
            TextView appTitelText = (TextView) findViewById(R.id.appTitelText);

            beschikbaarheidText.setVisibility(View.VISIBLE);
            appTitelText.setVisibility(View.VISIBLE);



            if (beschikbarePlaatsen == 0){                                                                                   //Tekst aanpassen als er geen, 1 of meerdere parkeerplaaten zijn.
                beschikbaarheidText.setText("Geen beschikbare parkeerplaatsen gevonden.");
            }else {
                if (beschikbarePlaatsen == 1){
                    beschikbaarheidText.setText("1 beschikbare parkeerplaats gevonden.");
                }else {
                    beschikbaarheidText.setText(beschikbarePlaatsen + " beschikbare parkeerplaatsen gevonden.");
                }
            }





        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //background process to get outlet info at remotee server
    private class BackTaskGetJSON extends AsyncTask<String,Void,Void> {
        String jsonString="";

        protected Void doInBackground(String...params){

            try{
                HttpClient httpclient=new DefaultHttpClient();                                                                 //HttpClient gebruiken om JSON te downloaden van url
                HttpPost httppost= new HttpPost(url); //delivery linux                                                         //bug.. mogelijk een library nodig
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                // Get json string from the url
                jsonString= httpclient.execute(httppost, responseHandler);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(Void result){
            try{
                if(!jsonString.equals("")) {
                    // Parse json and show output
                    showResult(jsonString);
                }
            }catch(Exception e){e.printStackTrace();}
        }

    }
}