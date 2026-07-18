package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name = "BlueGOALauto", preselectTeleOp = "27612 Tele OpMode")
public class BlueGOALauto extends LinearOpMode {

    // Mecanum Drivetrain Motors
    private DcMotorEx frontLeft, backLeft, frontRight, backRight;
    
    // Subsystem Hardware
    private DcMotorEx launcherWheel, intakeMotor;
    private CRServo intakeServo;
    private Servo flipper;

    // --- Encoder Constants (Tune these for your specific robot) ---
    static final double TICKS_PER_MOTOR_REV = 537.7;    // GoBilda 312 RPM Motor
    static final double WHEEL_DIAMETER_INCHES = 3.78; // 96mm Mecanum wheels
    static final double TICKS_PER_INCH = TICKS_PER_MOTOR_REV / (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double TICKS_PER_DEGREE = 8.5;       // Tune this for precise turns

    // Distances in INCHES for each wheel (Adjust these individually to fix drift)
    double FL_DISTANCE = -80.0;
    double FR_DISTANCE = -80.0;
    double BL_DISTANCE = -80.0;
    double BR_DISTANCE = -80.0;

    @Override
    public void runOpMode() {
        // Initialize Drivetrain
        frontLeft = hardwareMap.get(DcMotorEx.class, "front_left_drive");
        backLeft = hardwareMap.get(DcMotorEx.class, "back_left_drive");
        frontRight = hardwareMap.get(DcMotorEx.class, "front_right_drive");
        backRight = hardwareMap.get(DcMotorEx.class, "back_right_drive");
        
        // Initialize Subsystems
        launcherWheel = hardwareMap.get(DcMotorEx.class, "wheel");
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intake_motor");
        intakeServo = hardwareMap.get(CRServo.class, "intake_servo");
        flipper = hardwareMap.get(Servo.class, "flipper");

        // Set Directions
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);
        
        // Prepare encoders
        setDriveMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        // Set Brake mode
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized & Ready");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // Start subsystems
            // intakeServo.setPower(-1);
            // launcherWheel.setVelocity(-1500);
            // intakeMotor.setPower(-0.5);
            sleep(500);
            
            // Initial Backup (Using the 4 individual wheel distances)
            driveInches(FL_DISTANCE, FR_DISTANCE, BL_DISTANCE, BR_DISTANCE, 0.75, 4000);
            
            // Shooting sequence
            Shoot();
            
            requestOpModeStop();
        }
    }

    private void Shoot() {
        for (int count = 0; count < 3; count++) {
            flipper.setPosition(0.7);
            sleep(800);
            flipper.setPosition(0.5);
            sleep(800);
        }
    }

    /**
     * Drives using individual wheel encoder distances
     */
    private void driveInches(double fl, double fr, double bl, double br, double power, int timeout) {
        int targetFL = (int)(fl * TICKS_PER_INCH);
        int targetFR = (int)(fr * TICKS_PER_INCH);
        int targetBL = (int)(bl * TICKS_PER_INCH);
        int targetBR = (int)(br * TICKS_PER_INCH);

        moveRobot(targetFL, targetFR, targetBL, targetBR, power, timeout);
    }

    /**
     * Moves the robot using encoders.
     */
    private void moveRobot(int fl, int fr, int bl, int br, double power, int timeout) {
        setDriveMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setTargetPosition(fl);
        frontRight.setTargetPosition(fr);
        backLeft.setTargetPosition(bl);
        backRight.setTargetPosition(br);

        setDriveMode(DcMotor.RunMode.RUN_TO_POSITION);

        setDrivePower(Math.abs(power), Math.abs(power), Math.abs(power), Math.abs(power));

        long startTime = System.currentTimeMillis();
        while (opModeIsActive() &&
              (System.currentTimeMillis() - startTime < timeout) &&
              (frontLeft.isBusy() || frontRight.isBusy() || backLeft.isBusy() || backRight.isBusy())) {

            telemetry.addData("FL Remaining", fl - frontLeft.getCurrentPosition());
            telemetry.addData("FR Remaining", fr - frontRight.getCurrentPosition());
            telemetry.addData("BL Remaining", bl - backLeft.getCurrentPosition());
            telemetry.addData("BR Remaining", br - backRight.getCurrentPosition());
            telemetry.update();
            idle();
        }

        stopRobot();
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void setDriveMode(DcMotor.RunMode mode) {
        frontLeft.setMode(mode);
        backLeft.setMode(mode);
        frontRight.setMode(mode);
        backRight.setMode(mode);
    }

    private void setDrivePower(double fl, double fr, double bl, double br) {
        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        backLeft.setPower(bl);
        backRight.setPower(br);
    }

    private void stopRobot() {
        setDrivePower(0,0,0,0);
    }
}
