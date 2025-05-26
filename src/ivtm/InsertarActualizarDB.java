package ivtm;

import ivtm.IvtmSession;
import ivtm.IvtmTransaction;
import org.hibernate.Session;
import org.hibernate.query.Query;
import POJOS.*;

import java.util.List;
import java.util.Map;

public class InsertarActualizarDB {

    /*Metodo que inserta/actualiza todos los contribuyentes correctos*/
    public void insertaActualizaContribuyentes (Map<String, Contribuyente> contribuyentesPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<String, Contribuyente> entry : contribuyentesPojosMap.entrySet()) {
                Contribuyente cont = entry.getValue();

                //Busca si ya existe en la DB (IdContribuyente)
                Query<Contribuyente> query = s.createQuery(
                        "from Contribuyente c where c.nifnie = :nifnie", Contribuyente.class);

                query.setParameter("nifnie", cont.getNifnie());

                Contribuyente existe = query.uniqueResult();

                //Si el contribuyente ya estaba en la DB, se actualiza
                if (existe != null) {
                    existe.setNombre(cont.getNombre());
                    existe.setApellido1(cont.getApellido1());
                    existe.setApellido2(cont.getApellido2());
                    existe.setNifnie(cont.getNifnie());
                    existe.setDireccion(cont.getDireccion());
                    existe.setNumero(cont.getNumero());
                    existe.setPaisCcc(cont.getPaisCcc());
                    existe.setCcc(cont.getCcc());
                    existe.setIban(cont.getIban());
                    existe.setEmail(cont.getEmail());
                    existe.setBonificacion(cont.getBonificacion());
                    existe.setAyuntamiento(cont.getAyuntamiento());
                    existe.setReciboses(cont.getReciboses());
                    existe.setVehiculoses(cont.getVehiculoses());

                    s.update(existe);
                }
                else {
                    s.save(cont);
                }

                //Manejo de sobrecarga de memoria
                if (++contador % 50 == 0) {
                    s.flush();
                    s.clear();
                }
            }

