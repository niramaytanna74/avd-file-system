package com.avd.filesystem.model.entity;

import com.avd.filesystem.model.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "granted_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantedAccess extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id")
    private Bundle bundle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    @Column(nullable = false)
    private LocalDateTime grantedAt;

    public enum AccessType {
        FILE, BUNDLE
    }
}
