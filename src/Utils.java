import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class Utils {

    /**
     * A function for extracting the file name from a url String
     * @param url A string representing the url
     * @return The file's name
     */
    public static String getFileName(URL url){
        String[] file_path = url.getFile().split("/");
        String file_name = file_path[file_path.length-1];

        return file_name;
    }

    /**
     * A function which gets a file and returns its extension
     * @param file A file to get extension of
     * @return The file's extension
     */
    public static String getFileExtension(String file){
        String[] file_path = file.split("\\.");
        String file_extension = file_path[file_path.length-1];

        return file_extension;
    }

    /**
     * A function for getting the length of a file specified in a url
     * @param url A url of a file
     * @return The length of the file
     */
    public static long getContentLength(URL url){
        String content_length = "";
        HttpURLConnection connection = null;
        try{
            connection =  (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            int i = 0;

            while(true){ // it is known that Content-Length must be a header
                String header = connection.getHeaderFieldKey(i);
                if (header != null) {
                    if (header.equals("Content-Length")) {
                        content_length = connection.getHeaderField(i);
                        break;
                    }
                }
                i++;
            }

        } catch (IOException e){
            System.err.println("Couldn't get file's length: " + e.getMessage());
        }

        return Long.parseLong(content_length);
    }

    /**
     * A function which gets a URL string or a path to a file containing a list of URLs
     * and returns an array of urls
     * @param file a Url string or a File path string
     * @return an array containing a URL in the first case and an array of URLs in the second
     */
    public static URL[] getUrls(String file){
        URL[] result = new URL[1];
        LinkedList<URL> urls = new LinkedList<>();

        // The argument is a single URL
        if (!Utils.getFileExtension(file).equals("list")){
            try {
                result[0] = new URL(file);
                return result;
            } catch (MalformedURLException e){
                System.err.println("You entered an invalid URL: " + e);
            }
        }

        // The argument is a list of URLs
        else {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
            } catch (IOException e) {
                System.err.println("There was a problem opening the URLs file for reading: " + e);
            }

            String curUrl = "";
            try {
                while ((curUrl = br.readLine()) != null) {
                    urls.add(new URL(curUrl));
                }
            } catch (IOException e) {
                System.err.println("There was a problem while reading URLs from the file: " + e);
            }
        }

        return urls.toArray(result);
    }

    public static <E> void printArr(E[] arr){
        for (E e : arr){
            System.out.println(e);
        }
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
