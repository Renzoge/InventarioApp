package com.renzcode.inventarioapp;

public class Producto {
    private String codigo;
    private String nombre;
    private String categoria;
    private double costo;
    private double precioVenta;
    private int stock;

    public Producto(String codigo, String nombre, String categoria, double costo, double precioVenta, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.stock = stock;
    }

    public double getGananciaUnitaria() {
        return precioVenta - costo;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}