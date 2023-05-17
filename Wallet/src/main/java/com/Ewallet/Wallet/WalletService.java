package com.Ewallet.Wallet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "create_wallet",groupId = "test1234")
    public void createWallet(String message) {
        Wallet wallet= Wallet.builder().username(message).balance(100000).build();
        walletRepository.save(wallet);
    }

    @KafkaListener(topics = {"transaction_request"},groupId = "test1234")
    public void updateWallet(String message) throws JsonProcessingException {

        JSONObject jsonObject=objectMapper.readValue(message,JSONObject.class);

        String userName=(String)jsonObject.get("fromUser");
        String toUser=(String)jsonObject.get("toUser");
        int amount=(Integer)jsonObject.get("amount");
        String transactionId=(String)jsonObject.get("transactionId");

        //sender wallet
        Wallet senderWallet = walletRepository.getWalletByUsername(userName);
        //receiver wallet
        Wallet receiverWallet = walletRepository.getWalletByUsername(toUser);

        JSONObject returnObject=new JSONObject();

        returnObject.put("transactionId",transactionId);

        if(senderWallet.getBalance()<amount){
            returnObject.put("status",Status.FAILED.toString());
        }
        else {
            returnObject.put("status",Status.SUCCESS.toString());
            senderWallet.setBalance(senderWallet.getBalance()-amount);
            receiverWallet.setBalance(receiverWallet.getBalance()+amount);
        }


        String returnMessage=objectMapper.writeValueAsString(returnObject);

        kafkaTemplate.send("transaction_request_Update",returnMessage);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
    }
}
