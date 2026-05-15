package com.casbytes.core.modules.reference.service;

import com.casbytes.core.audit.Auditable;
import com.casbytes.core.audit.AuditableAction;
import com.casbytes.core.modules.reference.domain.ReferenceItem;
import com.casbytes.core.modules.reference.dto.ReferenceItemCreateRequest;
import com.casbytes.core.modules.reference.dto.ReferenceItemResponse;
import com.casbytes.core.modules.reference.mapper.ReferenceItemMapper;
import com.casbytes.core.modules.reference.repository.ReferenceItemRepository;
import com.casbytes.core.shared.exception.BusinessException;
import com.casbytes.core.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferenceItemService {

    private final ReferenceItemRepository referenceItemRepository;
    private final ReferenceItemMapper referenceItemMapper;

    @Transactional(readOnly = true)
    @Auditable(resource = "reference_item", action = AuditableAction.READ)
    public List<ReferenceItemResponse> listActive() {
        return referenceItemRepository.findAll().stream()
                .filter(ReferenceItem::isActive)
                .map(referenceItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Auditable(resource = "reference_item", action = AuditableAction.READ)
    public ReferenceItemResponse getById(UUID id) {
        ReferenceItem item = referenceItemRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReferenceItem", id));
        return referenceItemMapper.toResponse(item);
    }

    @Transactional
    @Auditable(resource = "reference_item", action = AuditableAction.CREATE)
    public ReferenceItemResponse create(ReferenceItemCreateRequest request) {
        if (referenceItemRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BusinessException("Reference item code already exists: " + request.getCode());
        }
        ReferenceItem entity = referenceItemMapper.toNewEntity(request);
        ReferenceItem saved = referenceItemRepository.save(entity);
        return referenceItemMapper.toResponse(saved);
    }
}
