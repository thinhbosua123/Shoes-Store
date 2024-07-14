package com.nhutkhuong1.application.controller.shop;

import com.nhutkhuong1.application.config.Contant;
import com.nhutkhuong1.application.entity.User;
import com.nhutkhuong1.application.exception.BadRequestException;
import com.nhutkhuong1.application.model.dto.UserDTO;
import com.nhutkhuong1.application.model.mapper.UserMapper;
import com.nhutkhuong1.application.model.request.ChangePasswordRequest;
import com.nhutkhuong1.application.model.request.CreateUserRequest;
import com.nhutkhuong1.application.model.request.LoginRequest;
import com.nhutkhuong1.application.model.request.UpdateProfileRequest;
import com.nhutkhuong1.application.security.CustomUserDetails;
import com.nhutkhuong1.application.security.JwtTokenUtil;
import com.nhutkhuong1.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/users")
    public ResponseEntity<Object> getListUsers() {
        List<UserDTO> userDTOS = userService.getListUsers();
        return ResponseEntity.ok(userDTOS);
    }

    @PostMapping("/api/admin/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest createUserRequest){
        User user = userService.createUser(createUserRequest);
        return ResponseEntity.ok(UserMapper.toUserDTO(user));
    }

    @PostMapping("/api/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserRequest createUserRequest, HttpServletResponse response) {
        //Create user
        User user = userService.createUser(createUserRequest);

        //Gen token
        UserDetails principal = new CustomUserDetails(user);
        String token = jwtTokenUtil.generateToken(principal);

        //Add token on cookie to login
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setMaxAge(Contant.MAX_AGE_COOKIE);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(UserMapper.toUserDTO(user));
    }

    @PostMapping("/api/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        //Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));
            //Gen token
            String token = jwtTokenUtil.generateToken((CustomUserDetails) authentication.getPrincipal());

            //Add token to cookie to login
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setMaxAge(Contant.MAX_AGE_COOKIE);
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok(UserMapper.toUserDTO(((CustomUserDetails) authentication.getPrincipal()).getUser()));
        } catch (Exception ex) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác!");

        }
    }

    @GetMapping("/tai-khoan")
    public String getProfilePage(Model model) {
        return "shop/account";
    }

    @PostMapping("/api/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest passwordReq) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        userService.changePassword(user, passwordReq);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PutMapping("/api/update-profile")
    public ResponseEntity<Object> updateProfile(@Valid @RequestBody UpdateProfileRequest profileReq) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        user = userService.updateProfile(user, profileReq);
        UserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok("Cập nhật thành công");
    }
}
