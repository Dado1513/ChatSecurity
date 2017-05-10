package com.example.dado.chatsecurity.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Gcm.Config;
import com.example.dado.chatsecurity.Model.Utility;
import com.example.dado.chatsecurity.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dado on 30/08/16.
 */
public class ActivityBackgroundImage extends AppCompatActivity {

    SeekBar impostaAlpha;
    ImageView imageViewBackground;
    Button loadImage,impostaSfondo,rimuoviSfondo;
    SharedPreferences preferencesFinale;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SharedPreferences.Editor editorFinale;
    private String userChoosenTask;
    final static int REQUEST_CAMERA=1,SELECT_FILE=2;
    String imagePath,fileName;
    Bitmap bitmapImage;
    String estensione;
    int graduazioneAlpha;
    public static final String TAG=ActivityBackgroundImage.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityimpostabackground);
        imageViewBackground=(ImageView)findViewById(R.id.imgViewSfondo);
        preferencesFinale = getSharedPreferences(Config.nameFileImageUrl, 0);
        preferences=getSharedPreferences("provvisorio",0);
        editor = preferences.edit();
        editorFinale=preferencesFinale.edit();
        //imageViewBackground.setAlpha(0.1f);
        impostaAlpha=(SeekBar)findViewById(R.id.seekBar);
        impostaAlpha.setProgress(10);


        String name=preferencesFinale.getString("nameImage",null);
        if(name!=null) {
            imageViewBackground.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(name));
            bitmapImage = MyApplication.getInstance().loadImageFromStorage(name);
            int alpha=preferencesFinale.getInt("alpha",10);
            imageViewBackground.setAlpha(alpha/(10.0f));
            impostaAlpha.setProgress(alpha);

            estensione="jpg";
        }
        loadImage=(Button)findViewById(R.id.loadImageSfonfo);
        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        impostaSfondo=(Button)findViewById(R.id.impostaImmagineSfondoButton);
        impostaSfondo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmapImage!=null && estensione!=null) {
                    editor.clear();
                    editor.commit();
                    saveToInternalStorage(bitmapImage, "sfondo." + estensione);
                    Toast.makeText(ActivityBackgroundImage.this,"Immagine Sfondo Impostata",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ActivityBackgroundImage.this,SettingsActivity.class));
                    finish();
                }
            }
        });

        impostaAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                graduazioneAlpha=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageViewBackground.setAlpha(graduazioneAlpha/(10.0f));

            }
        });
        rimuoviSfondo=(Button)findViewById(R.id.rimuoviImmagineSfondo);
        rimuoviSfondo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorFinale.clear();
                editorFinale.commit();
                Toast.makeText(ActivityBackgroundImage.this,"Immagine Sfondo Rimossa",Toast.LENGTH_LONG).show();
                startActivity(new Intent(ActivityBackgroundImage.this,SettingsActivity.class));
                finish();

            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.clear();
        editor.commit();
        startActivity(new Intent(ActivityBackgroundImage.this,SettingsActivity.class));
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences == null || preferences.getString("imagePath", null) == null) {
        } else {
            imageViewBackground.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));

        }

    }

    //verifica dei permessi e partono i vari intent
    private void selectImage(){
        final CharSequence[] items = { "Scatta Foto", "Scegli dalla galleria",
                "Annulla" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityBackgroundImage.this);
        builder.setTitle("Carica Immagine Profilo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(ActivityBackgroundImage.this);
                if (items[item].equals("Scatta Foto")) {
                    ActivityCompat.requestPermissions(ActivityBackgroundImage.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scatta Foto";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)&&
                            !(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                            !(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                        cameraIntent();
                    }
                } else if (items[item].equals("Scegli dalla galleria")) {
                    ActivityCompat.requestPermissions(ActivityBackgroundImage.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scegli dalla galleria";
                    if(result && !(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            && !(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            &&!(ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ActivityBackgroundImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                        galleryIntent();
                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //intent per accedere alla fotocamer
    private void cameraIntent(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_CAMERA);
    }

    //accedere alla galleria
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if(requestCode==REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null) {
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
            Log.d(TAG,imagePath);

            //le salvo nel caso cambiasse orientazione dello schermo
            editor.putString("imagePath", imagePath);
            editor.commit();
            // Set the Image in ImageView
            //imgPath -> percorso immagine
            if (imageViewBackground != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageViewBackground.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                    bitmapImage=bitmap;
                }else
                    Toast.makeText(this, "BIitmap caricare Immagine", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Impossibile caricare Immagine", Toast.LENGTH_SHORT).show();
            }
            // Get the Image's file name
            String fileNameSegments[] = imagePath.split("/");
            //nome effettivo del file
            fileName = fileNameSegments[fileNameSegments.length - 1];
            estensione="jpg";
        }

    }

    private void onCaptureImageResult(Intent data){

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String nameFile=System.currentTimeMillis()+"jpg";

        File destination = new File(Environment.getExternalStorageDirectory(),
                nameFile);
        FileOutputStream fo;

        try{
            destination.createNewFile();
            fo=new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        fileName=nameFile;
        imagePath=destination.getAbsolutePath();
        editor.putString("imagePath", imagePath);
        editor.commit();
        Log.d(TAG,imagePath);
        if(imageViewBackground!=null) {
            bitmapImage=thumbnail;
            estensione="jpg";
            imageViewBackground.setImageBitmap(thumbnail);
        }
    }


    //salvamento delimmagine all'interno del contextdell'app
    private String saveToInternalStorage(Bitmap bitmapImage,String name) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageProfile", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            editorFinale.putString("nameImage",name);
            editorFinale.putInt("alpha",graduazioneAlpha);
            editorFinale.commit();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
