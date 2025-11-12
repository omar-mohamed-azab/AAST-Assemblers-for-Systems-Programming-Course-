/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sicxeassembler;

import com.sun.xml.internal.ws.util.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import static java.lang.Math.abs;
import static java.lang.reflect.Array.set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author omar azab
 */
public class SICXEAssembler {
    static int StartAddress = 0x00;
    static int LocCnt = 0x00;
    static String ProgName = "SicXEAssembler";
    static LinkedHashMap<String, Integer> symbolTable = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<String, Integer> literalTable = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<String, Integer> opCodeTable = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<String, Integer> opCodeTable1 = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<String, Integer> opCodeTable2 = new LinkedHashMap<String, Integer>();
    static LinkedHashMap<Integer, Integer> linesTb = new LinkedHashMap<Integer, Integer>();
    static LinkedHashMap<String, String> Expression = new LinkedHashMap<String, String>();
    static LinkedHashMap<String, Integer> Registers = new LinkedHashMap<String, Integer>();
    static ArrayList literals = new ArrayList();
    static ArrayList format4 = new ArrayList();
    static String Label = "";
    static int ProgLength = 0;
    static int count = 0;
    static int literal = 0;
    static int literal2 = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Registers.put("A",0);
        Registers.put("X",1);
        Registers.put("L",2);
        Registers.put("B",3);
        Registers.put("S",4);
        Registers.put("T",5);
        Registers.put("F",6);
        
        opCodeTable1.put("FIX", 0xC4);
        opCodeTable1.put("FLOAT", 0xC0);
        opCodeTable1.put("HIO", 0xF4);
        opCodeTable1.put("NORM", 0xC8);
        opCodeTable1.put("SIO", 0xF0);
        opCodeTable1.put("TIO", 0xF8);
        
        opCodeTable2.put("ADDR", 0x90);
        opCodeTable2.put("CLEAR", 0xB4);
        opCodeTable2.put("COMPR", 0xA0);
        opCodeTable2.put("DIVR", 0x9C);
        opCodeTable2.put("MULR", 0x98);
        opCodeTable2.put("RMO", 0xAC);
        opCodeTable2.put("SHIFTL", 0xA4);
        opCodeTable2.put("SHIFTR", 0xA8);
        opCodeTable2.put("SUBR", 0x94);
        opCodeTable2.put("SVC", 0xB0);
        opCodeTable2.put("TIXR", 0xB8);
        
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
        opCodeTable.put("ADDF", 0x58);
        opCodeTable.put("COMPF", 0x88);
        opCodeTable.put("DIVF", 0x64);
        opCodeTable.put("LDB", 0x68);
        opCodeTable.put("LDF", 0x70);
        opCodeTable.put("LDS", 0x6C);
        opCodeTable.put("LDT", 0x74);
        opCodeTable.put("LPS", 0xD0);
        opCodeTable.put("MULF", 0x60);
        opCodeTable.put("SSK", 0xEC);
        opCodeTable.put("STB", 0x78);
        opCodeTable.put("STF", 0x80);
        opCodeTable.put("STI", 0xD4);
        opCodeTable.put("STS", 0x7C);
        opCodeTable.put("STT", 0x84);
        opCodeTable.put("SUBF", 0x5C);
        
        
        
        //PASS ONE
        File file = new File("SicXE.txt");        
        
