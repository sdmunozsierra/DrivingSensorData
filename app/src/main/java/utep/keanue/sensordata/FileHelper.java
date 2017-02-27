package utep.keanue.sensordata;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** FileHelper Class
 * Contains Read and Write a file.
 * @author Sergio David Munoz Sierra*/
public class FileHelper {

    //Variables
    final static String fileName = "sensorData.txt";
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensorData/" ;
    final static String TAG = FileHelper.class.getName();

    /** ReadFile
     * Method for reading a file.
     * @param context -> ?
     * @return String with file content */
    public static  String ReadFile(Context context){
        //Set a line = null
        String line = null;

        //Encapsulate in a try statement
        try {
            //Manage the file
            FileInputStream fileInputStream = new FileInputStream (new File(path + fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            //Loop trough file
            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            //Close bufferedReader
            bufferedReader.close();
        }

        //Catch file not found
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        //Debugging purposes
        catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }

        //Return String containing all data
        return line;
    }//end ReadFile

    /** SaveToFile
     * Method for reading a file.
     * @param data Data (String type) to be saved in file
     * @return True for operation success */
    public static boolean saveToFile(String data){

        //Encapsulate in try statement
        try {

            //Make a new directory
            new File(path).mkdir();
            File file = new File(path + fileName);

            //Check if file already on the system
            if (!file.exists()) {
                file.createNewFile(); //Create a new file
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true; //Operation Success
        }
        //Catch error
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        //Catch error
        catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return  false; //operation unsuccessful

    }//end saveToFile

    /** Delete a File */
    public static boolean deleteFile(File path) {
        boolean result = true;
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File child : path.listFiles()) {
                    result &= deleteFile(child);
                }
                result &= path.delete(); // Delete empty directory.
            } else if (path.isFile()) {
                result &= path.delete();
            }
            return result;
        } else {
            return false;
        }
    }//end delete file

}//end class