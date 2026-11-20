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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        //Barra de navegacion

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_inventario);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_vender) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_productos) {
                startActivity(new Intent(this, ProductoActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_inventario;
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(InventarioActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void cargarLista(String filtro) {
        listaProductos = db.listarProductos(filtro);
        List<Map<String, String>> datos = new ArrayList<>();

        for (Producto p : listaProductos) {
            Map<String, String> fila = new HashMap<>();
            fila.put("nombre", p.getNombre());
            fila.put("meta", "#" + p.getCodigo() + " • " + p.getCategoria());
            fila.put("stock", p.getStock() + " und.");
            fila.put("precio", String.format(Locale.US, "Venta: S/ %.2f", p.getPrecioVenta()));
            fila.put("ganancia", String.format(Locale.US, "Ganancia: +S/ %.2f", p.getGananciaUnitaria()));
            datos.add(fila);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                datos,
                R.layout.item_inventario,
                new String[]{"nombre", "meta", "stock", "precio", "ganancia"},
                new int[]{R.id.txtItemNombre, R.id.txtItemMeta, R.id.txtItemStock, R.id.txtItemPrecio, R.id.txtItemGanancia}
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