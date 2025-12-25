package com.perpustakaan.service_anggota.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllAnggotaQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
}

