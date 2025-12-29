package com.perpustakaan.service_pengembalian.vo;

import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTemplateVO {
    private PengembalianReadModel pengembalian; // Mongo Model
    private Peminjaman peminjaman; // VO dari Service Peminjaman
}