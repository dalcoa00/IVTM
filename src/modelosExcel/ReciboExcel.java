package modelosExcel;

import java.util.Date;

public class ReciboExcel {
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nifnie;
    private String direccion;
    private String iban;
    private String bonificacion;

    private Date fechaRecibo;
    private Date fechaPadron;

    private String tipoVehiculo;
    private String marca;
    private String modelo;
    private String matricula;
    private String bastidor;
    private String unidadCobro;
    private double valorUnidad;
    private double importe;
    private double exenc_bonif;
    private double total;

    // Constructor: establece la fecha actual como fechaRecibo
    public ReciboExcel() {
        this.fechaRecibo = new Date();
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido1() { return apellido1; }
    public void setApellido1(String apellido1) { this.apellido1 = apellido1; }

    public String getApellido2() { return apellido2; }
    public void setApellido2(String apellido2) { this.apellido2 = apellido2; }

    public String getNifnie() { return nifnie; }
    public void setNifnie(String nifnie) { this.nifnie = nifnie; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getBonificacion() { return bonificacion; }
    public void setBonificacion(String bonificacion) { this.bonificacion = bonificacion; }

    public Date getFechaRecibo() { return fechaRecibo; }
    public void setFechaRecibo(Date fechaRecibo) { this.fechaRecibo = fechaRecibo; }

    public Date getFechaPadron() { return fechaPadron; }
    public void setFechaPadron(Date fechaPadron) { this.fechaPadron = fechaPadron; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getBastidor() { return bastidor; }
    public void setBastidor(String bastidor) { this.bastidor = bastidor; }

    public String getUnidadCobro() { return unidadCobro; }
    public void setUnidadCobro(String unidadCobro) { this.unidadCobro = unidadCobro; }

    public double getValorUnidad() { return valorUnidad; }
    public void setValorUnidad(double valorUnidad) { this.valorUnidad = valorUnidad; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public double getExenc_bonif() { return exenc_bonif; }
    public void setExenc_bonif(double exenc_bonif) { this.exenc_bonif = exenc_bonif; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
