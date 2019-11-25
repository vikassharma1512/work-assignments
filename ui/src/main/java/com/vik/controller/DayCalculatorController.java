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

import com.vik.dto.DateForm;
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
	public String resultPage(@ModelAttribute DateForm dateForm, Model model) {
		if (dateForm == null || (dateForm.getStartDate().isEmpty() && dateForm.getEndDate().isEmpty())) {
			model.addAttribute("errorMessage", "Date form is empty!");
			return "home";
		}
		if (dateForm.getStartDate().isEmpty()) {
			model.addAttribute("errorMessage", "Start Date is empty!");
			return "home";
		}
		if (dateForm.getEndDate().isEmpty()) {
			model.addAttribute("errorMessage", "End Date is empty!");
			return "home";
		}

		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = dateFormatter.parse(dateForm.getStartDate());
			Date endDate = dateFormatter.parse(dateForm.getEndDate());
			if (startDate.after(endDate)) {
				model.addAttribute("errorMessage", errorMessage);
				return "home";
			}

			long days = dayCalculatorService.calculateDays(startDate, endDate);

			model.addAttribute("days", days);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "home";
	}
}
