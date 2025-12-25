package com.perpustakaan.service_buku.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllBukuQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
}

