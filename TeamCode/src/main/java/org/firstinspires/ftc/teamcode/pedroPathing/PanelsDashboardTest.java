package org.firstinspires.ftc.teamcode.pedroPathing;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Configurable
@TeleOp(name = "Panels Dashboard Test", group = "Pedro Pathing")
public class PanelsDashboardTest extends OpMode {
    private Follower follower;
    private TelemetryManager telemetryM;

    @Override
    public void init() {
        // Initialize Follower using the constants we created
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(0, 0, 0));

        // Initialize Panels Telemetry
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        telemetryM.addLine("Robot Initialized!");
        telemetryM.update(telemetry);
    }

    @Override
    public void loop() {
        // Update the follower (Pedro Pathing core logic)
        follower.update();

        // Basic TeleOp drive control for testing
        follower.setTeleOpDrive(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x, true);

        // Send data to the Panels Dashboard
        telemetryM.addData("X Position", follower.getPose().getX());
        telemetryM.addData("Y Position", follower.getPose().getY());
        telemetryM.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        
        // Debug information shown on dashboard
        telemetryM.debug("Path Busy", follower.isBusy());
        telemetryM.debug("Loop Time", "%.2f ms", getRuntime() * 1000);

        // Update both standard telemetry and Panels
        telemetryM.update(telemetry);
    }
}
