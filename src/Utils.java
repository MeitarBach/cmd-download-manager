import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

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
     * A function which creates an array of desired number of ConnectionReader threads, assigning each of them
     * except the last one a range of equal size to work on. The last thread gets the remaining bytes range.
     * @param connections_number number of threads to create.
     * @param content_length the size of the download file.
     * @param bq a blocking queue to which the threads put chunks.
     * @param url a url to get the file from
     * @return an array of ConnectionReader threads
     */
    public static ConnectionReader[] createConnectionReaders(int connections_number, long content_length,
                                                              BlockingQueue<Chunk> bq, URL url){
        ConnectionReader[] readers_pool = new ConnectionReader[connections_number];
        // check if the file's size is evenly divisible for the desired amount of threads, and act accordingly
        long reader_range_size = content_length % connections_number == 0 ?
                content_length / connections_number : content_length / connections_number+1;
        long cur_range_start = 0;
        for(int i = 0 ; i < readers_pool.length ; i++){
            if (i != readers_pool.length - 1) // every thread except the last one
                readers_pool[i] = new ConnectionReader(cur_range_start, reader_range_size, bq, url);
            else // last thread
                readers_pool[i] = new ConnectionReader(cur_range_start, content_length, bq, url);
            content_length -= reader_range_size; // keep track of last thread range size
            cur_range_start += reader_range_size;
        }

        return readers_pool;
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
