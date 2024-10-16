package com.upn.contactsapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.upn.contactsapp.AppDatabase;
import com.upn.contactsapp.MainActivity;
import com.upn.contactsapp.R;
import com.upn.contactsapp.daos.ContactDAO;
import com.upn.contactsapp.entities.Contact;
import com.upn.contactsapp.services.ContactService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Permissions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateContactActivity extends AppCompatActivity {

    ImageView ivPhoto;
    String imageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        setUpBtnTakePhoto();
        setUpBtnChoosePhoto();
        ivPhoto = findViewById(R.id.ivPhoto);

        AppDatabase db = AppDatabase.getInstance(this);
        ContactDAO contactDAO = db.contactDAO();

        Button btnGuardarContacto = findViewById(R.id.btnGuardarContacto);
        EditText etName = findViewById(R.id.etName);
        EditText etPhone = findViewById(R.id.etPhone);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://66d5b903f5859a7042673752.mockapi.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ContactService service = retrofit.create(ContactService.class);

        btnGuardarContacto.setOnClickListener(view -> {

            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();

            Contact contact = new Contact(name, phone);
            contact.image = imageBase64;

            contact.localId = (int) contactDAO.insert(contact);

            Log.i("CONTACT_LOCAL_ID",  String.valueOf(contact.localId));

//            service.create(contact).enqueue(new Callback<Contact>() {
//                @Override
//                public void onResponse(Call<Contact> call, Response<Contact> response) {
//                    Log.i("MAIN_APP", String.valueOf(response.code()));
//
//                    if (response.isSuccessful()) {
//
//                        Contact newContact = response.body();
//
//                        Intent intent = getIntent();
//                        intent.putExtra("CONTACT", new Gson().toJson(newContact));
//
//                        contact.id = newContact.id;
//                        contactDAO.update(contact.localId, newContact.id);
//
//                        setResult(100, intent);
//                        finish();
//
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<Contact> call, Throwable throwable) {
//                    Log.e("MAIN_APP", throwable.getMessage());
//                }
//            });
        });

    }

    private void setUpBtnChoosePhoto() {
        Button btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnChoosePhoto.setOnClickListener(view -> {
            openPhotoGallery();
        });
    }

    private void setUpBtnTakePhoto() {
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(view -> {
            // preguntar si tiene permisos para abrir la camara
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // abrir camara
                openCamera();
            } else {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    private void openPhotoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            ivPhoto.setImageBitmap(imageBitmap);
        } if( requestCode == 101&& resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            ivPhoto.setImageURI(selectedImage);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}