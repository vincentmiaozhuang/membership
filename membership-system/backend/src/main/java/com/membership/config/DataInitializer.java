package com.membership.config;

import com.membership.entity.Permission;
import com.membership.entity.Role;
import com.membership.entity.User;
import com.membership.repository.PermissionRepository;
import com.membership.repository.RoleRepository;
import com.membership.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) {
        // 初始化权限
        if (permissionRepository.count() == 0) {
            initPermissions();
        }
        
        // 初始化角色
        if (roleRepository.count() == 0) {
            initRoles();
        }
        
        // 初始化管理员用户
        if (userRepository.count() == 0) {
            initAdminUser();
        }
    }
    
    private void initPermissions() {
        // 用户管理权限
        createPermission("user:create", "user", "create", "创建用户");
        createPermission("user:read", "user", "read", "查看用户");
        createPermission("user:update", "user", "update", "更新用户");
        createPermission("user:delete", "user", "delete", "删除用户");
        
        // 角色管理权限
        createPermission("role:create", "role", "create", "创建角色");
        createPermission("role:read", "role", "read", "查看角色");
        createPermission("role:update", "role", "update", "更新角色");
        createPermission("role:delete", "role", "delete", "删除角色");
        
        // 权限管理权限
        createPermission("permission:create", "permission", "create", "创建权限");
        createPermission("permission:read", "permission", "read", "查看权限");
        createPermission("permission:update", "permission", "update", "更新权限");
        createPermission("permission:delete", "permission", "delete", "删除权限");
        
        // 供应商管理权限
        createPermission("supplier:create", "supplier", "create", "创建供应商");
        createPermission("supplier:read", "supplier", "read", "查看供应商");
        createPermission("supplier:update", "supplier", "update", "更新供应商");
        createPermission("supplier:delete", "supplier", "delete", "删除供应商");
        
        // 产品管理权限
        createPermission("product:create", "product", "create", "创建产品");
        createPermission("product:read", "product", "read", "查看产品");
        createPermission("product:update", "product", "update", "更新产品");
        createPermission("product:delete", "product", "delete", "删除产品");
        
        // 客户管理权限
        createPermission("customer:create", "customer", "create", "创建客户");
        createPermission("customer:read", "customer", "read", "查看客户");
        createPermission("customer:update", "customer", "update", "更新客户");
        createPermission("customer:delete", "customer", "delete", "删除客户");
        
        // 充值记录管理权限
        createPermission("recharge:create", "recharge", "create", "创建充值记录");
        createPermission("recharge:read", "recharge", "read", "查看充值记录");
        createPermission("recharge:delete", "recharge", "delete", "删除充值记录");
        
        // 供应商余额管理权限
        createPermission("supplier-balance:create", "supplier-balance", "create", "创建供应商余额");
        createPermission("supplier-balance:read", "supplier-balance", "read", "查看供应商余额");
        createPermission("supplier-balance:update", "supplier-balance", "update", "更新供应商余额");
        createPermission("supplier-balance:delete", "supplier-balance", "delete", "删除供应商余额");
        
        // 客户产品管理权限
        createPermission("customer-product:create", "customer-product", "create", "创建客户产品");
        createPermission("customer-product:read", "customer-product", "read", "查看客户产品");
        createPermission("customer-product:update", "customer-product", "update", "更新客户产品");
        createPermission("customer-product:delete", "customer-product", "delete", "删除客户产品");
        
        // 客户付款管理权限
        createPermission("customer-payment:create", "customer-payment", "create", "创建客户付款");
        createPermission("customer-payment:read", "customer-payment", "read", "查看客户付款");
        createPermission("customer-payment:update", "customer-payment", "update", "更新客户付款");
        createPermission("customer-payment:delete", "customer-payment", "delete", "删除客户付款");
        
        // 客户余额管理权限
        createPermission("customer-balance:create", "customer-balance", "create", "创建客户余额");
        createPermission("customer-balance:read", "customer-balance", "read", "查看客户余额");
        createPermission("customer-balance:update", "customer-balance", "update", "更新客户余额");
        createPermission("customer-balance:delete", "customer-balance", "delete", "删除客户余额");
    }
    
    private void createPermission(String name, String resource, String action, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(description);
        permissionRepository.save(permission);
    }
    
    private void initRoles() {
        // 管理员角色 - 拥有所有权限
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setDescription("系统管理员");
        adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
        roleRepository.save(adminRole);
        
        // 普通用户角色 - 只拥有查看权限
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole.setDescription("普通用户");
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permissionRepository.findByName("user:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("role:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("permission:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("supplier:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("product:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("customer:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("recharge:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userPermissions.add(permissionRepository.findByName("customer-payment:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);
        
        // 经理角色 - 拥有管理权限但没有系统管理权限
        Role managerRole = new Role();
        managerRole.setName("ROLE_MANAGER");
        managerRole.setDescription("经理");
        Set<Permission> managerPermissions = new HashSet<>();
        // 供应商管理
        managerPermissions.add(permissionRepository.findByName("supplier:create").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("supplier:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("supplier:update").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("supplier:delete").orElseThrow(() -> new RuntimeException("Permission not found")));
        // 产品管理
        managerPermissions.add(permissionRepository.findByName("product:create").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("product:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("product:update").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("product:delete").orElseThrow(() -> new RuntimeException("Permission not found")));
        // 客户管理
        managerPermissions.add(permissionRepository.findByName("customer:create").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer:update").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer:delete").orElseThrow(() -> new RuntimeException("Permission not found")));
        // 充值记录管理
        managerPermissions.add(permissionRepository.findByName("recharge:create").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("recharge:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("recharge:delete").orElseThrow(() -> new RuntimeException("Permission not found")));
        // 客户付款管理
        managerPermissions.add(permissionRepository.findByName("customer-payment:create").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer-payment:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer-payment:update").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("customer-payment:delete").orElseThrow(() -> new RuntimeException("Permission not found")));
        // 只读权限
        managerPermissions.add(permissionRepository.findByName("user:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerPermissions.add(permissionRepository.findByName("role:read").orElseThrow(() -> new RuntimeException("Permission not found")));
        managerRole.setPermissions(managerPermissions);
        roleRepository.save(managerRole);
    }
    
    private void initAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setPhone("13800138000");
        admin.setEnabled(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Role not found")));
        admin.setRoles(roles);
        
        userRepository.save(admin);
    }
}
