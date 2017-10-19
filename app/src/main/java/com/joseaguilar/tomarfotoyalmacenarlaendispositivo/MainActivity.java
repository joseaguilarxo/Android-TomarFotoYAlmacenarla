package com.joseaguilar.tomarfotoyalmacenarlaendispositivo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    Button btnfoto;
    ImageView imgFoto;

    //PASO 1: Crearemos las constantes y variables que nos serviran para el manejo de creacion del FILE y direccion del guardado
    private static final String CARPETA_PRINCIPAL = "misImagenesApp/"; //directorio principal
    private static final String CARPETA_IMAGEN = "imagenes"; //carpeta donde se guardaran las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN; // ruta de carpeta de direcciones
    private String path; //almacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;
    private static final int COD_FOTO = 20;
    private final int MIS_PERMISOS = 100; //ESTO ES PARA LOS PERMISOS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnfoto = (Button) findViewById(R.id.TomaFoto);
        imgFoto = (ImageView) findViewById(R.id.imgFotos);

        //Permisos --> en el paso 3 se explica
        if(solicitaPermisosVersionesSuperiores()){
            btnfoto.setEnabled(true);
        }else{
            btnfoto.setEnabled(false);
        }


        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogOpciones();
            }
        });
    }



    //Paso 2: crearemos un AlertDialog para que muestre las opciones para tomar foto
    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {"Tomar Foto", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una Opcion");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (opciones[i].equals("Tomar Foto")) {
                    abrirCamara();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //Paso 3: ahora programamos toda la logica de creacion del file y guardado
    private void abrirCamara() {
        File miFile = new File(Environment.getExternalStorageDirectory(), DIRECTORIO_IMAGEN);
        boolean isCreada = miFile.exists();

        //Estos if es para ver si nuestro directorio ya fue creado
        if (isCreada == false) {
            isCreada = miFile.mkdirs();
        }
        if (isCreada == true) {
            Long consecutivo = System.currentTimeMillis() / 1000; //obtenemos la hora exacta de cuando fue tomada la foto (servira como nombre de la imagen)
            String nombre = consecutivo.toString() + ".jpg"; //consecutivo.jpg

            path = Environment.getExternalStorageDirectory() + File.separator + DIRECTORIO_IMAGEN + File.separator + nombre; //indicamos la ruta del almacenamiento

            fileImagen = new File(path); //construimos el archivo

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));


            //PASO 4: OTORGAMOS PERMISOS PARA CELULARES CON VERSIONES 6.0.0 EN ADELANTE
            //Esto solo se opcional aunque se recomienda, esto servira para otorgar permisos y usarlos , esto es por un tema
            // de celulares con versiones de android de 6.0.1 en adelante -> se creara el metodo : solicitaPermisosVersionesSuperiores
           if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=getApplicationContext().getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(getApplicationContext(),authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }

            startActivityForResult(intent,COD_FOTO); //startActivityForResult consumira el metodo onActivityResult
        }
    }

    //Paso 5: programamos onActivityResult -- con este metodo ya harmeos qe la camara tome la fotografia
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case COD_FOTO:

                MediaScannerConnection.scanFile(this, new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Path", "" + path);
                            }
                        });

                bitmap = BitmapFactory.decodeFile(path);
                imgFoto.setImageBitmap(bitmap);
                break;
        }

    }
    //Paso 6: CREAMOS LOGICA PARA SOLICITAR LAS VERSIONES SUPERIORES
    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if((getApplicationContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&getApplicationContext().checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    //paso 6.1 este sera un simple dialogo que saldra cuando se ejecute la app por primera vez solicitando permisos
    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(this);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
                }
            }
        });
        dialogo.show();
    }
}
