package com.perpustakaan.service_buku.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetBukuByNomorQuery {
    private String nomorBuku;
}
