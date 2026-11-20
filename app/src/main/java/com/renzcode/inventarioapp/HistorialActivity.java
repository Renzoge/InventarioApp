package com.renzcode.inventarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

        //Barra de navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_historial);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_vender) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_inventario) {
                startActivity(new Intent(this, InventarioActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_productos) {
                startActivity(new Intent(this, ProductoActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_historial;
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(HistorialActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void cargarVentas() {
        listaVentas = db.listarVentas();
        List<Map<String, String>> datos = new ArrayList<>();

        for (Venta v : listaVentas) {
            Map<String, String> fila = new HashMap<>();
            fila.put("producto", v.getNombreProducto());
            fila.put("detalle", v.getCantidad() + " unidades • Cód. #" + v.getCodigoProducto());
            fila.put("fecha", v.getFecha());
            fila.put("total", String.format(Locale.US, "S/ %.2f", v.getTotalCobrado()));
            datos.add(fila);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                datos,
                R.layout.item_historial,
                new String[]{"producto", "detalle", "fecha", "total"},
                new int[]{R.id.txtHistProducto, R.id.txtHistDetalle, R.id.txtHistFecha, R.id.txtHistTotal}
        );
        lvHistorial.setAdapter(adapter);
    }

}