import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.exit;

public class CPU
{
    static int PC, SP, IR, AC, X, Y, Timer, TimerThreshold;
    static Runtime runtime;
    static Process memoryProcess;
    static BufferedReader input;
    static BufferedWriter output;

    public static void main(String[] args) throws IOException {
        //CPU Vars
        PC = IR = AC = X = Y = Timer = TimerThreshold = 0;
        SP = 1000; //inits at end of user stack

        //disables Timer if the arg is either not given or is 0
        if(args.length > 1)
            TimerThreshold = Integer.parseInt(args[1]);
        else
            System.out.println("Timer arg not found, setting Timer to 0 to disable Timer");

        //Startup child Memory Process
        runtime = Runtime.getRuntime();
        memoryProcess = runtime.exec("java Memory " + args[0]);

        //Streams for input/output
        input = new BufferedReader(new InputStreamReader(memoryProcess.getInputStream()));
        output = new BufferedWriter((new OutputStreamWriter((memoryProcess.getOutputStream()))));

        //wait for memory to load
        boolean isloadingMemory = true;
        while(isloadingMemory)
        {
            if(input.ready())
            {
                String reply = input.readLine();
                if(reply.contains("loaded"))
                    isloadingMemory = false;
                else
                    System.out.println(reply);
            }
        }

        //Operation Loop, quits out if it hits the quit instruction
        while(IR != 50)
        {
            fetch();
            execute();
        }
    }

