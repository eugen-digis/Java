package com.acme.processor.service.filter;

import com.acme.aggregator.dao.filter.FilterEntity;
import com.acme.aggregator.dao.filter.FilterRepository;
import com.acme.common.dto.filter.Filter;
import com.acme.common.mapper.FilterMapper;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Transactional
@Slf4j
public class FilterService implements IFilterService {

  private final FilterRepository filterRepository;
  private final FilterMapper filterMapper;

  @Override
  public Set<Filter> list() {
    log.debug("Getting all filters");
    return filterRepository.findAll().stream()
        .map(filterMapper::toDto)
        .collect(Collectors.toSet());
  }

  @Override
  public Filter save(@NotNull String jsonBody) {
    log.debug("Saving filter with json body: {}", jsonBody);
    return filterMapper
        .toDto(filterRepository.save(FilterEntity.builder().jsonBody(jsonBody).build()));
  }

  @Override
  public Filter update(@NotNull Filter filter) {
    log.debug("Updating filter to: {}", filter);
    return filterRepository.findById(filter.getId())
        .map(filterEntity -> {
          filterEntity.setJsonBody(filter.getJsonBody());
          return filterEntity;
        })
        .map(filterMapper::toDto)
        .orElseThrow(() -> new RuntimeException(
            String.format("Filter with id: %d, not found", filter.getId())));
  }

  @Override
  public void delete(int id) {
    log.debug("Deleting filter with id: {}", id);
    filterRepository.deleteById(id);
  }
}
