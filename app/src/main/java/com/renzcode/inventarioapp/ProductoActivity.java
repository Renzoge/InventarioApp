package com.renzcode.inventarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ProductoActivity extends AppCompatActivity {

    private EditText txtCodigo, txtNombre, txtCosto, txtPrecio, txtStock;
    private Spinner spCategoria;
    private ImageButton btnCamara;
    private Button btnBuscar, btnGuardar;

    private BaseDatos db;
    private Producto productoExistente = null;
    private final String[] categorias = {"Herramientas", "Electricidad", "Gasfitería", "Pinturas", "Construcción", "Otros"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        db = new BaseDatos(this);

        txtCodigo = findViewById(R.id.txtCodigoProd);
        txtNombre = findViewById(R.id.txtNombreProd);
        spCategoria = findViewById(R.id.spCategoria);
        txtCosto = findViewById(R.id.txtCostoProd);
        txtPrecio = findViewById(R.id.txtPrecioProd);
        txtStock = findViewById(R.id.txtStockProd);
        btnCamara = findViewById(R.id.btnCamaraProd);
        btnBuscar = findViewById(R.id.btnBuscarProd);
        btnGuardar = findViewById(R.id.btnGuardarProd);

        // Cargar categorías en el desplegable
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spCategoria.setAdapter(adapterCat);

        // Botón Cámara (ZXing)
        btnCamara.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Escanea el código del producto");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });

        // Botón Buscar, verifica si ya existe apra reabastecer
        btnBuscar.setOnClickListener(v -> buscarProducto());

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarOActualizar());

        //Barra de navegacion
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_productos);
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
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_productos;
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(ProductoActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void buscarProducto() {
        String codigo = txtCodigo.getText().toString().trim();
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa o escanea un código", Toast.LENGTH_SHORT).show();
            return;
        }

        productoExistente = db.buscarPorCodigo(codigo);

        if (productoExistente != null) {
            txtNombre.setText(productoExistente.getNombre());
            txtCosto.setText(String.valueOf(productoExistente.getCosto()));
            txtPrecio.setText(String.valueOf(productoExistente.getPrecioVenta()));
            txtStock.setText("");
            txtStock.setHint("Stock actual: " + productoExistente.getStock() + " (Ingresa cantidad a sumar)");

            // Seleccionar su categoría en el desplegable
            for (int i = 0; i < categorias.length; i++) {
                if (categorias[i].equals(productoExistente.getCategoria())) {
                    spCategoria.setSelection(i);
                    break;
                }
            }

            // Bloqueamos edición de datos base para solo sumar stock
            txtNombre.setEnabled(false);
            spCategoria.setEnabled(false);
            txtCosto.setEnabled(false);
            txtPrecio.setEnabled(false);
            btnGuardar.setText("Sumar Stock");
            Toast.makeText(this, "Producto encontrado: ingresa unidades a agregar", Toast.LENGTH_SHORT).show();
        } else {
            desbloquearCamposNuevo();
            Toast.makeText(this, "Código nuevo: completa los datos para registrarlo", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarOActualizar() {
        String codigo = txtCodigo.getText().toString().trim();
        String nombre = txtNombre.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String strCosto = txtCosto.getText().toString().trim();
        String strPrecio = txtPrecio.getText().toString().trim();
        String strStock = txtStock.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || strCosto.isEmpty() || strPrecio.isEmpty() || strStock.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double costo = Double.parseDouble(strCosto);
            double precio = Double.parseDouble(strPrecio);
            int cantidad = Integer.parseInt(strStock);

            if (costo < 0 || precio <= 0 || cantidad <= 0) {
                Toast.makeText(this, "Ingresa valores mayores a cero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productoExistente != null) {
                // Caso 1: Sumar stock a un producto existente
                int nuevoStock = productoExistente.getStock() + cantidad;
                if (db.actualizarStock(codigo, nuevoStock)) {
                    Toast.makeText(this, "Stock actualizado. Total ahora: " + nuevoStock, Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                }
            } else {
                // Caso 2: Registrar producto nuevo
                Producto nuevo = new Producto(codigo, nombre, categoria, costo, precio, cantidad);
                if (db.guardarProducto(nuevo)) {
                    Toast.makeText(this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                } else {
                    Toast.makeText(this, "Ese código ya existe. Usa el botón Buscar", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato numérico inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void desbloquearCamposNuevo() {
        productoExistente = null;
        txtNombre.setEnabled(true);
        spCategoria.setEnabled(true);
        txtCosto.setEnabled(true);
        txtPrecio.setEnabled(true);
        txtNombre.setText("");
        txtCosto.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        txtStock.setHint("Cantidad / Stock");
        btnGuardar.setText("Guardar Producto");
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        desbloquearCamposNuevo();
        txtCodigo.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            txtCodigo.setText(result.getContents());
            buscarProducto();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // MENÚ DE NAVEGACIÓN
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
        } else if (id == R.id.opInventario) {
            startActivity(new Intent(this, InventarioActivity.class));
            finish();
        } else if (id == R.id.opHistorial) {
            startActivity(new Intent(this, HistorialActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}