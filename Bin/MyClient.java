package Bin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;  
import java.net.*;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel; 

class State {
    boolean isWaiting = true, isClose = false;
    int data = -2;
    JButton jb[];
    JLabel l;
    Socket s;
    
    synchronized void disableButton(String a) {
        if (a.equals("-1")) {
            JOptionPane.showMessageDialog(null, "You loss", "Bingo", JOptionPane.WARNING_MESSAGE);
        } else if (a.equals("-7")) {
            JOptionPane.showMessageDialog(null, "other left", "player", JOptionPane.WARNING_MESSAGE);
        }
        for (int i = 0; i < 25; i++) {
            if (a.equals(jb[i].getText()))
                jb[i].setEnabled(false);
        }
    }
    
    synchronized void setButton(JButton j[]) {
        jb = j;
    }
    
    synchronized void display(String s) {
        l.setText(s);
    }
    
    synchronized void changeWaitingState(boolean b) {
        isWaiting = b;
    }
    
    synchronized void changeCloseState(boolean b) {
        isClose = b;
    }
    
    synchronized void addData(int b) {
        data = b;
    }
    
    synchronized void resetData() {
        data = -2;
    }
    
    synchronized int getData() {
        return data;
    }
    
    synchronized void closeSocket(Socket s1) {
        s = s1;
    }
}

class Conn implements Runnable {
    State state;
    
    Conn(State state) {
        this.state = state;
        new Thread(this).start();
    }
    
    public void run() {
        try {  
            Socket s = new Socket("localhost", 6666);
            state.closeSocket(s);
            DataInputStream dis = new DataInputStream(s.getInputStream());  
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String str = "";
            Scanner sc = new Scanner(System.in);
            
            while (!state.isClose) {
                System.out.println("Waiting for opponent..");
                state.changeWaitingState(true);
                state.display("Waiting for opponent..");
                
                str = (String) dis.readUTF();
                if (!str.equals("Start")) {
                    state.display("other player reply" + str);
                    state.disableButton(str);
                }
                state.changeWaitingState(false);
                if (str.equals("bye")) {
                    state.changeCloseState(true);
                    break;
                }
                System.out.print("Reply : " + str + "\n>>"); 
                while (state.getData() == -2) { Thread.sleep(100); }
                out.writeUTF((str = "" + state.getData()));
                state.resetData();
                if (str.equals("bye")) {
                    state.changeCloseState(true);
                    break;
                }
            }
            s.close();  
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

public class MyClient extends JFrame implements ActionListener {
    JButton[] b = new JButton[25];
    JPanel p; JLabel l;
    State state;
    
    MyClient(State state) {
        this.state = state;
        setTitle("BINGO");
        p = new JPanel();
        p.setSize(150, 150);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing() {
                state.addData(-7);
                JOptionPane.showMessageDialog(null, "bye", "hi", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        p.setLayout(new GridLayout(5, 5));
        Random rand = new Random();
        int[] arr = new int[26];
        int upperbound = 26;
        
        for (int i = 0; i < 25; i++) {
            while (true) {
                int n = rand.nextInt(upperbound);
                if (arr[n] == 0 && n != 0) {
                    arr[n] = 1;
                    b[i] = new JButton("" + n);
                    break;
                }
            }
            p.add(b[i]);
            b[i].addActionListener(this);
        }
        l = new JLabel();
        add(p);
        add(l, BorderLayout.NORTH);
        setVisible(true);
        setSize(600, 600);
    }
    
    public static void main(String[] args) {
        State state = new State();
        MyClient m = new MyClient(state);
        state.setButton(m.b);
        state.l = m.l;
        new Conn(state);
        m.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                state.changeCloseState(true);
                try {
                    state.s.getInputStream().close();
                    state.s.getOutputStream().close();
                    state.s.close();
                } catch (IOException ae) {}
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int d = Integer.parseInt(e.getActionCommand());
        if (!state.isWaiting) {
            state.disableButton(e.getActionCommand());
            boolean b = bingo();
            if (b) {
                System.out.println("bingo");
                state.addData(-1);
                JOptionPane.showMessageDialog(this, "You win", "Bingo", JOptionPane.WARNING_MESSAGE);
            } else {
                state.addData(d);
            }
        }
        if (state.isClose)
            this.dispose();
    }
    
    public boolean bingo() {
        int bing = 0;
        for (int i = 0; i < 5; i++)
            if (!b[i].isEnabled() && !b[i + 5].isEnabled() && !b[i + 10].isEnabled() && !b[i + 15].isEnabled() && !b[i + 20].isEnabled())
                bing++;
        for (int i = 0; i < 5; i++) {
            if (!b[5 * i].isEnabled() && !b[5 * i + 1].isEnabled() && !b[5 * i + 2].isEnabled() && !b[5 * i + 3].isEnabled() && !b[5 * i + 4].isEnabled())
                bing++;
        }
        if (!b[0].isEnabled() && !b[6].isEnabled() && !b[12].isEnabled() && !b[18].isEnabled() && !b[24].isEnabled())
            bing++;
        if (!b[4].isEnabled() && !b[8].isEnabled() && !b[12].isEnabled() && !b[16].isEnabled() && !b[20].isEnabled())
            bing++;
        return bing >= 5;
    }
}
