package modelosExcel;

import java.util.Date;

public class ReciboExcel {
    private int numRecibo;
    private ContribuyenteExcel contribuyente;
    private VehiculoExcel vehiculo;
    private String nifPropietario;
    private Date fechaRecibo;
    private Date fechaPadron;
    private String unidadCobro;
    private double totalRecibo;

    // Constructor: establece la fecha actual como fechaRecibo
    public ReciboExcel() {
        this.fechaRecibo = new Date();
    }

    // Getters y Setters
    public int getNumRecibo() { return numRecibo; }

    public void setNumRecibo(int numRecibo) { this.numRecibo = numRecibo; }

    public ContribuyenteExcel getContribuyente() { return contribuyente; }

    public void setContribuyente(ContribuyenteExcel contribuyente) { this.contribuyente = contribuyente; }

    public VehiculoExcel getVehiculo() { return vehiculo; }

    public void setVehiculo(VehiculoExcel vehiculo) { this.vehiculo = vehiculo; }

    public String getNifPropietario() { return nifPropietario; }

    public void setNifPropietario(String nif) { this.nifPropietario = nif; }

    public Date getFechaRecibo() { return fechaRecibo; }

    public void setFechaRecibo(Date fechaRecibo) { this.fechaRecibo = fechaRecibo; }

    public Date getFechaPadron() { return fechaPadron; }

    public void setFechaPadron(Date fechaPadron) { this.fechaPadron = fechaPadron; }

    public String getUnidadCobro() { return unidadCobro; }

    public void setUnidadCobro(int unidad) {
        String unidadCobro;

        //Paso la unidad de cobro a String para imprimir
        switch (unidad) {
            case 1:
                unidadCobro = "CABALLOS";
                break;
            case 2:
                unidadCobro = "PLAZAS";
                break;
            case 3:
                unidadCobro = "KG";
                break;
            case 4:
                unidadCobro = "CC";
                break;
            default:
                unidadCobro = "No definido";
                break;
        }

        this.unidadCobro = unidadCobro;
    }

    public double getTotalRecibo() { return totalRecibo; }

    public void setTotalRecibo(double totalRecibo) { this.totalRecibo = totalRecibo; }
}
