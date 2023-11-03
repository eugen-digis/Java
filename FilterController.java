package com.acme.processor.controller.rest;

import com.acme.common.dto.filter.Filter;
import com.acme.processor.service.filter.IFilterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/filters")
@Api(description = "Filters API")
public class FilterController {

  @Autowired
  private IFilterService filterService;

  @GetMapping(produces = "application/json")
  @ApiOperation(value = "Get list of currently available filters")
  public Set<Filter> list() {
    return filterService.list();
  }

  @PostMapping
  @ApiOperation(value = "Create new filter")
  public Filter create(@RequestBody String jsonBody) {
    return filterService.save(jsonBody);
  }

  @PutMapping
  @ApiOperation(value = "Update existing filter")
  public Filter update(@RequestBody Filter filter) {
    return filterService.update(filter);
  }

  @DeleteMapping(value = "/{id}")
  @ApiOperation(value = "Delete filter with specified id")
  public ResponseEntity delete(@PathVariable int id) {
    filterService.delete(id);
    return new ResponseEntity<Filter>(HttpStatus.NO_CONTENT);
  }
}
