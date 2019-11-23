package com.vik.service;

import java.util.Date;

public interface DayCalculatorService {
	/**
	 * Method is used to calculate number of days between startdate and enddate.
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	long calculateDays(Date startDate, Date endDate);
}
