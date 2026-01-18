package com.perpustakaan.service_anggota.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAnggotaByIdQuery {
    private String id;
}