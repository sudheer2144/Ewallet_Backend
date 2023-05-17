package com.Ewallet.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    String addUser(UserRequest userRequest){

        User user=User.builder().username(userRequest.getUsername()).age(userRequest.getAge()).mobileNo(userRequest.getMobileNo())
                .email(userRequest.getEmail()).name(userRequest.getName()).build();

        userRepository.save(user);

        saveInCache(user);

        kafkaTemplate.send("create_wallet",user.getUsername());

        return "User added Successfully";
    }

    @Autowired
    RedisTemplate<String,User> redisTemplate;
    @Autowired
    ObjectMapper objectMapper;
    private void saveInCache(User user) {
        Map map=objectMapper.convertValue(user, Map.class);
        String key=user.getUsername();
        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key, Duration.ofHours(12));
        System.out.println(key);
    }

    public User findUser(String userName){
        Map map=redisTemplate.opsForHash().entries(userName);
        User user=null;
        if(map==null){
            user=userRepository.findByUsername(userName);
            saveInCache(user);
        }
        else{
            user=objectMapper.convertValue(map,User.class);
        }
        return user;
    }


    public ResponseEmailDto findByEmail(String email) {

        User user=userRepository.findByEmail(email);

        ResponseEmailDto responseEmailDto = ResponseEmailDto.builder().name(user.getName()).email(user.getEmail()).build();
        return responseEmailDto;
    }

    public ResponseEmailDto findEmailDto(String username) {

        User user=userRepository.findByUsername(username);

        ResponseEmailDto responseEmailDto = ResponseEmailDto.builder().name(user.getName()).email(user.getEmail()).build();
        return responseEmailDto;

    }
}
