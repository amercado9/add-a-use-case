CREATE TABLE `student_account_status_change` (
    `status_change_id` int NOT NULL AUTO_INCREMENT,
    `student_id` int NOT NULL,
    `enabled` bit(1) NOT NULL,
    `effective_at` datetime(6) NOT NULL,
    PRIMARY KEY (`status_change_id`),
    KEY `idx_student_status_effective` (`student_id`, `effective_at`, `status_change_id`),
    CONSTRAINT `fk_student_status_change_student`
        FOREIGN KEY (`student_id`) REFERENCES `peer_evaluation_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
