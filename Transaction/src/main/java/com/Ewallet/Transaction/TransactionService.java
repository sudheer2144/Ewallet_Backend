package com.Ewallet.Transaction;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Service
public class TransactionService {



    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    RestTemplate restTemplate;

    public void createTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        Transaction transaction= Transaction.builder().toUser(transactionRequest.getToUser()).fromUser(transactionRequest.getFromUser())
                .amount(transactionRequest.getAmount()).purpose(transactionRequest.getPurpose()).transactionId(UUID.randomUUID().toString()).transactionDate(new Date())
                .transactionStatus(TransactionStatus.PENDING).build();

        transactionRepository.save(transaction);

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("toUser",transaction.getToUser());
        jsonObject.put("fromUser",transaction.getFromUser());
        jsonObject.put("amount",transaction.getAmount());
        jsonObject.put("transactionId",transaction.getTransactionId());

        String kafkaMessage=objectMapper.writeValueAsString(jsonObject);

        kafkaTemplate.send("transaction_request",kafkaMessage);

    }

    @KafkaListener(topics = {"transaction_request_Update"},groupId = "test1234")
    public void updateTransaction(String message) throws JsonProcessingException {

        JSONObject returnUpdate = objectMapper.readValue(message,JSONObject.class);
        String transactionId=(String)returnUpdate.get("transactionId");
        String status=(String)returnUpdate.get("status");

        Transaction transaction=transactionRepository.findTransactionByTransactionId(transactionId);

        transaction.setTransactionStatus(TransactionStatus.valueOf(status));

        transactionRepository.save(transaction);

        callNotificationService(transaction);

    }

    private void callNotificationService(Transaction transaction) throws JsonProcessingException {

        String fromUserName=transaction.getFromUser();
        String toUserName=transaction.getToUser();
        String transactionId=transaction.getTransactionId();
        int amount=transaction.getAmount();
        String transactionStatus=transaction.getTransactionStatus().toString();


        HttpEntity httpEntity=new HttpEntity(new HttpHeaders());


        URI fromUrl=URI.create("http://localhost:9999/user/findEmailDto/"+fromUserName);

        JSONObject fromUserJSONObject=restTemplate.exchange(fromUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        System.out.println(fromUrl);

        URI toUrl= URI.create("http://localhost:9999/user/findEmailDto/"+toUserName);

        JSONObject toUserJSONObject=restTemplate.exchange(toUrl,HttpMethod.GET,httpEntity, JSONObject.class).getBody();
        System.out.println(toUrl);


        String senderName=(String)fromUserJSONObject.get("name");
        String senderMail=(String)fromUserJSONObject.get("email");

        String receiverName=(String)toUserJSONObject.get("name");
        String receiverMail=(String)toUserJSONObject.get("email");

        String senderMessageBody=String.format("Hi %s, The transaction with ID:%s of amount %s is %s",senderName,transactionId,amount,transactionStatus);

        JSONObject mailObject=new JSONObject();

        mailObject.put("email",senderMail);
        mailObject.put("message",senderMessageBody);

        kafkaTemplate.send("send_mail",objectMapper.writeValueAsString(mailObject));

        if(transactionStatus.equals("SUCCESS")){

            String receiverMessageBody=String.format("Hi %s,/n You have received an amount of %s from %s.",receiverName,amount,senderName);

            mailObject.put("email",receiverMail);
            mailObject.put("message",receiverMessageBody);

            kafkaTemplate.send("send_email",objectMapper.writeValueAsString(mailObject));
        }

    }

}
