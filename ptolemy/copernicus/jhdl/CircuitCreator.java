package ptolemy.copernicus.jhdl;

import java.io.*;
import java.util.*;
import soot.util.*;
import soot.toolkits.graph.*;

public class CircuitCreator {

    private static String _getWireName(Object node) {
        return "wire" + node.toString();
    }

    public static void create(HashMutableDirectedGraph operatorGraph, 
            String fileName) throws IOException {
        //     int[] edge_array = {0,1,2,1,1,2,1,3};
        // String[] node_array = {"const1","add","reg","buf"};
        
        // int num_wires = edge_array.length / 2;
        
        File outputFile = new File(fileName);
        FileWriter writer = new FileWriter(outputFile);

        write_header(writer);
        
        for(Iterator nodes = operatorGraph.getNodes().iterator();
            nodes.hasNext();) {
            Object node = nodes.next();
            writer.write("    Wire " + _getWireName(node) + " = wire(32);\r\n");
        }

        for (Iterator nodes = operatorGraph.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            
            if (node.toString().equals("delay")) {
                Object pred = operatorGraph.getPredsOf(node).iterator().next();
                write_reg(writer, pred, node);
            } else if(node.toString().equals("add")) {
                Iterator preds = operatorGraph.getPredsOf(node).iterator();
                Object in1 = preds.next();
                Object in2 = preds.next();

                write_add(writer, in1, in2, node);
                
            } else if(node.toString().equals("buf")) {
                Object pred = operatorGraph.getPredsOf(node).iterator().next();
                write_buf(writer, pred);
            } else {
                try {
                    write_const(writer, node.toString(), 
                            Integer.parseInt(node.toString()));
                } catch (Exception ex) {
                }
            }
        }
        write_footer(writer);

        writer.close();
    }

    static void write_reg(FileWriter writer, Object in, Object out) 
            throws IOException {
        writer.write("    regc_o(" + _getWireName(in) + ", " +
                _getWireName(out) + ");\r\n");
    }

    static void write_add(FileWriter writer, Object in1,
            Object in2, Object out) throws IOException {
        writer.write("    add_o(" + _getWireName(in1) + ", " +
                _getWireName(in2) + ", " +
                _getWireName(out) + ");\r\n");
    }

    static void write_const(FileWriter writer, Object out, int value)
            throws IOException {
        
        writer.write("    constant_o(" + _getWireName(out) + ", " +
                value + ");\r\n");
    }

    static void write_buf(FileWriter writer, Object in)
            throws IOException {
        writer.write("    buf_o(" + _getWireName(in) + 
                ", LAD_Bus_Data_Out);\r\n");
    }

    static void write_header(FileWriter writer)
             throws IOException {
        writer.write("import byucc.jhdl.base.*;\r\n");
        writer.write("import byucc.jhdl.Logic.*;\r\n");
        writer.write("import byucc.jhdl.modgen.*;\r\n");
        writer.write("import byucc.jhdl.platforms.wildcard.*;\r\n");
        writer.write("import byucc.jhdl.platforms.wildcard.std_if.*;\r\n");
        writer.write("import byucc.jhdl.platforms.util.*;\r\n");
        writer.write("import byucc.jhdl.modgen.arrayMult.*;\r\n");
        
        writer.write("public class outputPE extends LogicCore {\r\n");
        
        writer.write("  public static CellInterface cell_interface[] = {\r\n");
        writer.write("    clk(\"K_Clk\"),\r\n");
        writer.write("    out(\"LAD_Bus_Data_Out\",32),\r\n");
        
        
        writer.write("  };\r\n");
        
        writer.write("  public outputPE(wc_pe parent){\r\n");
        writer.write("    super(parent);\r\n");
        
        writer.write("    Wire K_Clk=connect(\"K_Clk\",wa(\"K_Clk\"));\r\n");
        
        writer.write("    Wire LAD_Bus_Data_Out = connect(\"LAD_Bus_Data_Out\",\r\n");
        writer.write("		    wa(\"LAD_Bus_Data_Out\"));\r\n");
        
        
        writer.write("    setDefaultClock(K_Clk);\r\n");
    }
    
    static void write_footer(FileWriter writer)
            throws IOException {
        writer.write("  }\r\n");
        
        writer.write("  protected GenericInterfaceCell Clocks(GenericProcessingElement parent) {\r\n");
        writer.write("    return new K_Clock_IF(parent,\"K_Clock_IF\",constraints);\r\n");
        writer.write("  }\r\n");
        
        writer.write("}\r\n");
    }
}
