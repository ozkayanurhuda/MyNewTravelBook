package com.nurozkaya.mynewtravelbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //çektiğimiz isim ve locationları kaydetmek için arraylist
    static ArrayList<String> names = new ArrayList<String>(); // maps act den de erişmek için static yaptım
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // menumüzü bağlıyoruz
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //menuye tıklandığında ne olacak

        if (item.getItemId() == R.id.add_place) { // eğer tıklanılan menu add_place menusüyse
            //intent
            Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("info","new"); // yeni yer
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.listView);

        // sqlite işlemlerini yapıyoruz
        try {

            MapsActivity.database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null); // veri tabanını çekiyorum
            Cursor cursor = MapsActivity.database.rawQuery("SELECT * FROM places",null);

            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");



            while (cursor.moveToNext()) { // bir sonrakine gidebildiğin kadar git

                // dataları alıyoruz
                String nameFromDatabase = cursor.getString(nameIx); // name indexi al
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

                // aldıktan sonra stringleri double a çevirmemiz gerek
                // arrayliste eklememiz gerekiyor
                names.add(nameFromDatabase);
                Double l1 = Double.parseDouble(latitudeFromDatabase);
                Double l2 = Double.parseDouble(longitudeFromDatabase);

                // doublelardan bir lokasyon oluşturduk
                LatLng locationFromDatabase = new LatLng(l1,l2);

                // lokasyonu da buraya kaydettik
                locations.add(locationFromDatabase);



            }
            cursor.close();

        }catch (Exception e) {

        }

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,names);
        listView.setAdapter(arrayAdapter);

        // listwievda herhangi birşeye tıklandığında ne olacağı
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class); // intent bizi maps act ye götürecek

                intent.putExtra("info","old"); // eski seçili yer

                //seçilen yerin pozisyonunu yolluyorum 1,2,3 diye
                intent.putExtra("position",position);




                startActivity(intent);
            }
        });

    }


}