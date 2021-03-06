package tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Activities.Trip.StartTrip;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Activities.MainActivity;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Activities.Trip.TripOptionsActivity;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Helpers.GPSTracker;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Models.JourneyStatus;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.R;

public class STResultActivity extends AppCompatActivity {

    private TextView message;
    private Button backBt;
    private Intent mainActivity;
    private Intent tripOptionsActivity;
    private Boolean success = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    GPSTracker gps;
    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stresult);
        Intent father = getIntent();

        message = (TextView) findViewById(R.id.startTripResultTV);
        backBt = (Button) findViewById(R.id.startTripResultBt);

        mainActivity = new Intent(this, MainActivity.class);
        tripOptionsActivity = new Intent(this, TripOptionsActivity.class);

        sharedPreferences = getSharedPreferences("USBusData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try {
            JSONObject journey = new JSONObject(father.getStringExtra("journey"));
            if(journey.getString("status").equalsIgnoreCase(JourneyStatus.LEFT.toString())) {
                message.setText("Viaje iniciado correctamente");
                editor.putString("journey", journey.toString());
                editor.putString("journeyId", journey.get("id").toString());
                editor.putString("onCourseJourney", journey.get("id").toString());
                editor.putBoolean("odometerSet", false);
                editor.putString("busId", journey.getJSONObject("bus").getString("id"));

                for (int i = 0;
                     i < journey
                        .getJSONObject("service")
                        .getJSONObject("route")
                        .getJSONArray("busStops")
                        .length();
                     i++ ) {

                    journey.getJSONObject("service")
                            .getJSONObject("route")
                            .getJSONArray("busStops")
                            .getJSONObject(i)
                            .put("status", "PENDIENTE");
                }
                journey.getJSONObject("service")
                        .getJSONObject("route")
                        .getJSONArray("busStops")
                        .getJSONObject(0)
                        .put("status", "PARTIÓ");

                //Remuevo origen para que no aparezca en el listado de paradas
                //journey.getJSONObject("service").getJSONObject("route").getJSONArray("busStops").remove(0);

                editor.putString("routeStops", journey
                                                .getJSONObject("service")
                                                .getJSONObject("route")
                                                .getJSONArray("busStops").toString());
                editor.putInt("standingPassengers", journey.getJSONObject("bus").getInt("standingPassengers"));
                editor.putInt("standingTotal", journey.getJSONObject("bus").getInt("standingPassengers"));

                editor.apply();
                success = true;

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, INITIAL_PERMS, 42);
                }

                try {

                    gps = new GPSTracker(getApplicationContext());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                message.setText("Ha ocurrido un error iniciando el viaje\n Intente nuevamente");
                success = false;
            }

            //System.out.println(journey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (success) {
                    startActivity(mainActivity);
                } else {
                    startActivity(tripOptionsActivity);
                }
            }
        });
    }
}
