package com.drivemaster.drivemastermain.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class User {
    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final UserStatus status;
    private final LocalDateTime createdAt;

    private User(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .status(status)
                .createdAt(createdAt);
    }

    public static final class Builder {
        private Long id;
        private String name;
        private String email;
        private String passwordHash;
        private Role role;
        private UserStatus status = UserStatus.ACTIVE;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            Objects.requireNonNull(email, "email is required");
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(role, "role is required");
            Objects.requireNonNull(passwordHash, "passwordHash is required");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (email.isBlank()) {
                throw new IllegalArgumentException("email must not be blank");
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            return new User(this);
        }
    }
}
