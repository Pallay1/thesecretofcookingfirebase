package com.example.thesecretofcooking20;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listaRecetasView;
    private DatabaseHelper baseDatosHelper;
    private ArrayList<String> listaRecetas;
    private ArrayAdapter<String> adapter;
    private Button btnAddRecipe, btnDeleteRecipe, btnEditRecipe, logoutButton; // Botón para cerrar sesión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar base de datos y vistas
        baseDatosHelper = new DatabaseHelper(this);
        listaRecetas = new ArrayList<>();
        listaRecetasView = findViewById(R.id.ListaRecetasView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaRecetas);
        listaRecetasView.setAdapter(adapter);

        // Inicializar botones
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnDeleteRecipe = findViewById(R.id.btnDeleteRecipe);
        btnEditRecipe = findViewById(R.id.btnEditRecipe);
        logoutButton = findViewById(R.id.logoutButton); // Botón para cerrar sesión

        cargarRecetasDesdeBaseDeDatos();

        // Configurar eventos de los botones
        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoAgregarReceta();
            }
        });

        btnDeleteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEliminarReceta();
            }
        });

        btnEditRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEditarReceta();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    private void mostrarDialogoAgregarReceta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Receta");
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_add_recipe, null);
        builder.setView(viewInflated);

        final EditText inputRecipeName = viewInflated.findViewById(R.id.inputRecipeName);
        final EditText inputIngredients = viewInflated.findViewById(R.id.inputIngredients);
        final EditText inputRating = viewInflated.findViewById(R.id.inputRating);

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre = inputRecipeName.getText().toString();
                String ingredientes = inputIngredients.getText().toString();
                int calificacion;
                try {
                    calificacion = Integer.parseInt(inputRating.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Calificación no válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                baseDatosHelper.agregarReceta(nombre, ingredientes, calificacion);
                cargarRecetasDesdeBaseDeDatos();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void mostrarDialogoEliminarReceta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Receta");
        final EditText input = new EditText(this);
        input.setHint("Nombre de la receta");
        builder.setView(input);

        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre = input.getText().toString();
                int filasEliminadas = baseDatosHelper.eliminarReceta(nombre);
                if (filasEliminadas > 0) {
                    cargarRecetasDesdeBaseDeDatos();
                    Toast.makeText(MainActivity.this, "Receta eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No se encontró la receta", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void mostrarDialogoEditarReceta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Receta");
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_add_recipe, null);
        builder.setView(viewInflated);

        final EditText inputRecipeName = viewInflated.findViewById(R.id.inputRecipeName);
        final EditText inputIngredients = viewInflated.findViewById(R.id.inputIngredients);
        final EditText inputRating = viewInflated.findViewById(R.id.inputRating);

        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre = inputRecipeName.getText().toString();
                String ingredientes = inputIngredients.getText().toString();
                int calificacion;
                try {
                    calificacion = Integer.parseInt(inputRating.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Calificación no válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                baseDatosHelper.actualizarReceta(nombre, ingredientes, calificacion);
                cargarRecetasDesdeBaseDeDatos();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void cargarRecetasDesdeBaseDeDatos() {
        listaRecetas.clear();
        Cursor cursor = baseDatosHelper.obtenerTodasLasRecetas();
        if (cursor.moveToFirst()) {
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String ingredientes = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_INGREDIENTS));
                int calificacion = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_RATING));
                String receta = "Nombre: " + nombre + "\nIngredientes: " + ingredientes + "\nCalificación: " + calificacion;
                listaRecetas.add(receta);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut(); // Cerrar sesión en Firebase
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }
}
