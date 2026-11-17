package com.renzcode.inventarioapp;

public class Venta {

        private int idVenta;
        private String codigoProducto;
        private String nombreProducto;
        private int cantidad;
        private double totalCobrado;
        private String fecha;

        public Venta(int idVenta, String codigoProducto, String nombreProducto, int cantidad, double totalCobrado, String fecha) {
            this.idVenta = idVenta;
            this.codigoProducto = codigoProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.totalCobrado = totalCobrado;
            this.fecha = fecha;
        }

        public Venta(String codigoProducto, String nombreProducto, int cantidad, double totalCobrado, String fecha) {
            this.codigoProducto = codigoProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.totalCobrado = totalCobrado;
            this.fecha = fecha;
        }

        public int getIdVenta() { return idVenta; }
        public String getCodigoProducto() { return codigoProducto; }
        public String getNombreProducto() { return nombreProducto; }
        public int getCantidad() { return cantidad; }
        public double getTotalCobrado() { return totalCobrado; }
        public String getFecha() { return fecha; }

}
