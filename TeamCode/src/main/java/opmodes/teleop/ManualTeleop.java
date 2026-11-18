package opmodes.teleop;

import static launchpad.Constants.DRIVE_MOTOR_CONFIG;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.Constants;
import launchpad.TeleopBase;
import launchpad.geometry.MovementVector;
import launchpad.hardware.Motor;
import launchpad.movement.mecanum.MecanumDriver;
import launchpad.subsystems.Outtake;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

@TeleOp(name="Manual Teleop")
public class ManualTeleop extends TeleopBase {

    @TelemetryObject
    private final Outtake outtake = new Outtake();

    @TelemetryObject
    private MecanumDriver driver;

    @TelemetryObject
    private Motor fl, fr, bl, br;

    @Override
    protected void registerSubsystems() {
        robot.register(outtake);
    }

    @Override
    public void init() {
        super.init();

        fl = new Motor(hardwareMap.get(DcMotorEx.class, "fl"), DRIVE_MOTOR_CONFIG);
        fr = new Motor(hardwareMap.get(DcMotorEx.class, "fr"), DRIVE_MOTOR_CONFIG);
        bl = new Motor(hardwareMap.get(DcMotorEx.class, "bl"), DRIVE_MOTOR_CONFIG);
        br = new Motor(hardwareMap.get(DcMotorEx.class, "br"), DRIVE_MOTOR_CONFIG);

        driver = new MecanumDriver(fl, fr, bl, br, Constants.MECANUM_COEFFICIENT_MATRIX, Constants.MAX_WHEEL_VELOCITY);
    }

    @Override
    public void loop() {
        super.loop();

        outtake.setTargetHeading(Math.atan2(secondaryGamepad.leftJoystick.getY(), secondaryGamepad.leftJoystick.getX()));
        driver.setRelativeVelocity(new MovementVector(mainGamepad.leftJoystick.getY(), mainGamepad.leftJoystick.getX(), mainGamepad.rightJoystick.getX()));
    }
}
