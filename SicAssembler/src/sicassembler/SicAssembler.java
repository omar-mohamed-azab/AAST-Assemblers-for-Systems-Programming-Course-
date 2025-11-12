/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sicassembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 *
 * @author omar azab
 */
public class SicAssembler {

    static int StartAddress = 0x00;
    static int LocCnt = 0x00;
    static String ProgName = "SicAssembler";
    static LinkedHashMap<String, Integer> symbolTable = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<String, Integer> opCodeTable = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<Integer, Integer> linesTb = new LinkedHashMap<Integer, Integer>();
    static int ProgLength = 0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String hamada = "gone";
        System.out.print(hamada.substring(2,3));
        opCodeTable.put("ADD", 0x18);
        opCodeTable.put("AND", 0x40);
        opCodeTable.put("COMP", 0x28);
        opCodeTable.put("DIV", 0x24);
        opCodeTable.put("J", 0x3C);
        opCodeTable.put("JEQ", 0x30);
        opCodeTable.put("JGT", 0x34);
        opCodeTable.put("JLT", 0x38);
        opCodeTable.put("JSUB", 0X48);
        opCodeTable.put("LDA", 0x00);
        opCodeTable.put("LDCH", 0x50);
        opCodeTable.put("LDL", 0x08);
        opCodeTable.put("LDX", 0x04);
        opCodeTable.put("MUL", 0x20);
        opCodeTable.put("OR", 0x44);
        opCodeTable.put("RD", 0xD8);
        opCodeTable.put("RSUB", 0x4C);
        opCodeTable.put("STA", 0x0C);
        opCodeTable.put("STCH", 0x54);
        opCodeTable.put("STL", 0x14);
        opCodeTable.put("STSW", 0xE8);
        opCodeTable.put("STX", 0x10);
        opCodeTable.put("SUB", 0x1C);
        opCodeTable.put("TD", 0xE0);
        opCodeTable.put("TIX", 0x2C);
        opCodeTable.put("WD", 0xDC);
        
        
        //PASS ONE
        
        
        File file = new File("Sic.txt");        
        
