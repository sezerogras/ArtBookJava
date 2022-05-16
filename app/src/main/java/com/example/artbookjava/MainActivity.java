package com.example.artbookjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.LinearLayout;

import com.example.artbookjava.databinding.ActivityMainBinding;
import com.example.artbookjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;
import java.util.concurrent.CountedCompleter;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        getData();

    }

    private void getData(){     // verileri çekmek için bu fonksiyonu kullanacağım

        try{

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Sanat",MODE_PRIVATE,null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts",null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){

                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);

                  Art art = new Art(name, id);
                  artArrayList.add(art);


            }
            artAdapter.notifyItemChanged(artAdapter.position);


            cursor.close();


        }catch (Exception e){
            e.printStackTrace();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//ınflater
        MenuInflater menuInflater = getMenuInflater();    // burada yazdığım kod satırları menuyu activitiye baglama işlemi yapıyor ....
        menuInflater.inflate(R.menu.art_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {  // menuye tıklanınca ne olacığına bu kodları kullanarak öğrendim


        if(item.getItemId() == R.id.add_Art){

            Intent intent = new Intent(MainActivity.this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }
}