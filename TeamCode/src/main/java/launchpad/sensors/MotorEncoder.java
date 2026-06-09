package launchpad.sensors;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import launchpad.geometry.Angles;

public class MotorEncoder implements Encoder {
    private final DcMotorEx encoder;
    private final double ticksPerRotation;

    public MotorEncoder(@NonNull DcMotorEx encoder, double ticksPerRotation) {
        this.encoder = encoder;
        this.ticksPerRotation = ticksPerRotation;
        this.encoder.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    }

    public int getTicks() {
        return this.encoder.getCurrentPosition();
    }

    /**
     * Gets the encoder position in radians.
     * @return The position of the encoder in radians.
     */
    public double getPosition() {
        return Angles.normalizeAngle((getTicks() / ticksPerRotation) * 2.0 * Math.PI);
    }

    public void reset() {
        this.encoder.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        this.encoder.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
    }
}