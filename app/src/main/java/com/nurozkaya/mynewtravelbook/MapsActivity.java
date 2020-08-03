package com.nurozkaya.mynewtravelbook;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this); // uzun tıklamayı eşitliyoruz

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")) { // yeni bir yer eklemem bekleniyorsa


            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    // tek kelimelik kaydedilecek şeyleri shared preferences ile
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.nurozkaya.mynewtravelbook", MODE_PRIVATE);

                    // daha önceden notFirstTime diye bişey kaydetmediysem bana false gelicek,ilk defa appi kullanıyor demek
                    boolean firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", false);

                    // ikinci defada burası true olucak ve if çalışmayacak
                    if (!firstTimeCheck) { //  kullanıcının güncellenmesi işlemini ancak o zaman yap demek
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());// kullanıcı yerimiz
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15)); // kamera zoomu
                        sharedPreferences.edit().putBoolean("notFirstTime", true).apply(); // bundan sonra ilk gidişi olmuyor
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { // eğer izin yok ise

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // izin iste,aldıktan snrası onreqperres
                } else { //izin varsa

                    //kullanıcının konumunu almaya başla
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); // 10 sn ise 10000 yazman gerekiyor

                    mMap.clear();

                    // lastknown location için
                    Location lastLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER); // aynısını 23 ten küçükse de uygula
                    if (lastLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                    }
                }
            } else { // 23 ten küçükse izin istemeden konum alabilirz
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        } else { // yeni ise tee buraya kadar yap
            mMap.clear();
            // mainden gelen kaçıncıya tıklandı bilgisini aldım
            int position = intent.getIntExtra("position",0);
            LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
            String placeName = MainActivity.names.get(position); // ismini aldık

            mMap.addMarker(new MarkerOptions().title(placeName).position(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        }
    }

    @Override   // kullanıcının izni yoksa ve bu izni verirse ne olacağı
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (requestCode ==1) { // sdk int yerine context compat kullandık deneme olsun diye
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // eğer izin verilmişse
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener); // konum al

                    Intent intent = getIntent(); // yeni kayıtlı bi yer var mı diye, izin isterken de bakalım dedik ama gereksiz
                    String info = intent.getStringExtra("info");
                    if (info.matches("new")) {
                        Location lastLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }


                    } else { // eski ise yukarıdakinin aynısı
                        mMap.clear();
                        // mainden gelen kaçıncıya tıklandı bilgisini aldım
                        int position = intent.getIntExtra("position",0);
                        LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
                        String placeName = MainActivity.names.get(position); // ismini aldık

                        mMap.addMarker(new MarkerOptions().title(placeName).position(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));

                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) { // yukarı eklediğimiz metodun uzantısı
        // geocoder adresleri ve enlem boylamları eşleştirir
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1); // en fazla kaç result istediği
            if (addressList != null && addressList.size() > 0) {
                if (addressList.get(0).getThoroughfare() != null) { // thoroughfare kontrol ediyoruz
                    address += addressList.get(0).getThoroughfare();
                    if (addressList.get(0).getSubThoroughfare() != null) {
                        address += addressList.get(0).getSubThoroughfare();
                    }
                }
            } else { // addresslist boşsa
                address = "New Place";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().title(address).position(latLng)); // marker ekliyoruz
        // kullanıcı yeni bi yer yaptığının farkına varsın diye
        Toast.makeText(getApplicationContext(),"New Place OK !",Toast.LENGTH_SHORT).show();

        // appi tekrar kapatıp açmadan geri dönüldüğünde bilgiler kaydolsun diye
        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged(); // söyle arrayadapter a yeni datalar ekledim güncelleme yapsın demek

        try { // sql işlemleri try and catch içinde hep ,tıkladğımız yerleri database e kaydetmek için
            
            Double L1 = latLng.latitude; // enlem boylam aldım
            Double L2 = latLng.longitude;

            String coord1 = L1.toString(); // karışmaması için string olarak kaydettim
            String coord2 = L2.toString();

            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null); // handler a ihtiyaç yok null
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR)"); // değerleri aldım

            // compile edeceğim değerler için bir string daha
            String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?,?,?)"; // değerleri bilmediğim için ?

            //soru işaretleri yerine yeni değerler koymak için
            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            sqLiteStatement.bindString(1,address);
            sqLiteStatement.bindString(2,coord1);
            sqLiteStatement.bindString(3,coord2);

            sqLiteStatement.execute();

        } catch (Exception e) {

        }
    }
}