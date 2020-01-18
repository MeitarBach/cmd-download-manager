import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    /**
     * A function for extracting the file name from a url String
     * @param url A string representing the url
     * @return The file's name
     */
    public static String getFileName(String url){
        String[] file_path = url.split("/");
        String file_name = file_path[file_path.length-1];

        return file_name;
    }

    /**
     * A function for checking differences between 2 files
     * @param file_1 The first file to compare
     * @param file_2 Te second file to compare
     */
    public static void diff(File file_1, File file_2){
        StringBuilder diff = new StringBuilder();
        FileReader fr_1 = null;
        FileReader fr_2 = null;
        try{
            fr_1 = new FileReader(file_1);;
            fr_2 = new FileReader(file_2);
            int cur1 = fr_1.read();
            int cur2 = fr_2.read();
            long i = 0;
            int count = 0;
            while (cur1 != -1 && cur2 !=-1){
                if (cur1 != cur2){
                    diff.append("Byte " + i + " of file 1 is: " + cur1);
                    diff.append(" and Byte " + i + " of file 2 is: " + cur2 + "\n");
                    count++;
                }
                if (count > 9) break;
                cur1 = fr_1.read();
                cur2 = fr_2.read();
                i++;
            }
            System.out.println(i+1 + " Bytes were compared");
            System.out.println(diff);
            if (count == 0){
                System.out.println("Wooooooh");
            }
        } catch (IOException e){
            System.out.println(e);
        }
    }
}
