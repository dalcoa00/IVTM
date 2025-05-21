package ivtm;

import java.util.Calendar;
import java.util.Date;

public class Trimestres {
    private final int anio;

    public Trimestres (int anio) {
        this.anio = anio;
    }

    public Date getInicio (int trimestre) {
        return getFechaInicioFin(trimestre, true);
    }

    public Date getFin (int trimestre) {
        return getFechaInicioFin(trimestre, false);
    }

    private Date getFechaInicioFin (int trimestre, boolean inicio) {
        Calendar cal = Calendar.getInstance();

        //Establecer mes del trimestre
        int mes = (trimestre - 1) * 3 + (inicio ? 0 : 2); //Mes inicial o final del trimestre
        cal.set(Calendar.YEAR, anio);
        cal.set(Calendar.MONTH, mes);

        //Establecer el día del mes
        if (inicio) {
            //Primer día del mes
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        else {
            //Día final del mes
            int ultimo = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, ultimo);
        }

        return cal.getTime();
    }

    public int calculaTrimestresVehiculo (Date fechaAlta, Date fechaBaja) {
        int trimestres = 0;

        // Si no hay baja, se considera alta hasta el final del año
        if (fechaBaja == null) {
            Calendar cal = Calendar.getInstance();
            cal.set(anio, Calendar.DECEMBER, 31);
            fechaBaja = cal.getTime();
        }

        for (int i = 1; i <= 4; i++) {
            Date inicioTrimestre = getInicio(i);
            Date finTrimestre = getFin(i);

            // El trimestre se paga si hay solapamiento de fechas
            boolean solapa = !fechaBaja.before(inicioTrimestre) && !fechaAlta.after(finTrimestre);

            if (solapa) {
                trimestres++;
            }
        }

        return trimestres;
    }
}
