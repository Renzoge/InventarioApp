package com.renzcode.inventarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText txtCodigo, txtCantidad;
    private TextView lblNombre, lblCategoria, lblPrecio, lblStock, lblTotal;
    private ImageButton btnCamara;
    private Button btnBuscar, btnConfirmar;

    private BaseDatos db;
    private Producto productoActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new BaseDatos(this);

        txtCodigo = findViewById(R.id.txtCodigoVenta);
        txtCantidad = findViewById(R.id.txtCantidadVenta);
        lblNombre = findViewById(R.id.lblNombreVenta);
        lblCategoria = findViewById(R.id.lblCategoriaVenta);
        lblPrecio = findViewById(R.id.lblPrecioVenta);
        lblStock = findViewById(R.id.lblStockVenta);
        lblTotal = findViewById(R.id.lblTotalVenta);
        btnCamara = findViewById(R.id.btnCamaraVenta);
        btnBuscar = findViewById(R.id.btnBuscarVenta);
        btnConfirmar = findViewById(R.id.btnConfirmarVenta);

        // Botón Cámara
        btnCamara.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Escanea el producto a vender");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });

        // Botón Buscar
        btnBuscar.setOnClickListener(v -> buscarProducto());

        // Calcular el total cada vez que cambia el número en cantidad
        txtCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón Confirmar Venta
        btnConfirmar.setOnClickListener(v -> procesarVenta());

        // BARRA DE NAVEGACIÓN INFERIOR
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_vender);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventario) {
                startActivity(new Intent(this, InventarioActivity.class));
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
            return id == R.id.nav_vender;
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Salir de la aplicación")
                        .setMessage("¿Seguro que deseas cerrar el Punto de Venta?")
                        .setPositiveButton("Sí, salir", (dialog, which) -> finish())
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    private void buscarProducto() {
        String codigo = txtCodigo.getText().toString().trim();
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa o escanea un código", Toast.LENGTH_SHORT).show();
            return;
        }

        productoActual = db.buscarPorCodigo(codigo);

        if (productoActual == null) {
            limpiarVistaProducto();
            // Alerta clara si el código no está registrado
            new AlertDialog.Builder(this)
                    .setTitle("Producto no encontrado")
                    .setMessage("El código \"" + codigo + "\" no está registrado en el inventario.")
                    .setPositiveButton("Aceptar", null)
                    .show();
        } else {
            lblNombre.setText("Producto: " + productoActual.getNombre());
            lblCategoria.setText("Categoría: " + productoActual.getCategoria());
            lblPrecio.setText(String.format(Locale.US, "Precio Unitario: S/ %.2f", productoActual.getPrecioVenta()));
            lblStock.setText("Stock disponible: " + productoActual.getStock());
            txtCantidad.setText("1");
            txtCantidad.requestFocus();
            calcularTotal();
        }
    }

    private void calcularTotal() {
        if (productoActual == null) {
            lblTotal.setText("Total a cobrar: S/ 0.00");
            return;
        }
        String strCant = txtCantidad.getText().toString().trim();
        if (strCant.isEmpty()) {
            lblTotal.setText("Total a cobrar: S/ 0.00");
            return;
        }
        try {
            int cant = Integer.parseInt(strCant);
            double total = cant * productoActual.getPrecioVenta();
            lblTotal.setText(String.format(Locale.US, "Total a cobrar: S/ %.2f", total));
        } catch (NumberFormatException e) {
            lblTotal.setText("Total a cobrar: S/ 0.00");
        }
    }

    private void procesarVenta() {
        if (productoActual == null) {
            Toast.makeText(this, "Primero busca un producto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String strCant = txtCantidad.getText().toString().trim();
        if (strCant.isEmpty()) {
            Toast.makeText(this, "Ingresa la cantidad a vender", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cantidad = Integer.parseInt(strCant);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cantidad > productoActual.getStock()) {
                new AlertDialog.Builder(this)
                        .setTitle("Stock insuficiente")
                        .setMessage("Solo quedan " + productoActual.getStock() + " unidades disponibles de este producto.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }

            double total = cantidad * productoActual.getPrecioVenta();
            int nuevoStock = productoActual.getStock() - cantidad;
            String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            // Descontar stock en tabla productos
            boolean stockActualizado = db.actualizarStock(productoActual.getCodigo(), nuevoStock);
            // Guardar registro en tabla ventas
            Venta nuevaVenta = new Venta(productoActual.getCodigo(), productoActual.getNombre(), cantidad, total, fechaActual);
            boolean ventaGuardada = db.registrarVenta(nuevaVenta);

            if (stockActualizado && ventaGuardada) {
                Toast.makeText(this, String.format(Locale.US, "Venta registrada por S/ %.2f", total), Toast.LENGTH_LONG).show();
                txtCodigo.setText("");
                limpiarVistaProducto();
                txtCodigo.requestFocus();
            } else {
                Toast.makeText(this, "Error al procesar la venta", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarVistaProducto() {
        productoActual = null;
        lblNombre.setText("Producto: ---");
        lblCategoria.setText("Categoría: ---");
        lblPrecio.setText("Precio Unitario: S/ 0.00");
        lblStock.setText("Stock disponible: 0");
        txtCantidad.setText("");
        lblTotal.setText("Total a cobrar: S/ 0.00");
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

}