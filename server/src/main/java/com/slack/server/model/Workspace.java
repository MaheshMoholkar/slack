package com.slack.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "workspaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "join_code", nullable = false, unique = true)
    private String joinCode;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Member> members;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Channel> channels;

    @PrePersist
    protected void onCreate() {
        if (joinCode == null) {
            // Generate a random 6-character join code
            joinCode = generateJoinCode();
        }
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
} 