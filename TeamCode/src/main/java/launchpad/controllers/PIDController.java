package launchpad.controllers;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.PIDCoefficients;

import java.util.function.Supplier;

public class PIDController implements Controller {

    private PIDCoefficients coefficients;
    private final Supplier<Double> errorSupplier;

    private double integralSum = 0;
    private double lastError = 0;
    private double lastTime = 0;
    private double lastDerivative = 0;

    public PIDController(@NonNull PIDCoefficients coefficients, @NonNull Supplier<Double> currentPositionSupplier, @NonNull Supplier<Double> targetPositionSupplier) {
        this(coefficients, () -> targetPositionSupplier.get() - currentPositionSupplier.get());
    }

    public PIDController(@NonNull PIDCoefficients coefficients, @NonNull Supplier<Double> errorSupplier) {
        this.coefficients = coefficients;
        this.errorSupplier = errorSupplier;
    }

    @Override
    public double getPower() {
        if (lastTime == 0) {
            lastTime = System.currentTimeMillis();
            return 0;
        }

        double deltaTime = System.currentTimeMillis() - lastTime;
        double deltaTimeSeconds = deltaTime / 1000;

        double error = errorSupplier.get();

        double rawDerivative = (error - lastError) / deltaTimeSeconds;

        double alpha = 0.01;
        double derivative = (alpha * rawDerivative) + ((1 - alpha) * lastDerivative);

        integralSum += (error * deltaTimeSeconds);

        double result = (coefficients.p * error) + (coefficients.i * integralSum) + (coefficients.d * derivative);

        lastError = error;
        lastTime = System.currentTimeMillis();
        lastDerivative = derivative;

        return result;
    }

    @Override
    public double getError() {
        return errorSupplier.get();
    }

    public void setCoefficients(PIDCoefficients coefficients) {
        this.coefficients = coefficients;
    }
}