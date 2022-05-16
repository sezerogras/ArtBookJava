package com.example.artbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.artbookjava.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;


public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    Bitmap selectedBitmap;
    SQLiteDatabase  database ; // bir cok yerde kullanacagım için buraya tanımladım

    ActivityResultLauncher<Intent>  activityResultLauncher;  // galeriye gitmek için
    ActivityResultLauncher<String> permissionLauncher;       // izin istemek için

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        database = this.openOrCreateDatabase("Sanat",MODE_PRIVATE,null);




        Intent intent= getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new") && getIntent().getStringExtra("info")!=null){
            //new art
            binding.nameText.setText("");
            binding.artText.setText("");
            binding.yearText.setText("");
           Bitmap selectImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.select);
            binding.imageView2.setImageResource(R.drawable.select);
            binding.button.setVisibility(View.VISIBLE); // Buton burada görünür olsun

        }
        else{
            int artId = intent.getIntExtra("artId",1);
            binding.button.setVisibility(View.INVISIBLE); // burada ise buton gözükmesin


            try{

                Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id =?",new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){

                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);// byte dizisini alıp bitmapa ceviriyor
                    binding.imageView2.setImageBitmap(bitmap);



                }
                cursor.close();


            }catch (Exception e) {

                e.printStackTrace(); // bu şekilde sıkıntıyı alabilirim


            }
        }

    }


    public void save(View view){                                //burada ise kaydetme işlemleri gerçekleştrildi.
     String name = binding.nameText.getText().toString();
     String artist = binding.artText.getText().toString();
     String year = binding.yearText.getText().toString();
     Bitmap smaleImage = makeSmallerImage(selectedBitmap,200); // ne çok büyük olsun ne de çok küçük 300 ideal

        // veri tabanına kaydetmem için veriye çevirmem lazım onun için aşsağıdaki yöntem kullanıldı  yani aslında 1 ve 0 a çevirme işlemi oldu
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       selectedBitmap.compress(Bitmap.CompressFormat.PNG,50, ( outputStream));
        byte[]  byteArray =    outputStream.toByteArray(); // aslında bir bayt dizisine yani 1 ve 0 lara çevirmiş olduk

           try{  // uygulamanın patlamasını engellemek için hata ayıklama kullandım
               database = this.openOrCreateDatabase("Sanat",MODE_PRIVATE,null);
               database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");
               String sqlString ="INSERT INTO arts (artname, paintername, year,image) VALUES (?, ?, ?,?)";

               SQLiteStatement sqLiteStatement =database.compileStatement(sqlString);
               sqLiteStatement.bindString(1,name);
               sqLiteStatement.bindString(2,artist);
               sqLiteStatement.bindString(3,year);
              sqLiteStatement.bindBlob(4,byteArray);
               sqLiteStatement.execute(); // son olarak der isek bunu çalıştıracaktır

           } catch (NullPointerException nes){

               nes.printStackTrace();
           } catch (Exception e) {
               e.printStackTrace();

           }

               Intent intent = new Intent(ArtActivity.this,MainActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // burada main aktivitiye dönerken açık olan bütün aktivitleri kapat dedim
               startActivity(intent); // buraya kaydettiktwn sonra main aktivitiye dönmüş oldum
               // finish();




    }  // kaydetme işlemi


    public Bitmap makeSmallerImage(Bitmap image, int maksimumSize){    // imagenin büyüklüğü veri tabanında hataya sebep olacağı için küçültme işlemi yapacağım bu metotla ,bu metodu istediğin yerde kullanabilirsin


        int height = image.getHeight();  // yüksesklik ve genişliklerini alıyorum çünkü resmimin hangi boyutta olduğunu bilmiyorum ve orantısız küçültmenin önüne geçebilmek için

        int width = image.getWidth();
        float  bitmapRatio   =(float) width / (float)height ;                          // bitmap ratio bitmap oarnı demek

        if(bitmapRatio>1) {
            // landscape image yani yatay konumda image

            width = maksimumSize;
            height =(int) (width / bitmapRatio);  // matematiksel orana göre küçültme işlemi
        }
        else {
            // portrait image yani dikey image
            height = maksimumSize;
            width = (int)(height*bitmapRatio);  // burada bölüm yapmıyorum çünkü daha da büyür o yüzden çarpıyorum

        }


      return Bitmap.createScaledBitmap(image,width,height,true);  // boyutları bu kadar olan küçültme işlemi..


    }   // image boyut küçültme metodu.


    public void selectImage(View view ){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ // burada izinin olup olmadığını kontrol ettim yani daha önce izin verilmiş mi verilmemiş mi kontrol ediyorum bu kodla

        if(ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.READ_EXTERNAL_STORAGE)){

            Snackbar.make(view , "Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission ", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // requested permission yani izin isteme

                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }).show();
        }
        else {
            // requested permission
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);  // aymı şey, burayada yazdım yani izin isteme komutu

        }



    }
       else {

           // gallery

            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // ıntent sadece aktivitileri degistirmke icin kullanilmıyor actionn pick ile  secip aldım yani
            activityResultLauncher.launch(intentToGallery); // yine bu da daleriye gidecek


    }



}   // görsel seçme işlemleri gerçekleşttirildi.


    private void registerLauncher() {



        activityResultLauncher =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){  // result ok yani kullanıcı bir şey secti mi

                  Intent intentFromResult =  result.getData();

                  if(intentFromResult !=  null){

                   Uri  imageData = intentFromResult.getData();

                 //    binding.imageView2.setImageURI(imageData);   veriyi bitmap a cevirmem lazım

                      try {

                          if(Build.VERSION.SDK_INT >= 28) {
                              ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(), imageData);

                              selectedBitmap = ImageDecoder.decodeBitmap(source);  // sadece yyeni telefonlarda calısır

                              binding.imageView2.setImageBitmap(selectedBitmap);

                          }
                          else {
                              selectedBitmap = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                              binding.imageView2.setImageBitmap(selectedBitmap);


                          }

                      } catch (Exception  e){

                          // hatayı iceride yakala

                          e.printStackTrace();
                      }


                  }

                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {


                if(result) {
                                                            // burada izin işlerrimi bir metot altınd yazdım cunkju oncreate altında aktıf olduğunu belirteceğim ondan sonra metotları izin isteme durumlarına göre çağıracağım..

                    // izin verildi..
                    Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttoGallery);  // izin aldıysam o zaman galeriye gitmem gereli

                }
                 else {

                     // iziin verilmedi...

                    Toast.makeText(ArtActivity.this, "permission needed!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });



    }      //başlatıcıyı kaydetme işlemi gerçekleştrildi.


}