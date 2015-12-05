/*
 * FilterWheelMotor.h
 *
 *  Created on: 29 nov. 2015
 *      Author: utilisateur
 */

#ifndef FILTERWHEELMOTOR_H_
#define FILTERWHEELMOTOR_H_

#include "motor.h"
#include "Config.h"

class FilterWheelMotor: public Motor {
	bool calibrating;
	unsigned long calibrationTarget;

	// False mean calibration failed
	bool calibrationState;

	// Return true when magnet is near the hall sensor
	bool readHall();
	virtual void targetPositionReached();

	void setHallStatus(bool status);

public:
	FilterWheelMotor(const uint8_t * pins, uint8_t positionConfigId = ID_STORAGE_FILTERWHEEL_POSITION, int fastestPerHalfStep = 1600);
	virtual ~FilterWheelMotor();

	virtual void tick();

	virtual void loadConfigPosition();


	void startCalibration(unsigned long target);

	bool lastCalibrationFailed() { return calibrationState; }
};


extern FilterWheelMotor filterWheelMotor;


#endif /* FILTERWHEELMOTOR_H_ */
