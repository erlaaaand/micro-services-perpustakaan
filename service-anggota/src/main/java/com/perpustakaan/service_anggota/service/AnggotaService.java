package com.perpustakaan.service_anggota.service;

import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;

@Service
public class AnggotaService {

    @Autowired
    private AnggotaRepository anggotaRepository;

    public Anggota saveAnggota(AnggotaRequest request) {
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(request.getNomorAnggota());
        anggota.setNama(request.getNama());
        anggota.setAlamat(request.getAlamat());
        anggota.setEmail(request.getEmail());
        
        return anggotaRepository.save(anggota);
    }

    public Anggota getAnggotaById(Long id) {
        return anggotaRepository.findById(id).orElse(null);
    }

    public List<Anggota> getAllAnggota() {
        return anggotaRepository.findAll();
    }
}