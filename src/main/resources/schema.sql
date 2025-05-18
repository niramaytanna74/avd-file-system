-- SQL script to create all tables for FileSystem

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME
);

CREATE TABLE groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME
);

CREATE TABLE user_group_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME,
    CONSTRAINT fk_user_group_role_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_group_role_group FOREIGN KEY (group_id) REFERENCES groups(id)
);

CREATE TABLE bundles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME,
    CONSTRAINT fk_bundle_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT fk_bundle_group FOREIGN KEY (group_id) REFERENCES groups(id)
);

CREATE TABLE files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(255),
    click_location VARCHAR(255),
    click_time DATETIME,
    occasion VARCHAR(255),
    uploaded_by BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    bundle_id BIGINT,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME,
    CONSTRAINT fk_file_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT fk_file_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_file_bundle FOREIGN KEY (bundle_id) REFERENCES bundles(id)
);

CREATE TABLE access_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requestor_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME,
    CONSTRAINT fk_access_request_requestor FOREIGN KEY (requestor_id) REFERENCES users(id),
    CONSTRAINT fk_access_request_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_access_request_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE TABLE granted_access (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_id BIGINT,
    bundle_id BIGINT,
    access_type VARCHAR(20) NOT NULL,
    granted_by BIGINT NOT NULL,
    granted_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    created_time DATETIME,
    updated_by VARCHAR(100),
    updated_time DATETIME,
    CONSTRAINT fk_granted_access_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_granted_access_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_granted_access_bundle FOREIGN KEY (bundle_id) REFERENCES bundles(id),
    CONSTRAINT fk_granted_access_granted_by FOREIGN KEY (granted_by) REFERENCES users(id)
);
