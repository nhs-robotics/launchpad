package opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import launchpad.TeleopBase;

@TeleOp(name="Example Teleop")
public class ExampleTeleop extends TeleopBase {

    @Override
    public void init() {
        super.init();


    }

    @Override
    protected void registerSubsystems() {
        // robot.register(new SubsystemName());
    }

    @Override
    public void loop() {

    }
}
