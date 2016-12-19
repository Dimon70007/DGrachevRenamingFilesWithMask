package ru.dgrachev;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Created by OTBA}|{HbIu` on 12.11.16.
 */
public class MySearchInPath {

    private static final int Q_SIZE=10;
    private static final int THREADS_COUNT=100;
    private static final File DUMMY=new File("");
    private final BlockingQueue<File> QUEUE
            =new LinkedBlockingQueue<>(Q_SIZE);
//    public  final BlockingQueue<File> RESULT_FILES
//            =new LinkedBlockingQueue<>();
    private final String targetPattern;
    private final String directory;
    private final GUISearchForm guiSearchForm;
//    private final String endsAt;
//    private final String contains;
//    private boolean findFiles=true;
    private boolean renameFiles=false;
    private boolean deleteFiles=false;
//    private static final File results=new File("resultsOfSearch");

    public MySearchInPath(
            final String targetPattern
            , final String targetDir
            ,final GUISearchForm guiSearchForm
    ) {
        this.targetPattern =targetPattern;
        this.directory=targetDir;
        this.guiSearchForm=guiSearchForm;
    }

    public void searchFilesWithKeyword(){

        if(!correctPath(directory)){
            displayErrorMessage("Path does not exist");
            return;
        }
        Runnable put= () -> {
            try {
                enumerate(new File(directory));
                QUEUE.put(DUMMY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        new Thread(put).start();


        for (int i=0;i<THREADS_COUNT;i++){
            Runnable putResults= () -> {

                try {
                    File tmp;
                    boolean isDone=false;
                    while (!isDone) {
                        tmp = QUEUE.take();
                        if (!(tmp == DUMMY)) {
                            searchKeywordInFile(tmp, targetPattern);
                        } else {
                            QUEUE.put(DUMMY);
                            isDone=true;

                        }
                    }
                    QUEUE.put(DUMMY);
                } catch (FileNotFoundException e) {

//                    guiSearchForm.generateMessage(e.getMessage());
//                    displayErrorMessage(e.getMessage());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            new Thread(putResults).start();
        }
    }

    private synchronized void displayErrorMessage(String message) {
//        if (foundFile) {
            guiSearchForm.generateMessage(message);
//            foundFile = false;
//        }
    }

    private boolean correctPath(String directory) {
        File dir=new File(directory);
        if (dir.isDirectory()){
            File[] files=dir.listFiles();
            return (files != null ? files.length : 0) >0;
        }
        return dir.isFile();
    }

    private void searchKeywordInFile(File file, String keyword) throws FileNotFoundException {
                try(Scanner in=new Scanner(file,"UTF-8")) {
//                    int lineCount = 1;
                    StringBuilder sb=new StringBuilder();
                    boolean foundKeyword=false;
                    for (int lineCount=1;in.hasNext();lineCount++) {
                        String line = in.nextLine();
                        if (line.contains(keyword)) {
                            foundKeyword=true;
                            sb.append(lineCount);
                            sb.append(", ");
                        }
                    }
                    if (foundKeyword)
                        guiSearchForm.appendResults(String.format(
                                "Keyword:%s%n In path=%s%n found in lines:%s%n",keyword,file.getPath(),
                                sb.substring(0,sb.lastIndexOf(","))));
                }

    }
    private void searchInFilename(File file) throws FileNotFoundException {

        String fName = file.getName();
        int synbolNunber=matching(fName, targetPattern);
        boolean isFileRenamed=false;
        boolean isFileDeleted=false;
        String result=file.getPath();
        if(synbolNunber>0) {
            if (renameFiles) {
                isFileRenamed = file.renameTo(new File(Paths.get(
                        file.getParent(),
                        fName.substring(synbolNunber)
                ).toString()
                ));

                if (isFileRenamed) displayAddResult(result," has been renamed in ",file.getName());
                else displayAddResult(result," has not been renamed",".");
            }
            if (deleteFiles){
                isFileDeleted=file.delete();
                if (isFileDeleted)
                    displayAddResult(result," has been deleted",".");
                else
                    displayAddResult(result," ",".");
            }
        }
    }

    private int matching(String fName,String targetString) {
        Pattern pattern;
        Matcher matcher;
        try {
            pattern = Pattern.compile(targetString);
            matcher=pattern.matcher(fName);

            if (matcher.find()) {
                return matcher.end();
            }
        }catch (PatternSyntaxException e) {
            displayErrorMessage("wrong pattern"+e.getMessage());
        }
        return -1;
    }

    private void displayAddResult(String oldName,String message,String newName) {
        guiSearchForm.appendResults(String.format(
                "File:%s%n%s%n%s%n",oldName,message,newName));
    }

    private void enumerate(File directory) throws InterruptedException {
        if (!directory.isDirectory()){
            QUEUE.put(directory);
        }else {
            File[] files = directory.listFiles();
            for (File file : files != null ? files : new File[0]) {
                enumerate(file);
            }
        }
    }
}
