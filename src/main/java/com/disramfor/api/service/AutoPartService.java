package com.disramfor.api.service;

import com.disramfor.api.dto.AutoPartDTO;
import com.disramfor.api.dto.IAutoPartMapper;
import com.disramfor.api.entity.AutoPart;
import com.disramfor.api.repository.AutoPartRepository;
import com.disramfor.api.repository.specification.AutoPartSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoPartService {

    private final AutoPartRepository autoPartRepository;
    private final IAutoPartMapper autoPartMapper;

    public List<String> getProductTypes() {
        return autoPartRepository.findDistinctProductTypes();
    }

    public List<String> getBrands(String type) {
        return autoPartRepository.findDistinctBrandsByType(type);
    }

    public List<String> getModels(String type, String brand) {
        return autoPartRepository.findDistinctModelsByTypeAndBrand(type, brand);
    }

    public List<String> getEngines(String type, String brand, String model) {
        return autoPartRepository.findDistinctEnginesByHierarchy(type, brand, model);
    }

    public Page<AutoPartDTO> searchParts(String type, String brand, String model, String engine, String searchTerm,
            Pageable pageable) {
        Specification<AutoPart> spec = AutoPartSpecification.filterBy(type, brand, model, engine, searchTerm);
        Page<AutoPart> page = autoPartRepository.findAll(spec, pageable);
        return page.map(autoPartMapper::toDTO);
    }
}

            
            
            