        try
        {
            Scanner code = new Scanner(file);
            int LineIndex = 0;
            while(code.hasNext())
            {
                String line  = code.nextLine();
                System.out.println(String.format("%04x", LocCnt) + ": " + line);
                linesTb.put(LineIndex, LocCnt);                
                String[] col  = line.split(" ");
                if(col.length == 3)
                {
                    if(col[1].equals("START"))
                    {
                        StartAddress = Integer.parseInt(col[2], 16);
                        LocCnt = StartAddress;
                        ProgName = col[0];
                        symbolTable.put(col[0],LocCnt);
                    }
                    else
                    {
                        if(symbolTable.containsKey(col[0]))
                        {
                            System.out.println("Label is already in symbol table");
                            return;
                        }
                        else
                            symbolTable.put(col[0],LocCnt);
                        if(opCodeTable.containsKey(col[1]))
                            LocCnt += 3;
                        else if(col[1].equals("RESB"))
                            LocCnt  += Integer.parseInt(col[2]);
                        else if(col[1].equals("RESW"))
                           LocCnt += (3 * Integer.parseInt(col[2]));
                        else if(col[1].equals("BYTE"))
                            incrementLocCount(col[2], false);
                        else if(col[1].equals("WORD"))
                            incrementLocCount(col[2], true);
                        else
                            System.out.println("Inavlid Instruction");
                    }
                }
                if(col.length==2)
                {
                    if(col[0].equals("END"))
                        break;
                    else if(opCodeTable.containsKey(col[0]))
                        LocCnt+=3;
                    else if(opCodeTable.containsKey(col[1]))
                    {
                        if(symbolTable.containsKey(col[0]))
                            System.out.println("Label is already in symbol table");
                        else
                        {
                            symbolTable.put(col[0], LocCnt);
                            LocCnt+=3;
                        }                        
                    }
                    else
                        System.out.println("Invalid Instruction");
                }
                if(col.length==1)
                {
                    if(opCodeTable.containsKey(col[0]))
                        LocCnt+=3;
                    else if(col[0].equals("END"))
                        break;
                    else
                        System.out.println("Invalid Instruction");
                }
            LineIndex++;
            }
            ProgLength = LocCnt - StartAddress;
            System.out.println(symbolTable);
            System.out.println(ProgLength);
            code.close();
        //PASS TWO 
        File outFile = new File("Output.txt");
        Scanner Code = new Scanner(file);
        PrintWriter out = new PrintWriter(outFile);
        String tRecord = "";
        int tStartAddress = 0;
        int flag = 1;
        int o = 0;
        while(Code.hasNext())
        {
            String line  = Code.nextLine();
            String[] col  = line.split(" ");
            String obCode = "";
            if(col.length==3)
            {
                if(col[1].equals("START"))
                {
                    String programName = col[0];
                    if(programName.length()>6)
                        programName = programName.substring(0,6);
                    else if(programName.length()<6)
                        while(programName.length()<6)
                            programName +='X';
                    out.println("H:"+programName+"."+String.format("%06X", StartAddress)+"."+String.format("%06X", ProgLength));
                    tStartAddress = StartAddress;
                    
                }
                else
                { 
                    if(opCodeTable.containsKey(col[1]))
                    {
                        if(col[2].contains(","))
                        {
                            obCode = String.format("%02X",opCodeTable.get(col[1]));
                            obCode += String.format("%04X", symbolTable.get(col[2].substring(0, col[2].length()-2)));
                            String v = obCode.substring(2,3);
                            int z = (Integer.parseInt(obCode.substring(2,3)));
                            z += 8;
                            String w ;
                            w = String.format("%01X",z);
                            obCode.replace(obCode.substring(2,3), w);
                        }
                        else
                        {
                            obCode = String.format("%02X",opCodeTable.get(col[1]));
                            obCode += String.format("%04X", symbolTable.get(col[2]));
                        }
                    }
                    else if(col[1].equals("BYTE"))
                    {                                   
                        if (col[2].substring(0,1).equals("X")) 
                            {
                                obCode = col[2].substring(2, col[2].length()-1);
                            }
                        else if (col[2].substring(0,1).equals("C"))
                            {
                              char arr [] = col[2].substring(2, col[2].length()-1).toCharArray();
                              for(char c: arr )
                              {
                                  obCode += String.format("%02X", (int) c);
                              }   
                            }
                        else
                            obCode = String.format("%06X",Integer.parseInt(col[2]));
                    }
                    else if(col[1].equals("WORD"))
                    {           
                        if (col[2].substring(0,1).equals("X")) 
                            {
                                obCode = col[2].substring(2, col[2].length()-1);
                            }
                        else if (col[2].substring(0,1).equals("C"))
                            {
                              char arr [] = col[2].substring(2, col[2].length()-1).toCharArray();
                              for(char c: arr )
                              {
                                  obCode += String.format("%02X", (int) c);
                              }   
                            }
                        else
                            obCode = String.format("%06X",Integer.parseInt(col[2]));
                    }
                    else if(col[1].equals("RESW") || col[1].equals("RESB"))
                        {
                            int x = linesTb.get(o-1);
                            int length = x - tStartAddress;
                            if(!tRecord.equals(""))
                            out.println("T:"+String.format("%06X", tStartAddress)+"."+String.format("%02X", length)+"."+tRecord);
                            tStartAddress = linesTb.get(o+1);
                            if(LineIndex >= linesTb.size())
                            {
                                flag = 0;
                            }
                            tRecord = "";
                            o++;
                            continue;
                        }            
                }
            }
            else if(col.length==2)
            {
                        if (opCodeTable.containsKey(col[0])) 
                        {
                            if(col[1].contains(","))
                            {   
                                obCode = String.format("%02X",opCodeTable.get(col[0]));
                                obCode += String.format("%04X", symbolTable.get(col[1].substring(0, col[1].length()-2)));
                                String v = obCode.substring(2,3);
                                int z = (Integer.parseInt(obCode.substring(2,3)));
                                z += 8;
                                String w ;
                                w = String.format("%01X",z);
                                obCode.replace(obCode.substring(2,3), w);
                            }
                            else
                            {
                                obCode = String.format("%02X",opCodeTable.get(col[0]));
                                obCode += String.format("%04X", symbolTable.get(col[1]));    
                            } 
                        }
                    else if (opCodeTable.containsKey(col[1])) 
                    {
                        obCode = String.format("%02X", opCodeTable.get(col[1]));
                        obCode += "0000";
                    }
            }
            else if(col.length==1)
            {
                obCode = String.format("%02X", opCodeTable.get(col[0]));
                obCode += "0000";
            }
            if ((tRecord + obCode).length() > 60) 
                {
                    int length = linesTb.get(o) - tStartAddress;
                    out.println("T:"+String.format("%06X", tStartAddress)+"."+String.format("%02X", length)+"."+tRecord);
                    tRecord = obCode;
                    tStartAddress = linesTb.get(o);
                } 
                else 
                {
                    tRecord += "" + obCode;
                }
            o++;
        }
        o--;
        System.out.println(linesTb);
        int length = linesTb.get(o) - tStartAddress;
        if(flag != 0)
            if(!tRecord.equals(""))
                out.println("T:"+String.format("%06X", tStartAddress)+"."+String.format("%02X", length)+"."+tRecord);
        out.println("E:"+String.format("%06X", StartAddress));
        out.close();
        }
        
        
        catch(FileNotFoundException ex)
        {
            System.out.println(ex.getStackTrace());
        }   
    }
    
    
    
    
    
    static void incrementLocCount(String s,boolean f) 
    {
        if(f == false)
        {
            if (s.substring(0,1).equals("X")) 
            {
                s = s.substring(2, s.length()-1);
                double bytes = s.length() / 2.0;
                bytes = Math.ceil(bytes);
                LocCnt += bytes;
            }
            else if (s.substring(0,1).equals("C"))
            {
                s = s.substring(2, s.length()-1);
                LocCnt += s.length();
            } 
            else
            {
                LocCnt += 1;
            }
        }
        else if(f == true)
        {
            if (s.substring(0,1).equals("X")) 
            {
                s = s.substring(2, s.length()-1);
                double word = s.length() / 6.0;
                word = Math.ceil(word);
                LocCnt += 3*word;
            }
            else if (s.substring(0,1).equals("C"))
            {
                s = s.substring(2, s.length()-1);
                double word = s.length()/3.0;
                word = Math.ceil(word);
                LocCnt += 3*word;
            } 
            else
            {
                LocCnt += 3;
            }
        }  
    }
}
