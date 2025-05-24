/*package ivtm;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

public class ActualizarBD{
    
    public void insertarContribuyente(Connection conexion,int idContribuyente, String nombre, String apellido1, String apellido2, String nifNie, String direccion, String numero, String paisCCC, String CCC, String IBAN, String email, double bonificacion, String ayuntamiento){
        String selectSQL = "SELECT idContribuyente FROM contribuyente WHERE nombre = ? AND apellido1 = ? AND apellido2 = ? AND nifnie = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido1);
            stmt.setString(3, apellido2);
            stmt.setString(4, nifNie);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idContribuyente");
                String updateSQL = "UPDATE contribuyente SET direccion = ?, numero = ?, paisCCC = ?, CCC = ?, IBAN = ?, email = ?, bonificacion = ?, ayuntamiento = ? WHERE idContribuyente = ?";
                try (PreparedStatement updateStmt = conexion.prepareStatement(updateSQL)) {
                    updateStmt.setString(1, direccion);
                    updateStmt.setString(2, numero);
                    updateStmt.setString(3, paisCCC);
                    updateStmt.setString(4, CCC);
                    updateStmt.setString(5, IBAN);
                    updateStmt.setString(6, email);
                    updateStmt.setDouble(7, bonificacion);
                    updateStmt.setString(8, ayuntamiento);
                    updateStmt.executeUpdate();
                }
                return;
            }
        }

        // Insertar si no existe
        String insertSQL = "INSERT INTO contribuyente (idContribuyente, nombre, apellido1, apellido2, nifNie, direccion, numero, paisCCC, CCC, IBAN, email, bonificacion, ayuntamiento) " +
                        "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, idContribuyente);
            stmt.setString(2, nombre);
            stmt.setString(3, apellido1);
            stmt.setString(4, apellido2);
            stmt.setString(5, nifNie);
            stmt.setString(6, direccion);
            stmt.setString(7, numero);
            stmt.setString(8, paisCCC);
            stmt.setString(9, CCC);
            stmt.setString(10, IBAN);
            stmt.setString(11, email);
            stmt.setDouble(12, bonificacion);
            stmt.setString(13, ayuntamiento);

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
    }
    public void insertarVehiculo(Connection  conexion, int idVehiculo, String tipo,String marca,String modelo,String matricula,String numeroBastidor,double caballosFiscales,double plazas,double centimetrosCubicos,double kgCarga,String exencion,Date fechaMatriculacion,Date fechaAlta,Date fechaBaja,Date fechaBajaTemporal,int idContribuyente,int idOrdenanza){
        String selectSQL = "SELECT idVehiculo FROM vehiculos WHERE matricula = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setString(1, matricula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idVehiculo");
                String updateSQL = "UPDATE vehiculos SET tipo = ?, marca = ?, modelo = ?, numeroBastidor = ?, caballosFiscales = ?, plazas = ?, centimetroscubicos = ?, kgcarga = ?, exencion = ?, fechaMatriculacion = ?, fechaAlta = ?, fechaBaja = ?, fechaBajaTemporal = ?, idContribuyente = ?, idOrdenanza = ? WHERE idVehiculo = ?";
                try (PreparedStatement updateStmt = conexion.prepareStatement(updateSQL)) {
                    updateStmt.setInt(1, tipo);
                    updateStmt.setString(2, marca);
                    updateStmt.setString(3, modelo);
                    updateStmt.setString(4, numeroBastidor);
                    updateStmt.setDouble(5, caballosFiscales);
                    updateStmt.setDouble(6, plazas);
                    updateStmt.setDouble(7, centimetrosCubicos);
                    updateStmt.setDouble(8, kgCarga);
                    updateStmt.setString(9, exencion);
                    updateStmt.setDate(10, fechaMatriculacion);
                    updateStmt.setDate(11, fechaAlta);
                    updateStmt.setDate(12, fechaBaja);
                    updateStmt.setDate(13, fechaBajaTemporal);
                    updateStmt.setInt(14, idContribuyente);
                    updateStmt.setInt(15, idOrdenanza);
                    updateStmt.setInt(16, id);
                    updateStmt.executeUpdate();
                }
                return;
            }
        }
        String insertSQL = "INSERT INTO vehiculos (idVehiculo, tipo, marca, modelo, matricula, numeroBastidor, caballosFiscales, plazas, centimetroscubicos, kgcarga, exencion, fechaMatriculacion, fechaAlta, fechaBaja, fechaBajaTemporal, idContribuyente, idOrdenanza) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idVehiculo);
            stmt.setString(2, tipo);
            stmt.setString(3, marca);
            stmt.setString(4, modelo);
            stmt.setString(5, matricula);
            stmt.setString(6, numeroBastidor);
            stmt.setDouble(7, caballosFiscales);
            stmt.setDouble(8, plazas);
            stmt.setDouble(9, centimetrosCubicos);
            stmt.setDouble(10, kgCarga);
            stmt.setString(11, exencion);
            stmt.setDate(12, fechaMatriculacion);
            stmt.setDate(13, fechaAlta);
            stmt.setDate(14, fechaBaja);
            stmt.setDate(15, fechaBajaTemporal);
            stmt.setInt(16, idContribuyente);
            stmt.setInt(17, idOrdenanza);

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
    }
    public void insertarOrdenanza(Connection conexion, int id, String ayuntamiento,String tipoVehiculo,String unidad,String minimoRango,String maximoRango,double importe){
        String selectSQL = "SELECT id, importe FROM ordenanza WHERE ayuntamiento = ? AND tipoVehiculo = ? AND unidad = ? AND minimo_rango = ? AND maximo_rango = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(selectSQL)) {
            stmt.setString(1, ayuntamiento);
            stmt.setString(2, tipoVehiculo);
            stmt.setString(3, unidad);
            stmt.setString(4, minimoRango);
            stmt.setString(5, maximoRango);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                double importeActual = rs.getDouble("importe");
                if (Double.compare(importeActual, importe) != 0) {
                    String updateSQL = "UPDATE ordenanza SET importe = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conexion.prepareStatement(updateSQL)) {
                        updateStmt.setDouble(1, importe);
                        updateStmt.executeUpdate();
                    }
                }
                return;
            }
        }
        String insertSQL = "INSERT INTO ordenanza (id, ayuntamiento, tipoVehiculo, unidad, minimo_rango, maximo_rango, importe) VALUES (?,?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(insertSQL)) {
            stmt.setInt(1, id);
            stmt.setString(2, ayuntamiento);
            stmt.setString(3, tipoVehiculo);
            stmt.setString(4, unidad);
            stmt.setString(5, minimoRango);
            stmt.setString(6, maximoRango);
            stmt.setDouble(7, importe);
            stmt.executeUpdate();
        }
    } 
    public void insertarRecibo(Connection conexion, int numRecibo, Date fechaPadron,Date fechaRecibo,String nifContribuyente,String direccionCompleta,String IBAN,String tipoVehiculo,String marcaModelo,String unidad,double valorUnidad,double totalRecibo,String exencion,double bonificacion,String email,String ayuntamiento,int idContribuyente,int idVehiculo){
        String selectSQL = "SELECT numRecibo FROM recibos WHERE nifContribuyente = ? AND fechaPadron = ? AND idVehiculo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(selectSQL)) {
            stmt.setString(1, nifContribuyente);
            stmt.setDate(2, fechaPadron);
            stmt.setInt(3, idVehiculo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Ya existe, actualizamos
                int numRecibo = rs.getInt("numRecibo");
                String updateSQL = "UPDATE recibos SET fechaRecibo = ?, direccionCompleta = ?, IBAN = ?, tipoVehiculo = ?, marcaModelo = ?, unidad = ?, valorUnidad = ?, totalRecibo = ?, exencion = ?, bonificacion = ?, email = ?, ayuntamiento = ?, idContribuyente = ? WHERE numRecibo = ?";
                try (PreparedStatement updateStmt = conexion.prepareStatement(updateSQL)) {
                    updateStmt.setDate(1, fechaRecibo);
                    updateStmt.setString(2, direccionCompleta);
                    updateStmt.setString(3, IBAN);
                    updateStmt.setString(4, tipoVehiculo);
                    updateStmt.setString(5, marcaModelo);
                    updateStmt.setString(6, unidad);
                    updateStmt.setDouble(7, valorUnidad);
                    updateStmt.setDouble(8, totalRecibo);
                    updateStmt.setString(9, exencion);
                    updateStmt.setDouble(10, bonificacion);
                    updateStmt.setString(11, email);
                    updateStmt.setString(12, ayuntamiento);
                    updateStmt.setInt(13, idContribuyente);
                    updateStmt.setInt(14, numRecibo);
                    updateStmt.executeUpdate();
                }
                return;
            }
        }
        String insertSQL = "INSERT INTO recibos (numRecibo, fechaPadron, fechaRecibo, nifContribuyente, direccionCompleta, IBAN, tipoVehiculo, marcaModelo, unidad, valorUnidad, totalRecibo, exencion, bonificacion, email, ayuntamiento, idContribuyente, idVehiculo) " +
                        "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(insertSQL)) {
            stmt.setInt(1,numRecibo);
            stmt.setDate(2, fechaPadron);
            stmt.setDate(3, fechaRecibo);
            stmt.setString(4, nifContribuyente);
            stmt.setString(5, direccionCompleta);
            stmt.setString(6, IBAN);
            stmt.setString(7, tipoVehiculo);
            stmt.setString(8, marcaModelo);
            stmt.setString(9, unidad);
            stmt.setDouble(10, valorUnidad);
            stmt.setDouble(11, totalRecibo);
            stmt.setString(12, exencion);
            stmt.setDouble(13, bonificacion);
            stmt.setString(14, email);
            stmt.setString(15, ayuntamiento);
            stmt.setInt(16, idContribuyente);
            stmt.setInt(17, idVehiculo);
            stmt.executeUpdate();
        }   
    }

}*/
 