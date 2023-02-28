import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class Memory
{
    static int[] memory;
    public static void main(String[] args) throws IOException {

        //initalize and load memory on creation.
        memory = new int[2000];
        loadProgram(args[0]);

        //input stream for accepting read/write requests
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        //busy wait for instructions from the CPU
        while(true)
        {
            if(input.ready())
            {
                //Using a scanner for ease of Integer inference
                Scanner line = new Scanner(input.readLine());
                String op = line.next();
                if(op.equals("Read"))
                    Read(line.nextInt());
                else
                    Write(line.nextInt(), line.nextInt());
            }
        }
    }

    //loads the program
    public static void loadProgram(String filename)
    {
        //looks for the filename given in the current directory
        Path path = FileSystems.getDefault().getPath(filename).toAbsolutePath();

        try
        {
            //while content keep processing file
            int index = 0;
            Scanner file = new Scanner( new File(path.toUri()));
            while(file.hasNextLine())
            {
                //if line is blank, skip.
                Scanner line = new Scanner(file.nextLine());
                if(line.hasNext())
                {
                    //if the first "word" starts with a digit, parse instruction. if it starts with a '.', parse load index. anything else is a comment and is skipped.
                    String word = line.next();
                    if(Character.isDigit(word.charAt(0)))
                    {
                        memory[index] = Integer.parseInt(word);
                        index++;
                    }
                    else if(word.charAt(0) == '.')
                        index = Integer.parseInt(word.substring(1));
                }
                line.close();
            }
            file.close();
            System.out.println("loaded");
        }
        catch(Exception e)
        {
            //if it can't find the file print the stacktrace to a file so it doesn't get lost
            try {
                PrintStream writer = new PrintStream("Memory.txt");
                e.printStackTrace(writer);
                writer.close();
            } catch (IOException a) {
                throw new RuntimeException(a);
            }
            System.out.println("A file named " + filename + " was not found, perhaps it was not in the same folder as this program? (Hint: Expected file name should include the type extension, eg sample1.txt");
            e.printStackTrace();
        }
    }

    //read from the memory, write to a file the current state of the memory
    public static void Read(int index)
    {
        System.out.println(memory[index]);
        try {
            FileWriter writer = new FileWriter("MemoryState.txt");
            writer.write(Arrays.toString(memory));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //write to the memory, write to a file the current state of the memory
    public static void Write(int index, int data)
    {
        memory[index] = data;
        System.out.println("Write Complete");
        try {
            FileWriter writer = new FileWriter("MemoryState.txt");
            writer.write(Arrays.toString(memory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
