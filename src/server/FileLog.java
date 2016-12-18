package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


public class FileLog implements Observer {
    private static final String IO_ERROR = "Error with file promission.";
    private String fileName;

    public void setLogRoot(String logFile) {
        this.fileName = logFile;
    }

    public void emptyFile() {
        File logFile = new File(fileName);
        logFile.delete();
    }

    //Concurrency
    @Override
    public synchronized void update(Observable observable, Object arg) {
        try {
            File writeFile = new File(fileName);
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            }
            FileWriter filewriter = new FileWriter(fileName, true);
            if (observable instanceof ConcreteLogCenter) {
                filewriter.write((String) arg);
            }
            filewriter.close();
        } catch (IOException e) {
            System.err.println(IO_ERROR);
            System.exit(-1);
        }
    }
}
