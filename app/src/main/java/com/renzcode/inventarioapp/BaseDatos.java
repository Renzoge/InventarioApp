package com.renzcode.inventarioapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class BaseDatos extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "ferreteria.db";
    private static final int VERSION_BD = 1;

    public BaseDatos(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla 1: Productos de la ferretería
        db.execSQL("CREATE TABLE productos (" +
                "codigo TEXT PRIMARY KEY, " +
                "nombre TEXT, " +
                "categoria TEXT, " +
                "costo REAL, " +
                "precio_venta REAL, " +
                "stock INTEGER)");

        // Tabla 2: Historial de ventas
        db.execSQL("CREATE TABLE ventas (" +
                "id_venta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "codigo_producto TEXT, " +
                "nombre_producto TEXT, " +
                "cantidad INTEGER, " +
                "total_cobrado REAL, " +
                "fecha TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ventas");
        db.execSQL("DROP TABLE IF EXISTS productos");
        onCreate(db);
    }

    // ==================== MÉTODOS DE PRODUCTOS ====================

    // Guarda un producto nuevo (devuelve true si tuvo éxito, false si el código ya existía)
    public boolean guardarProducto(Producto p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("codigo", p.getCodigo());
        valores.put("nombre", p.getNombre());
        valores.put("categoria", p.getCategoria());
        valores.put("costo", p.getCosto());
        valores.put("precio_venta", p.getPrecioVenta());
        valores.put("stock", p.getStock());

        long resultado = db.insert("productos", null, valores);
        return resultado != -1;
    }

    // Busca un producto por su código exacto (si no existe, devuelve null)
    public Producto buscarPorCodigo(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM productos WHERE codigo = ?", new String[]{codigo});

        Producto producto = null;
        if (cursor.moveToFirst()) {
            producto = new Producto(
                    cursor.getString(0), // codigo
                    cursor.getString(1), // nombre
                    cursor.getString(2), // categoria
                    cursor.getDouble(3), // costo
                    cursor.getDouble(4), // precio_venta
                    cursor.getInt(5)     // stock
            );
        }
        cursor.close();
        return producto;
    }

    // Lista todos los productos (o filtra por texto si se envía algo en filtro)
    public ArrayList<Producto> listarProductos(String filtro) {
        ArrayList<Producto> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String parametro = "%" + filtro + "%";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM productos WHERE codigo LIKE ? OR nombre LIKE ? OR categoria LIKE ? ORDER BY nombre ASC",
                new String[]{parametro, parametro, parametro}
        );

        while (cursor.moveToNext()) {
            lista.add(new Producto(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getInt(5)
            ));
        }
        cursor.close();
        return lista;
    }

    // Actualiza el stock (sirve tanto para sumar al reabastecer como para restar al vender)
    public boolean actualizarStock(String codigo, int nuevoStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("stock", nuevoStock);

        int filas = db.update("productos", valores, "codigo = ?", new String[]{codigo});
        return filas > 0;
    }

    // Elimina un producto del inventario
    public boolean eliminarProducto(String codigo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete("productos", "codigo = ?", new String[]{codigo});
        return filas > 0;
    }

    // ==================== MÉTODOS DE VENTAS ====================

    // Registra una venta en la tabla ventas
    public boolean registrarVenta(Venta v) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("codigo_producto", v.getCodigoProducto());
        valores.put("nombre_producto", v.getNombreProducto());
        valores.put("cantidad", v.getCantidad());
        valores.put("total_cobrado", v.getTotalCobrado());
        valores.put("fecha", v.getFecha());

        long resultado = db.insert("ventas", null, valores);
        return resultado != -1;
    }

    // Lista todas las ventas desde la más reciente hasta la más antigua
    public ArrayList<Venta> listarVentas() {
        ArrayList<Venta> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ventas ORDER BY id_venta DESC", null);

        while (cursor.moveToNext()) {
            lista.add(new Venta(
                    cursor.getInt(0),    // id_venta
                    cursor.getString(1), // codigo_producto
                    cursor.getString(2), // nombre_producto
                    cursor.getInt(3),    // cantidad
                    cursor.getDouble(4), // total_cobrado
                    cursor.getString(5)  // fecha
            ));
        }
        cursor.close();
        return lista;
    }

    // Elimina una venta específica del historial por su ID
    public boolean eliminarVenta(int idVenta) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete("ventas", "id_venta = ?", new String[]{String.valueOf(idVenta)});
        return filas > 0;
    }
}
