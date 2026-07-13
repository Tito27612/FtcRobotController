/* Copyright (c) 2021 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//trying to upload this code to GitHub

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="AA Omni Linear OpMode", group="Linear OpMode")
//@Disabled
public class TJNBasicOmniOpMode_Linear_V2 extends LinearOpMode {
    //added final because stopwatch object never changes
    private final ElapsedTime runtime = new ElapsedTime();
    // Declare OpMode members for each of the 4 motors.
    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    // Declare OpMode members for intake servo & motor, flipper servo, and shooting wheel.
    private CRServo intakeservo = null;
    private DcMotor intakemotor = null;
    private Servo flipper = null;
    private DcMotorEx wheel = null;
    // AA Change due to located on the expansion hub controller
    // added private declarations for state machine variables
    private final String IDLE = "IDLE";
    private final String SPIN_UP = "SPIN_UP";
    private final String LAUNCH = "LAUNCH";
    private final String LAUNCHING = "LAUNCHING";

    private String launchState;
    private int LAUNCHER_TARGET_VELOCITY;
    private int LAUNCHER_MIN_VELOCITY;
    private ElapsedTime launchTime;


    @Override
    public void runOpMode() {
//Initialize hardware maps first
        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        intakeservo = hardwareMap.get(CRServo.class, "intake_servo");
        intakemotor = hardwareMap.get(DcMotor.class, "intake_motor");
        flipper = hardwareMap.get(Servo.class, "flipper");
        wheel = hardwareMap.get(DcMotorEx.class, "wheel"); //AA Change


        // ########################################################################################
        // !!!            IMPORTANT Drive Information. Test your motor directions.            !!!!!
        // ########################################################################################
        // Most robots need the motors on one side to be reversed to drive forward.
        // The motor reversals shown here are for a "direct drive" robot (the wheels turn the same direction as the motor shaft)
        // If your robot has additional gear reductions or uses a right-angled drive, it's important to ensure
        // that your motors are turning in the correct direction.  So, start out with the reversals here, BUT
        // when you first test your robot, push the left joystick forward and observe the direction the wheels turn.
        // Reverse the direction (flip FORWARD <-> REVERSE ) of any wheel that runs backward
        // Keep testing until ALL the wheels move the robot forward when you push the left joystick forward.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);

        intakemotor.setDirection(DcMotor.Direction.FORWARD);
        wheel.setDirection(DcMotor.Direction.FORWARD);

        // Wait for the game to start (driver presses START)
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        //driver presses the play button

        //reset timers & stats after start button is hit
        runtime.reset();
        launchTime = new ElapsedTime();
        launchState = IDLE;
        LAUNCHER_TARGET_VELOCITY = 1300;
        LAUNCHER_MIN_VELOCITY = 1150;


        if (opModeIsActive()) {
            wheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            while (opModeIsActive()) {
                // --- WHEEL SPEED CONTROLS ---
                // --- INTAKE CONTROLS ---
                if (gamepad2.dpad_left) {
                    intakeservo.setPower(1);
                } else if (gamepad2.dpad_down) {
                    intakeservo.setPower(0);
                }

// --- FLIPPER CONTROLS ---
                if (gamepad2.dpad_right) {
                    flipper.setPosition(0.7);
                } else if (gamepad2.dpad_up) {
                    flipper.setPosition(0.5);
                }

                // --- DRIVING CONTROLS (Always Active) ---

                double axial = -gamepad1.left_stick_y;
                double lateral = gamepad1.left_stick_x;
                double yaw = gamepad1.right_stick_x;

                double frontLeftPower = axial + lateral + yaw;
                double frontRightPower = axial - lateral - yaw;
                double backLeftPower = axial - lateral + yaw;
                double backRightPower = axial + lateral - yaw;

                double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
                max = Math.max(max, Math.abs(backLeftPower));
                max = Math.max(max, Math.abs(backRightPower));

                if (max > 1.0) {
                    frontLeftPower /= max;
                    frontRightPower /= max;
                    backLeftPower /= max;
                    backRightPower /= max;
                }

                frontLeftDrive.setPower(frontLeftPower);
                frontRightDrive.setPower(frontRightPower);
                backLeftDrive.setPower(backLeftPower);
                backRightDrive.setPower(backRightPower);

                // --- SCREEN DATA ---
                telemetry.addData("Status", "Run Time: " + runtime.toString());
                telemetry.addData("Front left/Right", "%4.2f, %4.2f", frontLeftPower, frontRightPower);
                telemetry.addData("Back  left/Right", "%4.2f, %4.2f", backLeftPower, backRightPower);
                telemetry.addData("Launch State", launchState);
                telemetry.addData("Wheel Velocity", wheel.getVelocity());
                telemetry.update();
            }
        }
    }
}
                // run until the end of the match (driver presses STOP)
        //AA change - moved inside one loop as AI said it would prevent freezing







