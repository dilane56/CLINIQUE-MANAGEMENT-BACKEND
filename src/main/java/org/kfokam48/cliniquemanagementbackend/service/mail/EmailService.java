package org.kfokam48.cliniquemanagementbackend.service.mail;

import org.kfokam48.cliniquemanagementbackend.dto.MailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); // ton adresse d'envoi
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public void sendMail(MailDTO mailDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); // ton adresse d'envoi
        message.setTo(mailDTO.getDestinataireEmail());
        message.setSubject(mailDTO.getSujet());
        message.setText(mailDTO.getMessage());
        mailSender.send(message);
    }
}
