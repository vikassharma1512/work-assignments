package com.vik.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class DayCalculatorServiceImpl implements DayCalculatorService {

	/**
	 * Method is used to calculate number of days between startdate and enddate.
	 */
	@Override
	public long calculateDays(Date startDate, Date endDate) {

		long diffInMilSec = Math.abs(startDate.getTime() - endDate.getTime());
		long days = TimeUnit.DAYS.convert(diffInMilSec, TimeUnit.MILLISECONDS);

		return days;

	}

}
