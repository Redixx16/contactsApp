package com.upn.contactsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.upn.contactsapp.activities.CreateContactActivity;
import com.upn.contactsapp.activities.LoginActivity;
import com.upn.contactsapp.adapters.ContactAdaptar;
import com.upn.contactsapp.daos.ContactDAO;
import com.upn.contactsapp.entities.Contact;
import com.upn.contactsapp.services.ContactService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
// Asegúrate de tener las siguientes importaciones
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    List<Contact> elementos = new ArrayList<>();
    ContactAdaptar adaptar;

    // Variables para la paginación
    private int currentPage = 1;
    private final int limit = 10; // Número de elementos por página
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private ContactService service;
    private ContactDAO contactDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar si el usuario está autenticado
        SharedPreferences sharedPref = getSharedPreferences("com.upn.contactsapp", Context.MODE_PRIVATE);
        String token = sharedPref.getString("TOKEN", null);
        Log.i("MainActivity", "TOKEN: " + token);

        if (token == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inicializar la base de datos y el DAO
        AppDatabase db = AppDatabase.getInstance(this);
        contactDAO = db.contactDAO();

        // Configurar Retrofit y el servicio
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://66d5b903f5859a7042673752.mockapi.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ContactService.class);

        // Configurar RecyclerView y adaptador
        setUpRecyclerView();

        // Cargar la primera página de contactos
        loadContacts(currentPage);

        // Botón para crear un nuevo contacto
        FloatingActionButton btnCreateContact = findViewById(R.id.btnCreateContact);
        btnCreateContact.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateContactActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    private void loadContacts(int page) {
        isLoading = true;

        // Mostrar el indicador de carga
        if (currentPage > 1) {
            adaptar.addLoading();
        }

        service.getContacts(page, limit).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                isLoading = false;

                // Quitar el indicador de carga
                if (currentPage > 1) {
                    adaptar.removeLoading();
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Contact> contacts = response.body();

                    if (contacts.size() < limit) {
                        isLastPage = true;
                    }

                    elementos.addAll(contacts);
                    adaptar.notifyDataSetChanged();

                    // Guardar contactos en la base de datos local
                    for (Contact contact : contacts) {
                        Contact localContact = contactDAO.findRemote(contact.id);
                        if (localContact == null) {
                            contactDAO.insert(contact);
                        }
                    }
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                isLoading = false;

                // Quitar el indicador de carga
                if (currentPage > 1) {
                    adaptar.removeLoading();
                }

                Log.e("MainActivity", t.getMessage());
            }
        });
    }

    private void setUpRecyclerView() {
        RecyclerView rvContacts = findViewById(R.id.rvContacts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvContacts.setLayoutManager(layoutManager);

        adaptar = new ContactAdaptar(elementos);
        rvContacts.setAdapter(adaptar);

        rvContacts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0){ // Verificar si el usuario está haciendo scroll hacia abajo
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            currentPage++;
                            loadContacts(currentPage);
                        }
                    }
                }
            }
        });
    }

    // Manejar el resultado al crear un nuevo contacto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String contactJson = data.getStringExtra("CONTACT");
            Contact contact = new Gson().fromJson(contactJson, Contact.class);

            elementos.add(0, contact); // Agregar al inicio de la lista
            adaptar.notifyItemInserted(0);
        }
    }
}
