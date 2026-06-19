package com.kopi.gudang.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_notes")
@Data
public class ShiftNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pesan;
    private String dibuatOleh;
    private LocalDateTime waktuDibuat;
}