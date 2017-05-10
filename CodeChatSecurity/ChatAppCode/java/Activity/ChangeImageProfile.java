package com.example.dado.chatsecurity.Activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dado.chatsecurity.Application.EndPoint;
import com.example.dado.chatsecurity.Application.MyApplication;
import com.example.dado.chatsecurity.Model.User;
import com.example.dado.chatsecurity.Model.Utility;
import com.example.dado.chatsecurity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dado on 30/08/16.
 */
public class ChangeImageProfile extends AppCompatActivity {


    private static final String TAG=ChangeImageProfile.class.getSimpleName();
    CircleImageView imageProfile;
    Button changeImage;
    Button ok;
    private String userChoosenTask;
    final static int REQUEST_CAMERA=1,SELECT_FILE=2;
    private static final String name_file = "pref_image";
    SharedPreferences preferences;
    String encodedString, imagePath, fileName;
    Bitmap bitmap;
    boolean orientationChanged=false;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_profile_image_activity);

        progressDialog=new ProgressDialog(ChangeImageProfile.this);

        imageProfile=(CircleImageView)findViewById(R.id.imgViewImageChange);
        imageProfile.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(MyApplication.getInstance().getDbChat().getUserFromId(MyApplication.getInstance().getPrefManager().getUser().getID_USER()).getUrlImage()));;
        preferences = getSharedPreferences(name_file, 0);
        editor = preferences.edit();
        changeImage=(Button)findViewById(R.id.loadImageImageChange);
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ok=(Button)findViewById(R.id.sendDataToServerImageChange);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.haveInternetConnection()) {
                    setRequestedOrientation(getResources().getConfiguration().orientation);
                    progressDialog.setMessage("Attendere");
                    progressDialog.show();
                    orientationChanged=true;
                    uploadImage();
                }
                else
                    Toast.makeText(ChangeImageProfile.this,"Attivare la connessione Internet",Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences == null || preferences.getString("imagePath", null) == null) {
            //se il file sharedPreference è vuoto allora dobbiamo usre le immagine di default
            preferences = getSharedPreferences(name_file, 0);
            editor = preferences.edit();
            imageProfile.setImageBitmap(MyApplication.getInstance().loadImageFromStorage(MyApplication.getInstance().getDbChat().getUserFromId(MyApplication.getInstance().getPrefManager().getUser().getID_USER()).getUrlImage()));

        } else {
            imageProfile.setImageBitmap(BitmapFactory.decodeFile(preferences.getString("imagePath", null)));
        }

        }

    @Override
    protected void onPause() {
        progressDialog.dismiss();
      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onPause();
    }

    @Override
    protected void onStop() {
        progressDialog.dismiss();
      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onStop();
    }

    private void selectImage(){
        final CharSequence[] items = { "Scatta Foto", "Scegli dalla galleria",
                "Annulla" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeImageProfile.this);
        builder.setTitle("Carica Immagine Profilo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(ChangeImageProfile.this);
                if (items[item].equals("Scatta Foto")) {
                    ActivityCompat.requestPermissions(ChangeImageProfile.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scatta Foto";
                    if(result && !(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)&&
                            !(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                            !(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                        cameraIntent();
                    }
                } else if (items[item].equals("Scegli dalla galleria")) {
                    ActivityCompat.requestPermissions(ChangeImageProfile.this, new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);

                    userChoosenTask="Scegli dalla galleria";
                    if(result && !(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            && !(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            &&!(ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChangeImageProfile.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                        galleryIntent();
                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void cameraIntent(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_CAMERA);
    }

    //accedere alla galleria
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }



    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode==SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if(requestCode==REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
        else
            super.onActivityResult(requestCode, resultCode, data);

    }

    /********************************************************************************************************

                provare questp
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);


     *********************************************************************************************************/

    /*
        da provare a implementare questa funzione
     */

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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


            //imagePath = cursor.getString(columnIndex);
            cursor.close();
            //  Log.d(TAG,imagePath);

            /**********************************************************************/
            //provare a implementare questo//
            imagePath=getPath(getApplicationContext(),selectedImage);
            /*********************************************************************/


            //le salvo nel caso cambiasse orientazione dello schermo
            editor.putString("imagePath", imagePath);
            editor.commit();
            // Set the Image in ImageView
            //imgPath -> percorso immagine
            if (imageProfile != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null)
                    imageProfile.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                else
                    Toast.makeText(this, "BIitmap caricare Immagine", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Impossibile caricare Immagine", Toast.LENGTH_SHORT).show();
            }
            // Get the Image's file name
            String fileNameSegments[] = imagePath.split("/");
            //nome effettivo del file
            fileName = fileNameSegments[fileNameSegments.length - 1];
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
        if(imageProfile!=null)
            imageProfile.setImageBitmap(thumbnail);

    }


    public void uploadImage() {
        // When Image is selected from Gallery
     //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        // Convert image to String using Base64
        encodeImageToString();
        // When Image is not selected from Gallery

    }

    //codifica dell'immagine in stringa cosi da potrela invuare al server
    public void encodeImageToString() {

        new compressImageAsincrono().execute();
    }


    //**************compressione dell'immagine*******************//////////
    public class compressImageAsincrono extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //ottengo le opzioni dell'oggetto bitmap
            BitmapFactory.Options options = null;
            options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            //carico l'iimmagine bitmap primo controllo se l'utente ne ha caricata una
            //se cosi non fosse prendo l'immagine di default
            if (imagePath != null && !imagePath.isEmpty() && preferences != null && preferences.getString("imagePath", null) != null)
                bitmap = BitmapFactory.decodeFile(imagePath, options);
            else {
                bitmap = MyApplication.getInstance().loadImageFromStorage(MyApplication.getInstance().getDbChat().getUserFromId(MyApplication.getInstance().getPrefManager().getUser().getID_USER()).getUrlImage());
                fileName=MyApplication.getInstance().getPrefManager().getUser().getUrlImage();
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //lo comprimo
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            //otengo i byte
            byte[] byte_arr = stream.toByteArray();
            //immagine di default
            encodedString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new sendDataToServer().execute();

        }
    }

    public class sendDataToServer extends AsyncTask<Void,Void,Void> {

        String messaggioInput;


        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(EndPoint.BASE_URL + "updateImageProfile.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("id", MyApplication.getInstance().getPrefManager().getUser().getID_USER())
                        .appendQueryParameter("filename", fileName)
                        .appendQueryParameter("email", MyApplication.getInstance().getPrefManager().getUser().EMAIL)
                        .appendQueryParameter("image", encodedString)
                        .appendQueryParameter("password", MyApplication.getInstance().getPrefManager().getUser().getPasswordMD5())
                        .appendQueryParameter("root", "caputotavellamantovani99");

                String data = builder.build().getEncodedQuery();
                writer.write(data);
                writer.flush();
                writer.close();

                url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null)
                    stringBuilder.append(nextLine);

                messaggioInput = stringBuilder.toString();


            } catch (SSLHandshakeException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG,messaggioInput);
            JSONObject object = null;
            try {
                object = new JSONObject(messaggioInput);
                if (!object.getBoolean("errore")) {
                    Toast.makeText(ChangeImageProfile.this,"Immagine profilo cambiata",Toast.LENGTH_LONG).show();
                    new downloadAndSaveImage().execute();
                } else {
                    Toast.makeText(ChangeImageProfile.this,"Si è verificato un errore"+object.getString("risultato"),Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(orientationChanged){
            progressDialog.setMessage("Attendere");
            progressDialog.show();
            Log.e(TAG,"orientation");
        }

    }

    private class downloadAndSaveImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String []name=MyApplication.getInstance().getPrefManager().getUser().getUrlImage().split("/");
            Bitmap b = getBitmapFromURL(EndPoint.BASE_URL  + MyApplication.getInstance().getPrefManager().getUser().getUrlImage());
            String path = saveToInternalStorage(b, name[name.length-1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
            progressDialog.dismiss();

            orientationChanged=false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            startActivity(new Intent(ChangeImageProfile.this,SettingsActivity.class));
            finish();
        }

        public Bitmap getBitmapFromURL(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //funzione che salva l'immagine internamente dandole un nome
        private String saveToInternalStorage(Bitmap bitmapImage, String name) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageProfile", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, name);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
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




    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
