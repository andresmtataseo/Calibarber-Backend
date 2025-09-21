package com.barbershop.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;


    public void enviarCorreoHtml(String destinatario, String asunto, String htmlContenido) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(htmlContenido, true);
        mailSender.send(mensaje);
    }

    public void enviarTokenRecuperacion(String destinatario, String nombreUsuario, String token, int tiempoExpiracion) throws MessagingException {
        String asunto = "Recuperación de Contraseña - Calibarber";
        String htmlContenido = construirHtmlRecuperacion(nombreUsuario, token, tiempoExpiracion);
        enviarCorreoHtml(destinatario, asunto, htmlContenido);
    }

    public void enviarEmailBienvenida(String destinatario, String nombreUsuario) throws MessagingException {
        String asunto = "¡Bienvenido a Calibarber Barbershop!";
        String htmlContenido = construirHtmlBienvenida(nombreUsuario);
        enviarCorreoHtml(destinatario, asunto, htmlContenido);
    }

    private String construirHtmlRecuperacion(String nombreUsuario, String token, int tiempoExpiracion) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #2c2c2c; 
                        background-color: #f5f5f5;
                    }
                    .email-wrapper { 
                        background-color: #f5f5f5; 
                        padding: 40px 20px; 
                        min-height: 100vh; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background-color: #ffffff; 
                        border-radius: 12px; 
                        overflow: hidden; 
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #1a1a1a 0%%, #2d2d2d 100%%); 
                        color: #ffffff; 
                        padding: 40px 30px; 
                        text-align: center; 
                        position: relative;
                    }
                    .header::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%%23ffffff" opacity="0.05"/><circle cx="75" cy="75" r="1" fill="%%23ffffff" opacity="0.05"/><circle cx="50" cy="10" r="0.5" fill="%%23ffffff" opacity="0.03"/></pattern></defs><rect width="100" height="100" fill="url(%%23grain)"/></svg>');
                        opacity: 0.3;
                    }
                    .logo { 
                        font-size: 32px; 
                        font-weight: 700; 
                        letter-spacing: 2px; 
                        margin-bottom: 8px;
                        position: relative;
                        z-index: 1;
                    }
                    .subtitle { 
                        font-size: 16px; 
                        font-weight: 300; 
                        opacity: 0.9; 
                        letter-spacing: 1px;
                        position: relative;
                        z-index: 1;
                    }
                    .content { 
                        padding: 40px 30px; 
                        background-color: #ffffff; 
                    }
                    .greeting { 
                        font-size: 18px; 
                        margin-bottom: 25px; 
                        color: #2c2c2c; 
                    }
                    .message { 
                        font-size: 16px; 
                        margin-bottom: 30px; 
                        color: #555555; 
                        line-height: 1.7; 
                    }
                    .token-container { 
                        background: linear-gradient(135deg, #d4af37 0%%, #f4d03f 100%%); 
                        color: #1a1a1a; 
                        padding: 25px 20px; 
                        text-align: center; 
                        font-size: 20px; 
                        font-weight: 700; 
                        margin: 30px 0; 
                        border-radius: 8px; 
                        letter-spacing: 2px;
                        box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
                        border: 2px solid #d4af37;
                        word-break: break-all;
                        font-family: 'Courier New', monospace;
                    }

                    .warning { 
                        background-color: #8b4513; 
                        color: #ffffff; 
                        padding: 18px 20px; 
                        border-radius: 8px; 
                        margin: 25px 0; 
                        text-align: center;
                        font-weight: 500;
                        border-left: 4px solid #d4af37;
                    }
                    .instructions { 
                        background-color: #f8f9fa; 
                        padding: 25px; 
                        border-radius: 8px; 
                        margin: 25px 0;
                        border-left: 4px solid #d4af37;
                    }
                    .instructions h3 { 
                        color: #2c2c2c; 
                        margin-bottom: 15px; 
                        font-size: 16px; 
                        font-weight: 600;
                    }
                    .instructions ol { 
                        padding-left: 20px; 
                        color: #555555; 
                    }
                    .instructions li { 
                        margin-bottom: 8px; 
                        font-size: 15px; 
                    }
                    .reset-link { 
                        background: linear-gradient(135deg, #d4af37 0%%, #f4d03f 100%%); 
                        color: #1a1a1a; 
                        padding: 15px 30px; 
                        border-radius: 8px; 
                        text-decoration: none; 
                        font-weight: 600; 
                        display: inline-block; 
                        margin: 20px 0;
                        transition: all 0.3s ease;
                        box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
                    }
                    .reset-link:hover { 
                        transform: translateY(-2px); 
                        box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4); 
                    }
                    .security-notice { 
                        background-color: #f1f3f4; 
                        padding: 20px; 
                        border-radius: 8px; 
                        margin-top: 25px;
                        border: 1px solid #e0e0e0;
                    }
                    .security-notice h4 { 
                        color: #2c2c2c; 
                        margin-bottom: 10px; 
                        font-size: 15px; 
                        font-weight: 600;
                    }
                    .security-notice p { 
                        color: #666666; 
                        font-size: 14px; 
                        margin: 0;
                    }
                    .footer { 
                        background-color: #1a1a1a; 
                        color: #cccccc; 
                        text-align: center; 
                        padding: 30px; 
                        font-size: 13px; 
                    }
                    .footer p { 
                        margin-bottom: 8px; 
                    }
                    .footer .copyright { 
                        color: #d4af37; 
                        font-weight: 500; 
                    }
                    @media only screen and (max-width: 600px) {
                        .email-wrapper { padding: 20px 10px; }
                        .container { margin: 0 10px; }
                        .header, .content { padding: 25px 20px; }
                        .logo { font-size: 28px; }
                        .token-container { font-size: 16px; letter-spacing: 1px; padding: 20px 15px; }
                        .reset-link { padding: 12px 25px; font-size: 14px; }
                    }
                </style>

            </head>
            <body>
                <div class="email-wrapper">
                    <div class="container">
                        <div class="header">
                            <div class="logo">CALIBARBER</div>
                            <div class="subtitle">BARBERSHOP PREMIUM</div>
                        </div>
                        <div class="content">
                            <div class="greeting">
                                Estimado/a <strong>%s</strong>,
                            </div>
                            
                            <div class="message">
                                Hemos recibido una solicitud para restablecer la contraseña de su cuenta en Calibarber Barbershop. 
                                Por motivos de seguridad, hemos generado un código de verificación temporal para proceder con el cambio.
                            </div>
                            
                            <div class="token-container">
                                %s
                            </div>
                            
                            <div class="warning">
                                <strong>IMPORTANTE:</strong> Este código de verificación tiene una validez de <strong>%d minutos</strong>
                            </div>
                            
                            <div class="instructions">
                                <h3>Instrucciones para restablecer su contraseña:</h3>
                                <ol>
                                    <li>Haga clic en el botón "Restablecer Contraseña" a continuación</li>
                                    <li>O acceda manualmente a la sección de recuperación de contraseña en nuestra aplicación</li>
                                    <li>Introduzca el código de verificación mostrado anteriormente</li>
                                    <li>Establezca su nueva contraseña siguiendo nuestros criterios de seguridad</li>
                                    <li>Confirme los cambios para completar el proceso</li>
                                </ol>
                                
                                <div style="text-align: center; margin-top: 20px;">
                                    <a href="https://calibarber-frontend.onrender.com/reset-password" class="reset-link">
                                        Restablecer Contraseña
                                    </a>
                                </div>
                            </div>
                            
                            <div class="security-notice">
                                <h4>Aviso de Seguridad</h4>
                                <p>
                                    Si usted no ha solicitado este restablecimiento de contraseña, puede ignorar este mensaje de forma segura. 
                                    Su contraseña actual permanecerá sin cambios. Le recomendamos revisar la actividad de su cuenta 
                                    y contactarnos si tiene alguna inquietud sobre la seguridad de la misma.
                                </p>
                            </div>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automático del sistema. Por favor, no responda a este correo electrónico.</p>
                            <p>Para consultas o soporte, contáctenos a través de nuestros canales oficiales.</p>
                            <p class="copyright">&copy; 2024 Calibarber Barbershop. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, nombreUsuario, token, tiempoExpiracion, token);
    }

    /**
     * Construye el contenido HTML para el correo de bienvenida
     * @param nombreUsuario Nombre del usuario
     * @return Contenido HTML del correo
     */
    private String construirHtmlBienvenida(String nombreUsuario) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #2c2c2c; 
                        background-color: #f5f5f5;
                    }
                    .email-wrapper { 
                        background-color: #f5f5f5; 
                        padding: 40px 20px; 
                        min-height: 100vh; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background-color: #ffffff; 
                        border-radius: 12px; 
                        overflow: hidden; 
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #1a1a1a 0%%, #2d2d2d 100%%); 
                        color: #ffffff; 
                        padding: 40px 30px; 
                        text-align: center; 
                        position: relative;
                    }
                    .header::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%%23ffffff" opacity="0.05"/><circle cx="75" cy="75" r="1" fill="%%23ffffff" opacity="0.05"/><circle cx="50" cy="10" r="0.5" fill="%%23ffffff" opacity="0.03"/></pattern></defs><rect width="100" height="100" fill="url(%%23grain)"/></svg>');
                        opacity: 0.3;
                    }
                    .logo { 
                        font-size: 32px; 
                        font-weight: 700; 
                        letter-spacing: 2px; 
                        margin-bottom: 8px;
                        position: relative;
                        z-index: 1;
                    }
                    .subtitle { 
                        font-size: 16px; 
                        font-weight: 300; 
                        opacity: 0.9; 
                        letter-spacing: 1px;
                        position: relative;
                        z-index: 1;
                    }
                    .content { 
                        padding: 40px 30px; 
                        background-color: #ffffff; 
                    }
                    .greeting { 
                        font-size: 20px; 
                        margin-bottom: 25px; 
                        color: #2c2c2c; 
                        text-align: center;
                        font-weight: 600;
                    }
                    .welcome-message { 
                        font-size: 16px; 
                        margin-bottom: 30px; 
                        color: #555555; 
                        line-height: 1.7; 
                        text-align: center;
                    }
                    .highlight-box { 
                        background: linear-gradient(135deg, #d4af37 0%%, #f4d03f 100%%); 
                        color: #1a1a1a; 
                        padding: 25px 20px; 
                        text-align: center; 
                        font-size: 18px; 
                        font-weight: 600; 
                        margin: 30px 0; 
                        border-radius: 8px; 
                        box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
                        border: 2px solid #d4af37;
                    }
                    .benefits { 
                        background-color: #f8f9fa; 
                        padding: 25px; 
                        border-radius: 8px; 
                        margin: 25px 0;
                        border-left: 4px solid #d4af37;
                    }
                    .benefits h3 { 
                        color: #2c2c2c; 
                        margin-bottom: 15px; 
                        font-size: 18px; 
                        font-weight: 600;
                        text-align: center;
                    }
                    .benefits-list { 
                        list-style: none;
                        padding: 0;
                    }
                    .benefits-list li { 
                        margin-bottom: 12px; 
                        font-size: 15px; 
                        color: #555555;
                        padding-left: 25px;
                        position: relative;
                    }
                    .benefits-list li::before {
                        content: '✓';
                        position: absolute;
                        left: 0;
                        color: #d4af37;
                        font-weight: bold;
                    }
                    .cta-button { 
                        background: linear-gradient(135deg, #d4af37 0%%, #f4d03f 100%%); 
                        color: #1a1a1a; 
                        padding: 15px 30px; 
                        border-radius: 8px; 
                        text-decoration: none; 
                        font-weight: 600; 
                        display: inline-block; 
                        margin: 20px 0;
                        transition: all 0.3s ease;
                        box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
                        text-align: center;
                    }
                    .cta-button:hover { 
                        transform: translateY(-2px); 
                        box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4); 
                    }
                    .contact-info { 
                        background-color: #f1f3f4; 
                        padding: 20px; 
                        border-radius: 8px; 
                        margin-top: 25px;
                        border: 1px solid #e0e0e0;
                        text-align: center;
                    }
                    .contact-info h4 { 
                        color: #2c2c2c; 
                        margin-bottom: 10px; 
                        font-size: 16px; 
                        font-weight: 600;
                    }
                    .contact-info p { 
                        color: #666666; 
                        font-size: 14px; 
                        margin: 5px 0;
                    }
                    .footer { 
                        background-color: #1a1a1a; 
                        color: #cccccc; 
                        text-align: center; 
                        padding: 30px; 
                        font-size: 13px; 
                    }
                    .footer p { 
                        margin-bottom: 8px; 
                    }
                    .footer .copyright { 
                        color: #d4af37; 
                        font-weight: 500; 
                    }
                    @media only screen and (max-width: 600px) {
                        .email-wrapper { padding: 20px 10px; }
                        .container { margin: 0 10px; }
                        .header, .content { padding: 25px 20px; }
                        .logo { font-size: 28px; }
                        .highlight-box { font-size: 16px; padding: 20px 15px; }
                        .cta-button { padding: 12px 25px; font-size: 14px; }
                    }
                </style>
            </head>
            <body>
                <div class="email-wrapper">
                    <div class="container">
                        <div class="header">
                            <div class="logo">CALIBARBER</div>
                            <div class="subtitle">BARBERSHOP PREMIUM</div>
                        </div>
                        <div class="content">
                            <div class="greeting">
                                 ¡Bienvenido/a <strong>%s</strong>!
                            </div>
                            
                            <div class="welcome-message">
                                Nos complace enormemente darte la bienvenida a <strong>Calibarber Barbershop</strong>, 
                                donde la tradición de la barbería se encuentra con la excelencia moderna.
                            </div>
                            
                            <div class="highlight-box">
                                Tu cuenta ha sido creada exitosamente
                            </div>
                            
                            <div class="benefits">
                                <h3>Descubre las bondades de nuestra aplicación:</h3>
                                <ul class="benefits-list">
                                    <li><strong>Reservas Online 24/7:</strong> Agenda tu cita cuando quieras, desde donde estés</li>
                                    <li><strong>Barberos Profesionales:</strong> Equipo experto con años de experiencia y técnicas modernas</li>
                                    <li><strong>Servicios Premium:</strong> Cortes clásicos, afeitados tradicionales y tratamientos especializados</li>
                                    <li><strong>Historial de Servicios:</strong> Lleva un registro completo de todos tus cortes y preferencias</li>
                                    <li><strong>Notificaciones Inteligentes:</strong> Recordatorios de citas y promociones exclusivas</li>
                                    <li><strong>Ambiente Exclusivo:</strong> Instalaciones modernas con el toque clásico de una barbería tradicional</li>
                                    <li><strong>Productos de Calidad:</strong> Utilizamos solo las mejores marcas y productos premium</li>
                                    <li><strong>Atención Personalizada:</strong> Cada cliente recibe un servicio único y adaptado a sus necesidades</li>
                                </ul>
                            </div>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="https://calibarber-frontend.onrender.com" class="cta-button">
                                    Explorar la Aplicación
                                </a>
                            </div>
                    
                        </div>
                        <div class="footer">
                            <p>Gracias por confiar en Calibarber Barbershop para tu cuidado personal.</p>
                            <p>Esperamos verte pronto en nuestras instalaciones.</p>
                            <p class="copyright">&copy; 2024 Calibarber Barbershop. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, nombreUsuario);
    }
}