            trans.commitTrans();
            System.out.println("Contribuyentes insertados/actualizados correctamente en la DB");
        } catch (Exception e) {
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally {
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }

    /*Metodo que inserta/actualiza todos los vehiculos que han generado recibo*/
    public void insertaActualizaVehiculos(Map<String, List<Recibos>> recibosPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<String, List<Recibos>> entry : recibosPojosMap.entrySet()) {
                List<Recibos> listaRecibos = entry.getValue();

                for (Recibos rec : listaRecibos) {
                    Vehiculos v = rec.getVehiculos();

                    // Verificar y asociar Contribuyente persistido
                    Contribuyente contrib = v.getContribuyente();
                    if (contrib != null && contrib.getNifnie() != null) {
                        Query<Contribuyente> contQuery = s.createQuery(
                            "from Contribuyente c where c.nifnie = :nif", Contribuyente.class);
                        contQuery.setParameter("nif", contrib.getNifnie());
                        Contribuyente persistido = contQuery.uniqueResult();

                        if (persistido != null) {
                            v.setContribuyente(persistido);
                        } else {
                            System.out.println("❗ Vehículo con matrícula " + v.getMatricula() + " tiene contribuyente no persistido. Se omite.");
                            continue;
                        }
                    }

                    // Verificar y asociar Ordenanza persistida
                    Ordenanza ord = v.getOrdenanza();
                    if (ord != null) {
                        Query<Ordenanza> ordQuery = s.createQuery(
                            "from Ordenanza o where o.ayuntamiento = :ayto and o.tipoVehiculo = :tipo and o.unidad = :unidad and o.minimoRango = :min and o.maximoRango = :max", Ordenanza.class);
                        ordQuery.setParameter("ayto", ord.getAyuntamiento());
                        ordQuery.setParameter("tipo", ord.getTipoVehiculo());
                        ordQuery.setParameter("unidad", ord.getUnidad());
                        ordQuery.setParameter("min", ord.getMinimoRango());
                        ordQuery.setParameter("max", ord.getMaximoRango());

                        Ordenanza ordPersistida = ordQuery.uniqueResult();
                        if (ordPersistida != null) {
                            v.setOrdenanza(ordPersistida);
                            ordPersistida.getVehiculoses().add(v);
                        } else {
                            System.out.println("❗ Vehículo con matrícula " + v.getMatricula() + " tiene ordenanza no persistida. Se omite.");
                            continue;
                        }
                    }

                    // Buscar si ya existe el vehículo
                    Query<Vehiculos> query = s.createQuery(
                        "from Vehiculos v where v.matricula = :matricula", Vehiculos.class);
                    query.setParameter("matricula", v.getMatricula());

                    Vehiculos existe = query.uniqueResult();

                    if (existe != null) {
                        // Actualizar campos
                        existe.setContribuyente(v.getContribuyente());
                        existe.setOrdenanza(v.getOrdenanza());
                        existe.setTipo(v.getTipo());
                        existe.setMarca(v.getMarca());
                        existe.setModelo(v.getModelo());
                        existe.setMatricula(v.getMatricula());
                        existe.setNumeroBastidor(v.getNumeroBastidor());
                        existe.setCaballosFiscales(v.getCaballosFiscales());
                        existe.setPlazas(v.getPlazas());
                        existe.setCentimetroscubicos(v.getCentimetroscubicos());
                        existe.setKgcarga(v.getKgcarga());
                        existe.setExencion(v.getExencion());
                        existe.setFechaMatriculacion(v.getFechaMatriculacion());
                        existe.setFechaAlta(v.getFechaAlta());
                        existe.setFechaBaja(v.getFechaBaja());
                        existe.setFechaBajaTemporal(v.getFechaBajaTemporal());
                        existe.setReciboses(v.getReciboses());

                        s.update(existe);
                    } else {
                        s.save(v);
                    }

                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

            trans.commitTrans();
            System.out.println("Vehículos insertados/actualizados correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            trans.rollbackTrans();
        } finally {
            if (s != null) s.close();
            FactorySession.closeSessionFactory();
        }
    }



    /*Metodo que inserta/actualiza las ordenanzas aplicadas a los vehículos que generan recibo*/
    public void insertaActualizaOrdenanzas(Map<String, List<Ordenanza>> ordenanzasMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<String, List<Ordenanza>> entry : ordenanzasMap.entrySet()) {
                List<Ordenanza> listaOrdenanzas = entry.getValue();

                for (Ordenanza o : listaOrdenanzas) {
                   

                    //Busca si ya existe en la DB (id)
                    Query<Ordenanza> query = s.createQuery(
                            "from Ordenanza o where o.ayuntamiento = :ayuntamiento and o.tipoVehiculo = :tipo and o.unidad = :unidad and o.minimoRango = :min and o.maximoRango = :max", Ordenanza.class);

                    query.setParameter("ayuntamiento", o.getAyuntamiento());
                    query.setParameter("tipo", o.getTipoVehiculo());
                    query.setParameter("unidad", o.getUnidad());
                    query.setParameter("min", o.getMinimoRango());
                    query.setParameter("max", o.getMaximoRango());

                    Ordenanza existe = query.uniqueResult();

                    //Si la ordenanza ya estaba en la DB, se actualiza
                    if (existe != null) {
                        existe.setAyuntamiento(o.getAyuntamiento());
                        existe.setTipoVehiculo(o.getTipoVehiculo());
                        existe.setUnidad(o.getUnidad());
                        existe.setMinimoRango(o.getMinimoRango());
                        existe.setMaximoRango(o.getMaximoRango());
                        existe.setImporte(o.getImporte());
                        existe.setVehiculoses(o.getVehiculoses());

                        s.update(existe);
                    } else {
                        s.save(o);
                    }

                    //Manejo de sobrecarga de memoria
                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

            trans.commitTrans();
            System.out.println("Ordenanzas insertados/actualizados correctamente en la DB");

        } catch(Exception e){
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally{
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }

    /*Metodo que inserta/actualiza todos los recibos generados*/
    public void insertaActualizaRecibos(Map<String, List<Recibos>> recibosPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<String, List<Recibos>> entry : recibosPojosMap.entrySet()) {
                List<Recibos> listaRecibos = entry.getValue();

                for (Recibos r : listaRecibos) {

                    // Buscar y asociar contribuyente persistido
                    if (r.getContribuyente() != null) {
                        Query<Contribuyente> contribQuery = s.createQuery(
                            "from Contribuyente c where c.nifnie = :nif", Contribuyente.class);
                        contribQuery.setParameter("nif", r.getContribuyente().getNifnie());
                        Contribuyente contribuyentePersistido = contribQuery.uniqueResult();
                        r.setContribuyente(contribuyentePersistido); // puede quedar en null si no existe
                    }

                    // Buscar y asociar vehículo persistido
                    if (r.getVehiculos() != null) {
                        Query<Vehiculos> vehQuery = s.createQuery(
                            "from Vehiculos v where v.matricula = :matricula", Vehiculos.class);
                        vehQuery.setParameter("matricula", r.getVehiculos().getMatricula());
                        Vehiculos vehiculoPersistido = vehQuery.uniqueResult();
                        r.setVehiculos(vehiculoPersistido); // puede quedar en null si no existe
                    }

                    // Buscar si el recibo ya está en la base de datos
                    Query<Recibos> query = s.createQuery(
                        "from Recibos r where r.nifContribuyente = :nif and r.fechaPadron = :fechaPadron and r.marcaModelo = :marcaModelo",
                        Recibos.class);
                    query.setParameter("nif", r.getNifContribuyente());
                    query.setParameter("fechaPadron", r.getFechaPadron());
                    query.setParameter("marcaModelo", r.getMarcaModelo());

                    Recibos existe = query.getResultList().stream().findFirst().orElse(null);

                    if (existe != null) {
                        // Actualización de campos
                        existe.setContribuyente(r.getContribuyente());
                        existe.setVehiculos(r.getVehiculos());
                        existe.setFechaPadron(r.getFechaPadron());
                        existe.setFechaRecibo(r.getFechaRecibo());
                        existe.setNifContribuyente(r.getNifContribuyente());
                        existe.setDireccionCompleta(r.getDireccionCompleta());
                        existe.setIban(r.getIban());
                        existe.setTipoVehiculo(r.getTipoVehiculo());
                        existe.setMarcaModelo(r.getMarcaModelo());
                        existe.setUnidad(r.getUnidad());
                        existe.setValorUnidad(r.getValorUnidad());
                        existe.setTotalRecibo(r.getTotalRecibo());
                        existe.setExencion(r.getExencion());
                        existe.setBonificacion(r.getBonificacion());
                        existe.setEmail(r.getEmail());
                        existe.setAyuntamiento(r.getAyuntamiento());

                        s.update(existe);
                    } else {
                        r.setNumRecibo(contador + 1);
                        s.save(r);
                    }

                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

            trans.commitTrans();
            System.out.println("Recibos insertados/actualizados correctamente en la DB");

        } catch (Exception e) {
            e.printStackTrace();
            trans.rollbackTrans();
        } finally {
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }

}
