package launchpad.hardware;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import launchpad.Loop;
import launchpad.controllers.PIDController;
import launchpad.geometry.Angles;
import launchpad.telemetry_viewer.websocket.TelemetryData;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

public class CRServoPositionController implements Loop {

    private final CRServo servo;
    private final AnalogInput analogServoPosition;

    @TelemetryData
    private double targetAngleRadians;

    @TelemetryData
    private double currentAngleRadians;

    @TelemetryData
    private double currentVoltage;

    @TelemetryObject
    private final PIDController pidController;


    public CRServoPositionController(CRServo servo, AnalogInput analogServoPosition, PIDCoefficients pidCoefficients) {
        this.servo = servo;
        this.analogServoPosition = analogServoPosition;

        pidController = new PIDController(pidCoefficients, () -> Angles.angleDifference(currentAngleRadians, targetAngleRadians));
    }

    @Override
    public void loop() {
        currentVoltage = analogServoPosition.getVoltage();
        currentAngleRadians = (currentVoltage / analogServoPosition.getMaxVoltage()) * Math.PI * 2;

        servo.setPower(Math.min(Math.max(pidController.getPower(), -1), 1));
    }

    public void setTargetAngleRadians(double targetAngleRadians) {
        this.targetAngleRadians = targetAngleRadians;
    }
}
