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

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

@TeleOp(name="27612 Tele OpMode", group="Linear OpMode")
//@Disabled
    // hiding local field warnings
@SuppressWarnings({"FieldCanBeLocal"})
public class Tele_OpMode_Linear_27612 extends LinearOpMode {
    private final ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    private CRServo intakeServo = null;
    private DcMotorEx intakeMotor = null;
    private Servo flipper = null;
    private DcMotorEx launcherWheel = null;
    final double LAUNCHER_TARGET_VELOCITY = 1300;
    final double INTAKE_TARGET_VELOCITY = 800;
    private boolean intakeOn = false;
    private boolean previousBumperState = false;

//for a default full speed
    private double driveSpeedMultiplier = 1.0;

    @Override
    public void runOpMode() {
        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        intakeServo = hardwareMap.get(CRServo.class, "intake_servo");
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intake_motor");
        flipper = hardwareMap.get(Servo.class, "flipper");
        launcherWheel = hardwareMap.get(DcMotorEx.class, "wheel");

        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        launcherWheel.setDirection(DcMotor.Direction.REVERSE);

        launcherWheel.setZeroPowerBehavior(BRAKE);
        frontLeftDrive.setZeroPowerBehavior(BRAKE);
        backLeftDrive.setZeroPowerBehavior(BRAKE);
        frontRightDrive.setZeroPowerBehavior(BRAKE);
        backRightDrive.setZeroPowerBehavior(BRAKE);

        launcherWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcherWheel.setVelocityPIDFCoefficients(10, 0, 0, 14);

        intakeMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMotor.setVelocityPIDFCoefficients(10, 0, 0, 14);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        previousBumperState = gamepad1.right_bumper;

        // Wait for the game to start (driver presses START)
        waitForStart();

        //reset timers & stats after start button is hit
        runtime.reset();

            while (opModeIsActive()) {
                //Launcher Wheel Speed
                if (gamepad2.y) {
                    launcherWheel.setVelocity(LAUNCHER_TARGET_VELOCITY);
                } else if (gamepad2.x) {
                    launcherWheel.setVelocity(0);
                }

                if (gamepad1.right_bumper && !previousBumperState) {
                    intakeOn = !intakeOn;
                }
                // Save current state for the next loop cycle
                previousBumperState = gamepad1.right_bumper;
                // Left bumper serves as an explicit safety stop button
                if (gamepad1.left_bumper) {
                    intakeOn = false;
                }
                // Intake system control
                if (gamepad1.b) {
                    // Reverses intake system (outtake)
                    intakeMotor.setVelocity(-INTAKE_TARGET_VELOCITY);
                    intakeServo.setPower(1);
                } else if (intakeOn) {
                    // Turns on intake system using target velocity
                    intakeMotor.setVelocity(INTAKE_TARGET_VELOCITY);
                    intakeServo.setPower(-1);
                } else {
                    // Turns off intake system
                    intakeMotor.setVelocity(0);
                    intakeServo.setPower(0);
                }
                //Flipper Control
                if (gamepad2.b) {
                    //lifting flipper up to launch ball
                    flipper.setPosition(0.7);
                } else if (gamepad2.a) {
                    //lowering flipper down
                    flipper.setPosition(0.5);
                }
                //half speed for shooting alignment
                if (gamepad1.dpad_down) {
                    driveSpeedMultiplier = 0.3; //
                }   else if (gamepad1.dpad_up) {
                    driveSpeedMultiplier = 1.0; // Return to standard full speed
                }

                //OMNI DRIVING CONTROLS (Always Active)
                double axial = -gamepad1.left_stick_y;
                double lateral = gamepad1.left_stick_x;
                double yaw = gamepad1.right_stick_x;
                //speed multiplier applied to joystick
                axial   *= driveSpeedMultiplier;
                lateral *= driveSpeedMultiplier;
                yaw     *= driveSpeedMultiplier;

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
                telemetry.addData("Status", "Run Time: " + runtime.seconds());
                telemetry.addData("Intake Target", intakeOn ? INTAKE_TARGET_VELOCITY : 0);
                telemetry.addData("Intake Actual", intakeMotor.getVelocity());
                telemetry.addData("Launcher Target", launcherWheel.getVelocity());
                telemetry.addData("Launcher Error",  launcherWheel.getVelocity() - LAUNCHER_TARGET_VELOCITY);
                telemetry.update();
            }
        }
    }







