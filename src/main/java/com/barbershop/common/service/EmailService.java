package com.barbershop.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);  // Configura el destinatario del correo
        mensaje.setSubject(asunto);   // Configura el asunto del correo
        mensaje.setText(cuerpo);      // Configura el contenido del mensaje
        mensaje.setFrom("tuemail@gmail.com"); // Define quién envía el correo

        mailSender.send(mensaje); // Envía el correo
    }

    public void enviarCorreoHtml(String destinatario, String asunto, String htmlContenido) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(htmlContenido, true); // El segundo parámetro indica que es HTML
        helper.setFrom("tuemail@gmail.com");

        mailSender.send(mensaje);
    }
}