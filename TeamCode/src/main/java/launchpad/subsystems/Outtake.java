package launchpad.subsystems;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import launchpad.hardware.CRServoPositionController;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

public class Outtake extends Subsystem {
    @TelemetryObject
    private CRServoPositionController outtakeHeadingServo;

    private static final PIDCoefficients headingPIDCoefficients = new PIDCoefficients(1, 0, 0);

    @Override
    public void init(HardwareMap hardwareMap) {
        outtakeHeadingServo = new CRServoPositionController(hardwareMap.get(CRServo.class, "outtakeRotatorServo"), hardwareMap.get(AnalogInput.class, "outtakeRotatorServoPosition"), headingPIDCoefficients);
    }

    @Override
    public void loop() {
        outtakeHeadingServo.loop();
    }

    public void setTargetHeading(double targetHeadingRadians) {
        this.outtakeHeadingServo.setTargetAngleRadians(targetHeadingRadians);
    }
}
