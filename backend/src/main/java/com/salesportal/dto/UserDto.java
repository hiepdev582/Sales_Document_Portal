package com.salesportal.dto;

import com.salesportal.entity.Department;
import com.salesportal.entity.Role;
import com.salesportal.entity.User;

public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private Department department;

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.department = user.getDepartment();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}
