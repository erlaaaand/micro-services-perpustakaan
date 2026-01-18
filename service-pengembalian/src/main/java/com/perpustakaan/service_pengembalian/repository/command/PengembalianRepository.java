package com.perpustakaan.service_pengembalian.repository.command;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perpustakaan.service_pengembalian.entity.command.Pengembalian;

@Repository
public interface PengembalianRepository extends JpaRepository<Pengembalian, UUID> {
}