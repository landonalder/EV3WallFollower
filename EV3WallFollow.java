/*
 * Program Name: EV3WallFollow
 * Description: This program runs on a Lego EV3 robot and causes it to go through a given maze,
 * following to the right of the walls using an ultrasonic sensor and a touch sensor. When the
 * touch sensor is hit, the robot reverses & fixes its orientation to the wall.
 * I/O Representation: 
 * Left Motor - Port A
 * Right Motor - Port B
 * Touch Sensor - Port 1 
 * Ultrasonic Sensor - Port 2
 * Course Name: CSCI 372, Fall 2013
 * Student Name: Landon Alder
 * References: EV3BumperCar source code from the leJOS git
 * Dependencies: EV3Classes from the leJOS git
 */

package wall_follower;

import lejos.hardware.Button;
import lejos.hardware.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;

public class EV3WallFollow
{
	static RegulatedMotor leftMotor = Motor.A;
	static RegulatedMotor rightMotor = Motor.B;
	static UltrasonicSensor USensor;
	static TouchSensor TSensor;

	public static void main(String[] args)
	{
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		USensor = new UltrasonicSensor();
		USensor.setDaemon(true);
		USensor.start();
		TSensor = new TouchSensor();
		TSensor.setDaemon(true);
		TSensor.start();
		LCD.drawString("EV3 Bumper Car",0,1);
		Button.LEDPattern(6);
		Button.waitForAnyPress();
		driveForward();
	}
	
	public static void driveForward()
	{
		double followDist = 0.08; // Following distance is 8 cm from wall
		double threshold = 0.01;  // 1 cm variance allowed 
		while (true)
		{	
			EV3WallFollow.leftMotor.forward();
			EV3WallFollow.rightMotor.forward();
			EV3WallFollow.leftMotor.setSpeed(225);
			EV3WallFollow.rightMotor.setSpeed(225);
			if(EV3WallFollow.TSensor.pressed)
			{
				collision(); // Hit wall, back up and fix position
			}
			else if (EV3WallFollow.USensor.distance < followDist - threshold)
			{
				EV3WallFollow.rightMotor.setSpeed(95);
			// Case where robot is too far from wall
			} else if (EV3WallFollow.USensor.distance > followDist + threshold)
			{
				EV3WallFollow.leftMotor.setSpeed(95);
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Quit loop if button is pressed
			if (Button.readButtons() != 0)
			{
				quit();
			}
		}
	}
		
		public static void collision()
		{
			EV3WallFollow.leftMotor.setSpeed(150);
			EV3WallFollow.rightMotor.setSpeed(150);
			EV3WallFollow.leftMotor.backward();
			EV3WallFollow.rightMotor.backward();
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			EV3WallFollow.leftMotor.rotate(120);
			EV3WallFollow.rightMotor.rotate(-120);
		}
		
		public static void quit()
		{
			EV3WallFollow.leftMotor.stop();
			EV3WallFollow.rightMotor.stop();          
			Button.LEDPattern(0);
			System.exit(1);
		}
}

class TouchSensor extends Thread
{
	EV3TouchSensor sensor = new EV3TouchSensor(SensorPort.S1);
	public boolean pressed = false;

	public void run()
	{
		while (true)
		{
			pressed = sensor.isPressed();
		}
	}
}

class UltrasonicSensor extends Thread
{
	EV3UltrasonicSensor sensor = new EV3UltrasonicSensor(SensorPort.S2);
	public double distance = 255;

	public void run()
	{
		while (true)
		{
			float [] sample = new float[1];
			sensor.getDistanceMode().fetchSample(sample, 0);
			distance = sample[0];
			
			if (distance <= 0.08)
			{
				Button.LEDPattern(1); // Makes lights turn green when it's within proper range of the wall
			}
			else
			{
				Button.LEDPattern(2); // Makes lights turn red when it's not within proper range of the wall			
			}
			
		}
	}
}
