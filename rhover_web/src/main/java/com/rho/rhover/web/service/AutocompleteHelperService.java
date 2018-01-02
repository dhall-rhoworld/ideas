package com.rho.rhover.web.service;

import java.util.List;

import com.rho.rhover.web.dto.JqueryUiAutocompleteDto;

public interface AutocompleteHelperService {

	List<JqueryUiAutocompleteDto> findMatchingFieldInstances(String fragment, Long studyId);
}
