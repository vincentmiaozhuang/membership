package com.membership.controller;

import com.membership.dto.JwtResponse;
import com.membership.dto.LoginRequest;
import com.membership.dto.MessageResponse;
import com.membership.dto.SignupRequest;
import com.membership.core.entity.Role;
import com.membership.core.entity.User;
import com.membership.core.repository.RoleRepository;
import com.membership.core.repository.UserRepository;
import com.membership.security.JwtUtils;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("用户登录请求，用户名: {}", loginRequest.getUsername());
        // 首先检查用户名是否存在
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);
        
        if (user == null) {
            logger.warn("用户登录失败，用户名不存在: {}", loginRequest.getUsername());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("此用户不存在"));
        }
        
        try {
            // 进行密码验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            
            // 获取用户的所有权限
            Set<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getResource() + ":" + permission.getAction())
                    .collect(Collectors.toSet());
            
            logger.info("用户登录成功，用户ID: {}, 用户名: {}, 角色: {}", user.getId(), user.getUsername(), roles);
            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles,
                    permissions));
        } catch (Exception e) {
            // 密码验证失败
            logger.warn("用户登录失败，用户名: {}, 错误: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("用户名、密码不匹配，请修改"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("用户注册请求，用户名: {}, 邮箱: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
        
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.warn("用户注册失败，用户名已存在: {}", signUpRequest.getUsername());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("用户注册失败，邮箱已被使用: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRealName(signUpRequest.getRealName());
        user.setPhone(signUpRequest.getPhone());
        
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        
        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> {
                        logger.error("默认角色 ROLE_USER 不存在");
                        return new RuntimeException("Error: Role is not found.");
                    });
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role foundRole = roleRepository.findByName(role)
                        .orElseThrow(() -> {
                            logger.error("角色不存在: {}", role);
                            return new RuntimeException("Error: Role is not found.");
                        });
                roles.add(foundRole);
            });
        }
        
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        
        logger.info("用户注册成功，用户ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
