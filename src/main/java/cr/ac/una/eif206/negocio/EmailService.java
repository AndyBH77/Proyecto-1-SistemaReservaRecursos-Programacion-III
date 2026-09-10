package cr.ac.una.eif206.negocio;

import cr.ac.una.eif206.util.ConfigManager;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Servicio encargado de enviar correos reales usando una cuenta de Gmail
 * por SMTP. Los datos de la cuenta (usuario y "contraseña de aplicacion")
 * se leen de config.properties, NUNCA se dejan escritos en el codigo.
 */
public class EmailService {

    public void enviarClaveGenerada(String correoDestino, String nombreUsuario, String claveGenerada)
            throws MessagingException {

        String usuarioGmail = ConfigManager.obtener("gmail.usuario");
        String claveAppGmail = ConfigManager.obtener("gmail.password");

        if (usuarioGmail.isBlank() || claveAppGmail.isBlank()) {
            throw new MessagingException("Falta configurar gmail.usuario y gmail.password en config.properties");
        }

        Properties propiedadesSmtp = new Properties();
        propiedadesSmtp.put("mail.smtp.auth", "true");
        propiedadesSmtp.put("mail.smtp.starttls.enable", "true");
        propiedadesSmtp.put("mail.smtp.host", "smtp.gmail.com");
        propiedadesSmtp.put("mail.smtp.port", "587");

        Session sesion = Session.getInstance(propiedadesSmtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuarioGmail, claveAppGmail);
            }
        });

        Message mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(usuarioGmail));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        mensaje.setSubject("Sistema de Reservas - Su nueva clave de acceso");

        String cuerpo = "Hola " + nombreUsuario + ",\n\n"
                + "Su clave de acceso al Sistema de Reservas es:\n\n"
                + "    " + claveGenerada + "\n\n"
                + "Le recomendamos cambiarla despues de ingresar por primera vez.\n\n"
                + "Este es un correo automatico, por favor no responder.";

        mensaje.setText(cuerpo);

        Transport.send(mensaje);
    }
}
