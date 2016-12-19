package ru.dgrachev;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by OTBA}|{HbIu` on 19.12.16.
 */
public class GUISearchForm {
    private JPanel RenamingFilesByMask;
    private JTextField contains;
    private JTextField pathToFind;
    private JButton DeleteFiles;
    private JButton RenameFiles;
    private JButton FindFiles;
    private JTextArea FoundFiles;
    private int counts;
    private ConcurrentHashMap<String, LongAdder> foundFilesMap;

    public GUISearchForm() {
        counts=0;
        foundFilesMap = new ConcurrentHashMap<>();
        FindFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
    }


    public void generateMessage(String s) {
        JOptionPane.showMessageDialog(RenamingFilesByMask.getParent(), s);
    }

    public synchronized void appendResults(String lines) {
        counts++;
        FoundFiles.append(String.format("%d.%s", counts, lines));
    }
}
