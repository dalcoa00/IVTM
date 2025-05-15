package modelosExcel;

import java.util.Date;

public class VehiculoExcel {
    private int idFila;
    private String nifPropietario;
    private String tipoVehiculo;
    private String marca;
    private String modelo;
    private String matricula;
    private String bastidor;
    private Date fechaAlta;
    private Date fechaBaja;
    private Date fechaBajaTemporal;
    //1=CVs, 2=Plazas, 3=KGs, 4=CCs
    private int unidadCobro;
    //nº cvs, plazas, kgs o ccs
    private double valorUnidad;
    private double importe; //Se añade al calcularlo leyendo Ordenanza
    private Character exencion;
    private double total; //Tras leer ordenanza se calcula

    public int getIdFila() {
        return idFila;
    }

    public void setIdFila(int idFila) {
        this.idFila = idFila;
    }

    public String getNifPropietario() {
        return nifPropietario;
    }

    public void setNifPropietario(String nifPropietario) {
        this.nifPropietario = nifPropietario;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getBastidor() {
        return bastidor;
    }

    public void setBastidor(String bastidor) {
        this.bastidor = bastidor;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Date getFechaBajaTemporal() {
        return fechaBajaTemporal;
    }

    public void setFechaBajaTemporal(Date fechaBajaTemporal) {
        this.fechaBajaTemporal = fechaBajaTemporal;
    }

    public int getUnidadCobro() {
        return unidadCobro;
    }

    public void setUnidadCobro(int unidadCobro) {
        this.unidadCobro = unidadCobro;
    }

    public double getValorUnidad() {
        return valorUnidad;
    }

    public void setValorUnidad(double valorUnidad) {
        this.valorUnidad = valorUnidad;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public Character getExencion() {
        return exencion;
    }

    public void setExenc_bonif(Character exencion) {
        this.exencion = exencion;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
