package ivtm;

public class Recibo {
    private int idRecibo;
    private String exencion;
    private int idFilaExcelVehiculo;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String nif;
    private String iban;
    private String tipoVehiculo;
    private String marcaModelo;
    private String matricula;
    private double totalRecibo;

    // --- Getters ---
    public int getIdRecibo() {
        return idRecibo;
    }

    public String getExencion() {
        return exencion;
    }

    public int getIdFilaExcelVehiculo() {
        return idFilaExcelVehiculo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public String getNif() {
        return nif;
    }

    public String getIban() {
        return iban;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public double getTotalRecibo() {
        return totalRecibo;
    }
}
