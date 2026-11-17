package com.renzcode.inventarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

public class HistorialActivity extends AppCompatActivity {

    private ListView lvHistorial;
    private BaseDatos db;
    private ArrayList<Venta> listaVentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        db = new BaseDatos(this);
        lvHistorial = findViewById(R.id.lvHistorial);

        cargarVentas();

        // Modal de confirmación para eliminar una venta
        lvHistorial.setOnItemLongClickListener((parent, view, position, id) -> {
            Venta seleccionada = listaVentas.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Deseas eliminar el registro de venta de \"" + seleccionada.getNombreProducto() + "\" por S/ " + String.format(Locale.US, "%.2f", seleccionada.getTotalCobrado()) + "?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        if (db.eliminarVenta(seleccionada.getIdVenta())) {
                            Toast.makeText(this, "Venta eliminada del historial", Toast.LENGTH_SHORT).show();
                            cargarVentas();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    private void cargarVentas() {
        listaVentas = db.listarVentas();
        ArrayList<Map<String, String>> datos = new ArrayList<>();

        for (Venta v : listaVentas) {
            Map<String, String> fila = new HashMap<>();
            fila.put("titulo", v.getNombreProducto() + " (x" + v.getCantidad() + ") - Total: S/ " + String.format(Locale.US, "%.2f", v.getTotalCobrado()));
            fila.put("detalle", "Código: " + v.getCodigoProducto() + " | Fecha: " + v.getFecha());
            datos.add(fila);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                datos,
                android.R.layout.simple_list_item_2,
                new String[]{"titulo", "detalle"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        lvHistorial.setAdapter(adapter);
    }

    // ==================== MENÚ DE NAVEGACIÓN ====================
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
        } else if (id == R.id.opInventario) {
            startActivity(new Intent(this, InventarioActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}