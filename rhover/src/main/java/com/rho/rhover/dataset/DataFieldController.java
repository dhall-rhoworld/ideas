package com.rho.rhover.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/data_field")
public class DataFieldController {
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;

	@RequestMapping("/all")
	public String all(@RequestParam(name="dataset_id") Long datasetId, Model model) {
		Dataset dataset = datasetRepository.findOne(datasetId);
		model.addAttribute("dataset", dataset);
		model.addAttribute("data_fields", dataFieldRepository.findByDataset(dataset));
		return "data_fields";
	}
}
