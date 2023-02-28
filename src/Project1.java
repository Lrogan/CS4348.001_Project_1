//import java.io.File;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.sql.Time;
//import java.util.Random;
//import java.util.Scanner;
//
//import static java.lang.System.exit;
//
//public class Project1 {
//    static int PC, SP, IR, AC, X, Y, Timer, TimerThreshold;
//    static int[] memory;
//    public static void main(String[] args){
//        //Setup
//        //CPU Vars
//        PC = IR = AC = X = Y = Timer = TimerThreshold = 0;
//        SP = 1000; //inits at end of user stack
//
//        //Memory Var
//        memory = new int[2000];
//
//        //disables Timer if the arg is either not given or is 0
//        if(args.length > 1)
//            TimerThreshold = Integer.parseInt(args[1]);
//        else
//            System.out.println("Timer arg not found, setting Timer to 0 to disable Timer");
//
//        loadProgram(args[0]);
//
////        System.out.println(Arrays.toString(memory));
////        System.out.println(memory[1000]);
//
//        //Operation Loop
//        while(IR != 50)
//        {
//            fetch();
//            execute();
//        }
//    }
//
//    public static void loadProgram(String filename)
//    {
//        Path path = FileSystems.getDefault().getPath(filename).toAbsolutePath();
////        System.out.println(path);
//
//        try
//        {
//            int index = 0;
//            Scanner file = new Scanner( new File(path.toUri()));
//            while(file.hasNextLine())
//            {
//                Scanner line = new Scanner(file.nextLine());
//                if(line.hasNext())
//                {
//                    String word = line.next();
//                    if(Character.isDigit(word.charAt(0)))
//                    {
//                        memory[index] = Integer.parseInt(word);
//                        index++;
//                    }
//                    else if(word.charAt(0) == '.')
//                        index = Integer.parseInt(word.substring(1));
//                }
//                line.close();
//            }
//            file.close();
//        }
//        catch(Exception e)
//        {
//            System.out.println("A file named " + filename + " was not found, perhaps it was not in the same folder as this program? (Hint: Expected file name should include the type extension, eg sample1.txt");
//            e.printStackTrace();
//        }
//    }
//
//    //used to read the memory at PC and load next instruction into IR, also handles timeouts
//    public static void fetch()
//    {
//        //disable interrupts if in Kernel Mode
//        if(PC < 1000)
//        {
//            if(TimerThreshold != 0 && Timer >= TimerThreshold)
//            {
//                Timer = 0;
//                toKernelMode(1000);
//            }
//            Timer++;
//        }
//
//        IR = memory[PC];
//        PC++;
//    }
//
//    //used to read the parameter that the current executing instruction needs without allowing interrupts
//    public static void fetchAtomic()
//    {
//        IR = memory[PC];
//        PC++;
//    }
//
//    public static void toKernelMode(int newPC)
//    {
//
//        //store X & Y in user stack
//        SP--;
//        memory[SP] = X;
//
//        SP--;
//        memory[SP] = Y;
//
//        //Save User SP as first thing in System Stack, then set Stack Pointer to the top of the System Stack
//        memory[1999] = SP;
//        SP = 1999;
//
//        //Store User PC into System Stack
//        SP--;
//        memory[SP] = PC;
//
//        //Move Program Counter to the beginning of the System Program
//        PC = newPC;
//
//    }
//
//    public static void toUserMode()
//    {
//        //Restore User Program Counter
//        PC = memory[SP];
//        SP++;
//
//        //Restore User Stack Pointer
//        SP = memory[SP];
//
//        //Restore Y
//        Y = memory[SP];
//        SP++;
//
//        //Restore X
//        X = memory[SP];
//        SP++;
//
//    }
//
//    public static void isProtected(int addr)
//    {
//        if(PC < 1000 && addr > 999)
//        {
//            System.out.println("Memory violation: accessing system address " + addr + " in user mode");
//            exit(1);
//        }
//    }
//
//    public static void execute()
//    {
//        switch (IR)
//        {
//            case 1 ->           //Load value
//            {
//                fetchAtomic();
//                AC = IR;
//            }
//            case 2 ->           //Load addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                AC = memory[IR];
//            }
//            case 3 ->           //LoadInd addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                isProtected(memory[IR]); //could I have made this less jank? yes, but idc
//                AC = memory[memory[IR]];
//            }
//            case 4 ->           //LoadIdxX addr
//            {
//                fetchAtomic();
//                isProtected(IR + X);
//                AC = memory[IR + X];
//            }
//            case 5 ->           //LoadIdxY addr
//            {
//                fetchAtomic();
//                isProtected(IR + Y);
//                AC = memory[IR + Y];
//            }
//            case 6 ->           //LoadSpX
//            {
//                isProtected(SP + X);
//                AC = memory[SP + X];
//            }
//            case 7 ->           //Store addr
//            {
//                fetchAtomic();
//                isProtected(memory[IR]);
//                memory[IR] = AC;
//            }
//            case 8 ->           //Get
//            {
//                Random randI = new Random();
//                AC = randI.nextInt(100) + 1;
//            }
//            case 9 ->           //Put port
//            {
//                fetchAtomic();
//                if (IR == 1)
//                    System.out.print(AC);
//                else
//                    System.out.print(Character.toChars(AC));
//            }
//            case 10 ->          //AddX
//                    AC += X;
//            case 11 ->          //AddY
//                    AC += Y;
//            case 12 ->          //SubX
//                    AC -= X;
//            case 13 ->          //SubY
//                    AC -= Y;
//            case 14 ->          //CopyToX
//                    X = AC;
//            case 15 ->          //CopyFromX
//                    AC = X;
//            case 16 ->          //CopyToY
//                    Y = AC;
//            case 17 ->          //CopyFromY
//                    AC = Y;
//            case 18 ->          //CopyToSp
//            {
//                isProtected(AC);
//                SP = AC;
//            }
//            case 19 ->          //CopyFromSp
//                    AC = SP;
//            case 20 ->          //Jump addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                PC = IR;
//            }
//            case 21 ->          //JumpIfEqual addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                if (AC == 0)
//                    PC = IR;
//            }
//            case 22 ->          //JumpIfNotEqual addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                if (AC != 0)
//                    PC = IR;
//            }
//            case 23 ->          //Call addr
//            {
//                fetchAtomic();
//                isProtected(IR);
//                SP--;
//                memory[SP] = PC;
//                PC = IR;
//            }
//            case 24 ->          //Ret
//            {
//                PC = memory[SP];
//                SP++;
//            }
//            case 25 ->          //IncX
//                    X++;
//            case 26 ->          //DecX
//                    X--;
//            case 27 ->          //Push
//            {
//                SP--;
//                memory[SP] = AC;
//            }
//            case 28 ->          //Pop
//            {
//                AC = memory[SP];
//                SP++;
//            }
//            case 29 ->          //Int
//                    toKernelMode(1500);
//            case 30 ->          //IRet
//                    toUserMode();
//            case 50 ->          //End
//                    exit(0);
//            default -> {
//                System.out.println("ERROR: IR was not an Instruction Code");
//                System.out.println("System Info:");
//                System.out.println("\tPC: " + PC);
//                System.out.println("\tSP: " + SP);
//                System.out.println("\tIR: " + IR);
//                System.out.println("\tAC: " + AC);
//                System.out.println("\tX: " + X);
//                System.out.println("\tY: " + Y);
//            }
//        }
//    }
//}