    //handles asking the Memory Process for memory values
    public static int readMemory(int index)
    {
        try
        {
            output.write("Read " + index);
            output.newLine();
            output.flush();

            //busy wait while the Memory retrieves the data
            while(true)
            {
                if(input.ready())
                {
                    Scanner reply = new Scanner(input.readLine());
                    return reply.nextInt();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("There was an Error reading from memory");
            e.printStackTrace();
        }

        return -1;
    }

    //handles asking the Memory Process to write memory values, success boolean currently not used but useful for debugging in the future
    public static boolean writeMemory(int index, int data)
    {
        try
        {
            output.write("Write " + index + " " + data);
            output.newLine();
            output.flush();

            //busy wait while the Memory retrieves the data
            while(true)
            {
                if(input.ready())
                {
                    return input.readLine().contains("Complete");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("There was an Error writing to memory");
            e.printStackTrace();
        }

        return false;
    }

    //used to read the memory at PC and load next instruction into IR, also handles timeouts
    public static void fetch()
    {
        //disable interrupts if in Kernel Mode
        if(PC < 1000)
        {
            if(TimerThreshold != 0 && Timer >= TimerThreshold)
            {
                Timer = 0;
                toKernelMode(1000);
            }
            Timer++;
        }

        IR = readMemory(PC);
        PC++;
    }

    //used to read the parameter that the current executing instruction needs without allowing interrupts
    public static void fetchAtomic()
    {
        IR = readMemory(PC);
        PC++;
    }

    //Switches mode to Kernel
    public static void toKernelMode(int newPC)
    {

        //store X & Y in user stack
        SP--;
        writeMemory(SP, X);

        SP--;
        writeMemory(SP, Y);

        //Save User SP as first thing in System Stack, then set Stack Pointer to the top of the System Stack
        writeMemory(1999, SP);
        SP = 1999;

        //Store User PC into System Stack
        SP--;
        writeMemory(SP, PC);

        //Move Program Counter to the beginning of the System Program
        PC = newPC;

    }

    //Switches mode back to User
    public static void toUserMode()
    {
        //Restore User Program Counter
        PC = readMemory(SP);
        SP++;

        //Restore User Stack Pointer
        SP = readMemory(SP);

        //Restore Y
        Y = readMemory(SP);
        SP++;

        //Restore X
        X = readMemory(SP);
        SP++;

    }

    //Checks to see if in User Mode, then checks if the address is valid
    public static void isProtected(int addr)
    {
        if(PC < 1000 && addr > 999)
        {
            System.out.println("Memory violation: accessing system address " + addr + " in user mode");
            exit(1);
        }
    }


    //A massive switch case to handle the Instruction Set
    public static void execute()
    {
        switch (IR)
        {
            case 1 ->           //Load value
            {
                fetchAtomic();
                AC = IR;
            }
            case 2 ->           //Load addr
            {
                fetchAtomic();
                isProtected(IR);
                AC = readMemory(IR);
            }
            case 3 ->           //LoadInd addr
            {
                fetchAtomic();
                isProtected(IR);
                isProtected(readMemory(IR)); //could I have made this less jank? yes, but idc
                AC = readMemory(readMemory(IR));
            }
            case 4 ->           //LoadIdxX addr
            {
                fetchAtomic();
                isProtected(IR + X);
                AC = readMemory(IR + X);
            }
            case 5 ->           //LoadIdxY addr
            {
                fetchAtomic();
                isProtected(IR + Y);
                AC = readMemory(IR + Y);
            }
            case 6 ->           //LoadSpX
            {
                isProtected(SP + X);
                AC = readMemory(SP + X);
            }
            case 7 ->           //Store addr
            {
                fetchAtomic();
                isProtected(readMemory(IR));
                writeMemory(IR, AC);
            }
            case 8 ->           //Get
            {
                Random randI = new Random();
                AC = randI.nextInt(100) + 1;
            }
            case 9 ->           //Put port
            {
                fetchAtomic();
                if (IR == 1)
                    System.out.print(AC);
                else
                    System.out.print(Character.toChars(AC));
            }
            case 10 ->          //AddX
                    AC += X;
            case 11 ->          //AddY
                    AC += Y;
            case 12 ->          //SubX
                    AC -= X;
            case 13 ->          //SubY
                    AC -= Y;
            case 14 ->          //CopyToX
                    X = AC;
            case 15 ->          //CopyFromX
                    AC = X;
            case 16 ->          //CopyToY
                    Y = AC;
            case 17 ->          //CopyFromY
                    AC = Y;
            case 18 ->          //CopyToSp
            {
                isProtected(AC);
                SP = AC;
            }
            case 19 ->          //CopyFromSp
                    AC = SP;
            case 20 ->          //Jump addr
            {
                fetchAtomic();
                isProtected(IR);
                PC = IR;
            }
            case 21 ->          //JumpIfEqual addr
            {
                fetchAtomic();
                isProtected(IR);
                if (AC == 0)
                    PC = IR;
            }
            case 22 ->          //JumpIfNotEqual addr
            {
                fetchAtomic();
                isProtected(IR);
                if (AC != 0)
                    PC = IR;
            }
            case 23 ->          //Call addr
            {
                fetchAtomic();
                isProtected(IR);
                SP--;
                writeMemory(SP, PC);
                PC = IR;
            }
            case 24 ->          //Ret
            {
                PC = readMemory(SP);
                SP++;
            }
            case 25 ->          //IncX
                    X++;
            case 26 ->          //DecX
                    X--;
            case 27 ->          //Push
            {
                SP--;
                writeMemory(SP, AC);
            }
            case 28 ->          //Pop
            {
                AC = readMemory(SP);
                SP++;
            }
            case 29 ->          //Int
                    toKernelMode(1500);
            case 30 ->          //IRet
                    toUserMode();
            case 50 ->          //End, technically redundant, but useful as a just in case.
                    exit(0);
            default -> {        //Just in case something goes horribly, horribly wrong, it'll at least be simpler to understand where.
                System.out.println("ERROR: IR was not an Instruction Code");
                System.out.println("System Info:");
                System.out.println("\tPC: " + PC);
                System.out.println("\tSP: " + SP);
                System.out.println("\tIR: " + IR);
                System.out.println("\tAC: " + AC);
                System.out.println("\tX: " + X);
                System.out.println("\tY: " + Y);
            }
        }
    }
}