        try
        {
            Scanner code = new Scanner(file);
            int LineIndex = 0;
            while(code.hasNext())
            {
                String line  = code.nextLine();
                System.out.println(String.format("%04x", LocCnt) + ": " + line);
                linesTb.put(LineIndex, LocCnt);                
                String[] col  = line.split("  ");
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
                        if(col[1].equals("EQU"))
                        {
                            if(col[2].equals("*"))
                            {
                                symbolTable.put(col[0],LocCnt);
                                Expression.put(col[0], "Relative");
                            }
                            else
                            {
                                if(symbolTable.containsValue(col[2]))
                                {
                                    symbolTable.put(col[0], symbolTable.get(col[2]));
                                    Expression.put(col[0], "Relative");
                                }
                                
                                else if(col[2].contains("+") || col[2].contains("-"))
                                {
                                    String[] exp = col[2].split(" ");
                                    int sum = symbolTable.get(exp[0]);
                                    int plus = 1,minus = 0;
                                    for(int i = 1;i<exp.length;i+=2)
                                    {
                                        switch(exp[i])
                                        {
                                            case "+": 
                                                if(symbolTable.containsKey(exp[i+1]))
                                                {
                                                    plus++;
                                                    sum+=symbolTable.get(exp[i+1]);
                                                }
                                                else
                                                    sum+=Integer.parseInt(exp[i+1]);
                                                break;
                                            case "-": 
                                                if(symbolTable.containsKey(exp[i+1]))
                                                {
                                                    minus++;
                                                    sum-=symbolTable.get(exp[i+1]);
                                                }
                                                else
                                                    sum-=Integer.parseInt(exp[i+1]);
                                                break;
                                        }
                                    }
                                    symbolTable.put(col[0], sum);
                                    if(plus==minus)
                                    {
                                        Expression.put(col[0],"Absolute");
                                    }
                                    else
                                    {                                        
                                        Expression.put(col[0],"Relative");
                                    }
                                    
                                }
                                else
                                {
                                    symbolTable.put(col[0], Integer.parseInt(col[2]));
                                    Expression.put(col[0], "Absolute");
                                }
                            }
                        }
                        else if (col[2].contains("="))
                        {
                            if(literalTable.containsKey(col[2]))
                                System.out.println("Literal already exists in literal table");
                            else
                            {    
                                literalTable.put(col[2],0x00);
                                literals.add(col[2]);
                                LocCnt+=3;
                                count++;
                            }
                            
                        }
                        else    
                        {
                                if(symbolTable.containsKey(col[0]))
                            {
                                System.out.println("Label is already in symbol table");
                                return;
                            }                        
                            else if(opCodeTable2.containsKey(col[1]))
                            { 
                                symbolTable.put(col[0], LocCnt);
                                Expression.put(col[0], "Relative");
                                LocCnt+=2;
                            }    
                            else if(col[1].contains("+") && opCodeTable.containsKey(col[1].substring(1)))
                            {
                                symbolTable.put(col[0], LocCnt);
                                Expression.put(col[0], "Relative");
                                LocCnt+=4;
                            }
                            else
                            {
                                symbolTable.put(col[0],LocCnt);
                                Expression.put(col[0], "Relative");
                            }
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
                }
                    
                
                if(col.length==2)
                {
                    if(col[0].equals("END"))
                    {
                        Set<String> keys = literalTable.keySet();
                        for(String k:keys)
                        {
                            if(literalTable.get(k)==0x00)
                            {
                                literalTable.put(k, LocCnt);
                                incrementLocCount(k.substring(1), false);
                            }
                        }
                        literal2 = count + literal;
                        count = 0;
                        break;
                    }
                    else if(col[0].equals("BASE"))
                    {
                        Label = col[1];
                        continue;
                    }
                    else if (col[1].contains("="))
                        {
                            if(literalTable.containsKey(col[1]))
                                System.out.println("Literal already exists in literal table");
                            else
                            {    
                                literalTable.put(col[1],0x00);
                                literals.add(col[1]);
                                LocCnt+=3;
                                count++;
                            }
                            
                        }
                    else if(opCodeTable.containsKey(col[0]))
                        LocCnt+=3;
                    else if(opCodeTable2.containsKey(col[0]))
                        LocCnt+=2;
                    else if(col[0].contains("+") && opCodeTable.containsKey(col[0].substring(1)))
                        LocCnt+=4;
                    else if(opCodeTable.containsKey(col[1]))
                    {
                        if(symbolTable.containsKey(col[0]))
                            System.out.println("Label is already in symbol table");
                        else
                        {
                            symbolTable.put(col[0], LocCnt);
                            Expression.put(col[0], "Relative");
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
                    else if(opCodeTable1.containsKey(col[0]))
                        LocCnt+=1;
                    else if(col[0].equals("LTORG"))
                    {
                        Set<String> keys = literalTable.keySet();
                        for(String k:keys)
                        {
                            if(literalTable.get(k)==0x00)
                            {
                                literalTable.put(k, LocCnt);
                                incrementLocCount(k.substring(1), false);
                            }
                        }
                        literal = count;
                        count = 0;
                    }
                    else if(col[0].equals("END"))
                    {
                        Set<String> keys = literalTable.keySet();
                        for(String k:keys)
                        {
                            if(literalTable.get(k)==0x00)
                            {
                                literalTable.put(k, LocCnt);
                                incrementLocCount(k.substring(1), false);
                            }
                        }
                        literal2 = count + literal;
                        count = 0;
                        break;
                    }
                    else
                        System.out.println("Invalid Instruction");
                }
            LineIndex++;
            }
            ProgLength = LocCnt - StartAddress;
            System.out.println(symbolTable);
            System.out.println(ProgLength);
            System.out.println(literalTable);
            System.out.println(Expression);
            code.close();
            //PASS TWO 
        File outFile = new File("Output.txt");
        Scanner Code = new Scanner(file);
        PrintWriter out = new PrintWriter(outFile);
        String tRecord = "";
        int Base = symbolTable.get(Label);
        int tStartAddress = 0;
        int flag = 1;
        int o = 0;
        while(Code.hasNext())
        {
            String line  = Code.nextLine();
            String[] col  = line.split("  ");
            String obCode = "";
            int add = 0x00;
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
                else if(col[1].equals("EQU"))
                {
                    continue;
                }
                else
                {                    
                    if(opCodeTable2.containsKey(col[1]))
                    {
                        obCode = String.format("02X",opCodeTable2.get(col[1]));
                        obCode += String.format("%01X",Registers.get(col[2].substring(0,1)));
                        obCode += String.format("%01X",Registers.get(col[2].substring(2,3)));
                    }
                    else if(opCodeTable.containsKey(col[1]))
                    {
                        String bin = String.format("%02X",opCodeTable.get(col[1]));
                        if(col[2].contains("#"))
                        {
                            String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+1));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;                           
                            bin += "0";
                            bin += opCodeTable.get(col[2]).toString().substring(1,col[2].length());
                        }
                        else if(col[2].contains("@"))
                        {
                            String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+2));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;
                            if(col[2].contains(","))
                            {
                                add = symbolTable.get(col[2].substring(0,col[2].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "C";
                                    add = Base - linesTb.get(o+1);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "A";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "A";
                                    bin += String.format("%03X", add);
                                }
                            }
                            else
                            {
                                add = symbolTable.get(col[2].substring(0,col[2].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - linesTb.get(o+1);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    int c= Integer.toHexString(add).length();
                                    bin += String.format("%03X", add);
                                }
                            }
                            obCode += bin;
                        }
                        else 
                        {
                            String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+3));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;
                            if(col[2].contains(","))
                            {
                                add = symbolTable.get(col[2].substring(0,col[2].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "C";
                                    add = Base - linesTb.get(o+1);
                                    bin += add;
                                    bin += String.format("%03X",add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "A";
                                    add = abs(add);
                                    String address =String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "A";
                                    bin += String.format("%03X", add);
                                }
                            }
                            else if(col[2].contains("="))
                            {
                                add = literalTable.get(col[2]) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - linesTb.get(o+1);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    bin += String.format("%03X", add);
                                }
                            }
                            else
                            {
                                add = symbolTable.get(col[2]) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - linesTb.get(o+1);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    bin += String.format("%03X", add);
                                }
                            }
                        }
                        obCode += bin;          
                    }
                    if(col[1].contains("+"))
                    {
                        String bin = String.format("%02X",(opCodeTable.get(col[1].substring(1,col[1].length()))));
                        String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+3));
                        String bin3 = bin.substring(0,1);
                        bin3 += bin2;
                        bin = bin3;
                        if(col[2].contains(","))
                            bin += "9";
                        else
                            bin += "1";
                        String add2 = Integer.toHexString(symbolTable.get(col[2]));
                        while(add2.length()!=5)
                            add2 = "0" + add2;
                        obCode += bin + add2;
                        format4.add(linesTb.get(o)+1);
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
                        int x = linesTb.get(o);
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
                if(col[0].equals("END"))
                {
                    for(int i=literal;i<literal2;i++)
                    {
                        String lit  = literals.get(i).toString();
                        if(lit.contains("X"))
                        {
                            obCode += lit.substring(3, lit.length()-1);
                        }
                        else if(lit.contains("C"))
                        {
                            char arr [] = lit.substring(3, lit.length()-1).toCharArray();
                              for(char c: arr )
                              {
                                  obCode += String.format("%02X", (int) c);
                              }
                        }
                        else
                        {
                             obCode += lit.substring(2, lit.length()-1);
                             System.out.print("");
                             System.out.print(obCode);
                        }
                    }
                }
                else if(col[0].equals("BASE"))
                {
                    continue;
                }
                else if(opCodeTable2.containsKey(col[0]))
                {
                    obCode = String.format("02X",opCodeTable2.get(col[0]));
                    obCode += String.format("%01X",Registers.get(col[1].substring(0,1)));
                    obCode += String.format("%01X",Registers.get(col[1].substring(2,3)));
                }
                else if (opCodeTable.containsKey(col[0])) 
                {
                        String bin = String.format("%02X",opCodeTable.get(col[0]));                        
                        if(col[1].contains("#"))
                        {
                           String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+1));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;
                            bin += "0";
                            bin += opCodeTable.get(col[1]).toString().substring(1,col[1].length());
                        }
                        else if(col[1].contains("@"))
                        {
                            String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+2));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;
                            if(col[1].contains(","))
                            {
                                add = symbolTable.get(col[1].substring(0,col[1].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "C";
                                    add = Base - linesTb.get(o+1); 
                                    int c= Integer.toHexString(add).length();
                                    while(c!=3)
                                    {
                                        bin+= "0";
                                        c++;
                                    }
                                    bin += add;
                                }
                                else if(add < 0x000)
                                {
                                    bin += "A";
                                    add = abs(add);
                                    String address = Integer.toHexString(add);
                                    while(address.length() < 3)
                                    {
                                        address = "F" + address;
                                    }
                                    bin += address;
                                }
                                else
                                {
                                    bin += "A";
                                    int c= Integer.toHexString(add).length();
                                    while(c!=3)
                                    {
                                        bin+= "0";
                                        c++;
                                    }
                                    bin += add;
                                }
                            }
                            else
                            {
                                add = symbolTable.get(col[1].substring(0,col[1].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - linesTb.get(o+1); 
                                    //int c= Integer.toHexString(add).length();
                                    System.out.println(add);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = Integer.toHexString(add);
                                    while(address.length() < 3)
                                    {
                                        address = "F" + address;
                                    }
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    int c= Integer.toHexString(add).length();
                                    while(c!=3)
                                    {
                                        bin+= "0";
                                        c++;
                                    }
                                    bin += add;
                                }
                            }
                            obCode += bin;
                        }
                        else 
                        {
                            String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+3));
                            String bin3 = bin.substring(0,1);
                            bin3 += bin2;
                            bin = bin3;
                            if(col[1].contains(","))
                            {
                                add = symbolTable.get(col[1].substring(0,col[1].length()-2)) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "C";
                                    add = Base - linesTb.get(o+1);
                                    
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "A";
                                    add = abs(add);
                                    bin += String.format("%03X", add);
                                }
                                else
                                {
                                    bin += "A";
                                    bin += String.format("%03X", add);
                                }
                            }
                            else if(col[1].contains("="))
                            {
                                add = literalTable.get(col[1]) - linesTb.get(o+1);
                                
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - symbolTable.get(col[1]);
                                    bin += String.format("%03X",add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    bin += String.format("%03X",add);
                                }
                            }
                            else
                            {
                                add = symbolTable.get(col[1]) - linesTb.get(o+1);
                                if(add > 0xfff)
                                {
                                    bin += "4";
                                    add = Base - symbolTable.get(col[1]);
                                    System.out.println(add);
                                    bin += String.format("%03X", add);
                                }
                                else if(add < 0x000)
                                {
                                    bin += "2";
                                    add = abs(add);
                                    String address = String.format("%03X",add);
                                    bin += address;
                                }
                                else
                                {
                                    bin += "2";
                                    System.out.println(add);
                                    bin += String.format("%03X", add);
                                }
                            }
                        }
                        obCode += bin;
                }
                
                if(col[0].contains("+"))
                {
                    String bin = String.format("%02X",(opCodeTable.get(col[0].substring(1,col[0].length()))));
                    String bin2 = String.format("%01X",(Integer.parseInt(bin.substring(bin.length() - 1 , bin.length()),16)+3));
                    String bin3 = bin.substring(0,1);
                    bin3 += bin2;
                    bin = bin3;
                    if(col[1].contains(","))
                        bin += "9";
                    else
                        bin += "1";
                    String add2 = Integer.toHexString(symbolTable.get(col[1]));
                    while(add2.length()!=5)
                        add2 = "0" + add2;
                    obCode += bin + add2;
                    format4.add(linesTb.get(o)+1);
                }
            }
            else if(col.length==1)
            {
                if(col[0].equals("END"))
                {
                    for(int i=literal;i<literal2;i++)
                    {
                        String lit  = literals.get(i).toString();
                        if(lit.contains("X"))
                        {
                            obCode += lit.substring(3, lit.length()-1);
                        }
                        else if(lit.contains("C"))
                        {
                            char arr [] = lit.substring(3, lit.length()-1).toCharArray();
                              for(char c: arr )
                              {
                                  obCode += String.format("%02X", (int) c);
                              }
                        }
                        else
                        {
                            obCode += lit.substring(2, lit.length()-1);
                        }
                    }
                }
                else if(opCodeTable1.containsKey(col[0]))
                    {
                        obCode = String.format("%02X",opCodeTable1.get(col[0]));
                    }
                else if(col[0].equals("LTORG"))
                { 
                    for(int i=0;i<literal;i++)
                    {
                        String lit  = literals.get(i).toString();
                        if(lit.contains("X"))
                        {
                            obCode += lit.substring(3, lit.length()-1);
                        }
                        else if(lit.contains("C"))
                        {
                            char arr [] = lit.substring(3, lit.length()-1).toCharArray();
                              for(char c: arr )
                              {
                                  obCode += String.format("%02X", (int) c);
                              }
                        }
                        else
                        {
                            obCode = String.format("%06X",Integer.parseInt(lit));
                        }
                    }
                    
                }
                else
                {
                obCode = String.format("%02X", opCodeTable.get(col[0]));
                obCode += "0000";
                }
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
            System.out.println(o); 
        }
        o--;
        System.out.println(o);
        System.out.println(linesTb);
        int length = tRecord.length()/2;
        if(flag != 0)
            if(!tRecord.equals(""))
                out.println("T:"+String.format("%06X", tStartAddress)+"."+String.format("%02X", length)+"."+tRecord);
        for(int i=0;i<format4.size();i++)
        {
            out.println("M:"+String.format("%06X",format4.get(i))+"."+"05");
        }
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
    
