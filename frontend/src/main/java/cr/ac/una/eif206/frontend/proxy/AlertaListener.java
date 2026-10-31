package cr.ac.una.eif206.frontend.proxy;

import cr.ac.una.eif206.common.protocolo.Notificacion;

/**
 * Quien quiera enterarse de las alertas implementa esta interfaz.
 * BENJA: su AlertasController implementa AlertaListener.
 *
 * alRecibir(...) siempre se llama en el hilo de Swing, asi que ahi se puede
 * actualizar la pantalla directamente.
 */
public interface AlertaListener {

    void alRecibir(Notificacion notificacion);
}