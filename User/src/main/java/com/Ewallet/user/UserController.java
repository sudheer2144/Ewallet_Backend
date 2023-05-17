package com.Ewallet.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/add")
    public String addUser(@RequestBody UserRequest userRequest){
        return userService.addUser(userRequest);
    }

    @GetMapping("/find")
    public User findUser(@RequestParam("username") String username){
        return userService.findUser(username);
    }

    @GetMapping("/findEmailDto/{username}")
    public ResponseEmailDto findByEmail(@PathVariable("username")String username){
        return userService.findEmailDto(username);
    }

}
