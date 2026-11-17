package com.renzcode.inventarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InventarioActivity extends AppCompatActivity {

    private EditText txtBuscar;
    private ListView lvInventario;
    private BaseDatos db;
    private ArrayList<Producto> listaProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        db = new BaseDatos(this);
        txtBuscar = findViewById(R.id.txtBuscarInventario);
        lvInventario = findViewById(R.id.lvInventario);

        // Cargar todos los productos al abrir la pantalla
        cargarLista("");

        // Filtrar mientras el usuario escribe
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarLista(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Eliminar producto al mantener presionado (clic largo)
        lvInventario.setOnItemLongClickListener((parent, view, position, id) -> {
            Producto seleccionado = listaProductos.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar producto")
                    .setMessage("¿Seguro que deseas eliminar \"" + seleccionado.getNombre() + "\" del inventario?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        if (db.eliminarProducto(seleccionado.getCodigo())) {
                            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                            cargarLista(txtBuscar.getText().toString().trim());
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    private void cargarLista(String filtro) {
        listaProductos = db.listarProductos(filtro);
        ArrayList<Map<String, String>> datos = new ArrayList<>();

        for (Producto p : listaProductos) {
            Map<String, String> fila = new HashMap<>();
            fila.put("titulo", "[" + p.getCodigo() + "] " + p.getNombre() + " (" + p.getCategoria() + ")");
            fila.put("detalle", String.format(Locale.US,
                    "Stock: %d | Venta: S/ %.2f | Ganancia unit.: S/ %.2f",
                    p.getStock(), p.getPrecioVenta(), p.getGananciaUnitaria()));
            datos.add(fila);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                datos,
                android.R.layout.simple_list_item_2,
                new String[]{"titulo", "detalle"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        lvInventario.setAdapter(adapter);
    }

    //MENÚ DE NAVEGACIÓN
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.opVender) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.opProductos) {
            startActivity(new Intent(this, ProductoActivity.class));
            finish();
        } else if (id == R.id.opHistorial) {
            startActivity(new Intent(this, HistorialActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}