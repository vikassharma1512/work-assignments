package com.vik.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.vik.dto.RequestObject;
import com.vik.service.DayCalculatorService;

@Controller
public class DayCalculatorController {

	@Value("${spring.application.name}")
	private String appName;

	@Value("${custom.error.message}")
	private String errorMessage;

	private DayCalculatorService dayCalculatorService;

	@Autowired
	public DayCalculatorController(DayCalculatorService dayCalculatorService) {
		this.dayCalculatorService = dayCalculatorService;
	}

	@GetMapping("/")
	public String homePage(Model model) {
		model.addAttribute("appName", appName);
		return "home";
	}

	@PostMapping("/calculate")
	public String resultPage(@ModelAttribute RequestObject requestObject, Model model) {
		if (requestObject == null || (requestObject.getStartDate().isEmpty() && requestObject.getEndDate().isEmpty())) {
			model.addAttribute("errorMessage", "request is empty!");
			return "error";
		}
		if (requestObject.getStartDate().isEmpty()) {
			model.addAttribute("errorMessage", "Start Date is empty!");
			return "error";
		}
		if (requestObject.getEndDate().isEmpty()) {
			model.addAttribute("errorMessage", "End Date is empty!");
			return "error";
		}

		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = dateFormatter.parse(requestObject.getStartDate());
			Date endDate = dateFormatter.parse(requestObject.getEndDate());
			if (startDate.after(endDate)) {
				model.addAttribute("errorMessage", errorMessage);
				return "error";
			}

			long days = dayCalculatorService.calculateDays(startDate, endDate);

			model.addAttribute("days", days);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "result";
	}
}
