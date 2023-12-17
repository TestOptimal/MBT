/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * @author yxl01
 *
 */
@RestController
@RequestMapping("/api/v1/demo")
@JsonIgnoreProperties(ignoreUnknown = true)
@CrossOrigin
public class DemoController {
	private static Logger logger = LoggerFactory.getLogger(DemoController.class);
	
	@GetMapping(value = "insurance", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InsurancePremium> getInsurancePremium (
		@RequestParam (name="age", required=true) double age,
		@RequestParam (name="atFaultClaims", required=true) double atFaultClaims,
		@RequestParam (name="goodStudent", required=true) boolean goodStudent,
		@RequestParam (name="nonDrinker", required=true) boolean nonDrinker) throws Exception {
		
		InsurancePremium prem = new InsurancePremium ((int)age, (int)atFaultClaims, goodStudent, nonDrinker);
		prem.calc();
		return new ResponseEntity<>(prem, HttpStatus.OK);
	}

	public class InsurancePremium {
		public int age;
		public int atFaultClaims;
		public boolean goodStudent;
		public boolean nonDrinker;
		public int premium;
		
		public InsurancePremium (int age_p, int atFaultClaims_p, boolean goodStudent_p, boolean nonDrinker_p) {
			this.age = age_p;
			this.atFaultClaims = atFaultClaims_p;
			this.goodStudent = goodStudent_p;
			this.nonDrinker = nonDrinker_p;
		}
		
		public void calc () throws Exception {
			this.premium = 0;
			if (this.age < 16) {
				throw new Exception ("Age < 16");
			}
			else if (this.age < 25) {
				this.premium += 900;
			}
			else if (this.age < 65) {
				this.premium += 600;
			}
			else if (this.age < 90) {
				this.premium += 720;
			}
			else throw new Exception ("Age over 89");
			
			if (this.atFaultClaims == 0) {
				//
			}
			else if (this.atFaultClaims < 5) {
				this.premium += 100;
			}
			else if (this.atFaultClaims < 11) {
				this.premium += 300;
			}
			else {
				throw new Exception ("Too many atFaultClaims");
			}
			
			if (this.goodStudent) {
				this.premium -= 50;
			}
			
			if (this.nonDrinker) {
				this.premium -= 75;
			}
			
			// extra bonus for being example student
				if (this.nonDrinker && this.goodStudent && this.atFaultClaims == 0) {
				this.premium -= 100;
			}
		}
	}
}
