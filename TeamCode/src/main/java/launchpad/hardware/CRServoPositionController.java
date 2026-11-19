package launchpad.hardware;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;

import launchpad.Loop;
import launchpad.controllers.PIDController;
import launchpad.geometry.Angles;
import launchpad.telemetry_viewer.websocket.TelemetryData;
import launchpad.telemetry_viewer.websocket.TelemetryObject;

/**
 * Drives a continuous rotation servo to a position using its analog position feedback.
 * <p>
 * The feedback only reports 0-360 degrees of servo rotation, so this counts total servo rotation across
 * the 360 -> 0 wrap, letting a geared output turn more than one servo rotation. That total is saved to a file on
 * the Control Hub so the output keeps the same zero between runs and reboots.
 */
public class CRServoPositionController implements Loop {
    private static final double TWO_PI = Math.PI * 2;

    /**
     * Save once the servo has turned this far since the last save. On startup, the shortest-path correction from the
     * saved value to the live reading covers this gap, so it must stay well under PI.
     */
    private static final double SAVE_THRESHOLD_RADIANS = 0.25;

    private final CRServo servo;
    private final AnalogInput analogServoPosition;

    /** Servo rotations per one output rotation. Negative if the output turns opposite the servo. */
    @TelemetryData
    private final double gearRatio;

    @TelemetryData
    private final double minAngleRadians;

    @TelemetryData
    private final double maxAngleRadians;

    /** Where the rotation total is saved between runs, or null to not save it. */
    private final File saveFile;

    @TelemetryObject
    private final PIDController pidController;

    /** Output angle target, relative to the saved zero. */
    @TelemetryData
    private double targetAngleRadians;

    /** Output angle, relative to the saved zero. */
    @TelemetryData
    private double currentAngleRadians;

    @TelemetryData
    private double currentVoltage;

    /** Total servo rotation, always equal to the raw sensor angle plus some whole number of rotations. */
    @TelemetryData
    private double totalServoRadians;

    /** The value of totalServoRadians where the output angle is 0. */
    private double zeroServoRadians;

    private double lastRawServoRadians;
    private double lastSavedTotalServoRadians;
    private boolean hasReading = false;

    public CRServoPositionController(CRServo servo, AnalogInput analogServoPosition, PIDCoefficients pidCoefficients) {
        this(servo, analogServoPosition, pidCoefficients, 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param gearRatio       servo rotations per one output rotation (negative if the output turns opposite the servo)
     * @param minAngleRadians lowest output angle the target can be set to, relative to the saved zero
     * @param maxAngleRadians highest output angle the target can be set to, relative to the saved zero
     * @param saveName        unique name for the file that stores this servo's position between runs, or null to not
     *                        save it (the output's position at startup becomes 0)
     */
    public CRServoPositionController(CRServo servo, AnalogInput analogServoPosition, PIDCoefficients pidCoefficients,
                                     double gearRatio, double minAngleRadians, double maxAngleRadians, String saveName) {
        this.servo = servo;
        this.analogServoPosition = analogServoPosition;
        this.gearRatio = gearRatio;
        this.minAngleRadians = minAngleRadians;
        this.maxAngleRadians = maxAngleRadians;
        this.saveFile = saveName == null ? null : AppUtil.getInstance().getSettingsFile(saveName + "_position.txt");

        boolean hasLimits = !Double.isInfinite(minAngleRadians) || !Double.isInfinite(maxAngleRadians);

        pidController = new PIDController(pidCoefficients, () -> hasLimits
                ? targetAngleRadians - currentAngleRadians
                : Angles.angleDifference(currentAngleRadians, targetAngleRadians));
    }

    @Override
    public void loop() {
        updatePosition();

        servo.setPower(Math.min(Math.max(pidController.getPower(), -1), 1));
    }

    public void setTargetAngleRadians(double targetAngleRadians) {
        this.targetAngleRadians = Math.min(Math.max(targetAngleRadians, minAngleRadians), maxAngleRadians);
    }

    public double getCurrentAngleRadians() {
        return currentAngleRadians;
    }

    /**
     * Recalibrate: declare the output's current position to be the given angle, and save it.
     * Use this once with the mechanism in a known position (e.g. lined up with a mark), or if the saved position is
     * ever wrong because the mechanism was moved while the robot was off.
     */
    public void setCurrentAngleRadians(double angleRadians) {
        updatePosition();

        zeroServoRadians = totalServoRadians - angleRadians * gearRatio;
        currentAngleRadians = angleRadians;
        save();
    }

    private void updatePosition() {
        currentVoltage = analogServoPosition.getVoltage();
        double rawServoRadians = (currentVoltage / analogServoPosition.getMaxVoltage()) * TWO_PI;

        if (!hasReading) {
            if (!load(rawServoRadians)) {
                // Nothing saved yet: the current position becomes 0
                totalServoRadians = rawServoRadians;
                zeroServoRadians = rawServoRadians;
            }
            lastSavedTotalServoRadians = totalServoRadians;
            hasReading = true;
        } else {
            // Shortest path between readings, so the 360 -> 0 jump counts as a small move
            totalServoRadians += Angles.angleDifference(lastRawServoRadians, rawServoRadians);
        }
        lastRawServoRadians = rawServoRadians;

        currentAngleRadians = (totalServoRadians - zeroServoRadians) / gearRatio;

        if (Math.abs(totalServoRadians - lastSavedTotalServoRadians) >= SAVE_THRESHOLD_RADIANS) {
            save();
        }
    }

    /** Restores the saved position, correcting for small moves since it was saved. Returns false if nothing was saved. */
    private boolean load(double rawServoRadians) {
        if (saveFile == null || !saveFile.exists()) return false;

        try {
            String[] parts = ReadWriteFile.readFile(saveFile).trim().split(",");
            double savedTotal = Double.parseDouble(parts[0]);
            zeroServoRadians = Double.parseDouble(parts[1]);

            // The saved total can be up to SAVE_THRESHOLD_RADIANS stale, so snap it to the live reading
            totalServoRadians = savedTotal + Angles.angleDifference(savedTotal, rawServoRadians);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void save() {
        lastSavedTotalServoRadians = totalServoRadians;
        if (saveFile == null) return;

        ReadWriteFile.writeFile(saveFile, totalServoRadians + "," + zeroServoRadians);
    }
}
