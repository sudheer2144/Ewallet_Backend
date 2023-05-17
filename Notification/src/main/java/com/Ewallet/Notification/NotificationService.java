package com.Ewallet.Notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

//    @Autowired
//    private JavaMailSender mailSender;
    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "send_mail",groupId = "test1234")
    public void sendMessage(String kafkaMessage) throws JsonProcessingException {

        JSONObject jsonObject=objectMapper.readValue(kafkaMessage,JSONObject.class);

        String email=(String)jsonObject.get("email");
        String message=(String)jsonObject.get("message");

        System.out.println("we are in sending email "+email+" "+message);

//        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
//        simpleMailMessage.setSubject("Ewallet Application | Transaction Details");
//        simpleMailMessage.setText(message);
//        simpleMailMessage.setTo(email);
//        simpleMailMessage.setFrom("sudheerq7@gmail.com");

//        MimeMessage mimeMessage=mailSender.createMimeMessage();
//
//        MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(mimeMessage,true);
//
//        mimeMessageHelper.setFrom("sudheerq7@gmail.com");
//        mimeMessageHelper.setTo(email);
//        mimeMessageHelper.setText(message);
//        mimeMessageHelper.setSubject("Transaction Update");
        
//        mailSender.send(simpleMailMessage);

    }
}
