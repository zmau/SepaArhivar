package net.zmau.sepaarchive;

import java.util.ArrayList;
import java.util.List;

public class SepaArchiver {
    public static void main(String[] args) {
        SepaArchiver archiver = new SepaArchiver();
        archiver.archiveData();
    }

    public SepaArchiver(){

    }
    public void archiveData(){
        try {
            SepaReader reader = new SepaReader();
            List<HourDataItem> hourData = parsedData(reader.readData());
            SheetWriter writer = new SheetWriter();
            for(HourDataItem dataItem : hourData) {
                writer.WriteItem(dataItem);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private List<HourDataItem> parsedData(String cellularData){
        List<HourDataItem> result = new ArrayList<HourDataItem>();
        String[] rows = cellularData.split("\n");
        for(int i = 6; i < rows.length; i++){
            HourDataItem observation =parseLine(rows[i]);
            if(observation != null)
                result.add(observation);
        }
        return result;
    }
    private HourDataItem parseLine(String line){
        String[] cell = line.split(" ");
        if (cell.length < 7)
            return null;

        HourDataItem result = new HourDataItem();
        result.setTime(cell[0] + " " + cell[1]);
        if(cell.length > 2)
            result.setSO2(cell[2]);
        if(cell.length > 3)
            result.setPM10(cell[3]);
        if(cell.length > 4)
            result.setNO2(cell[4]);
        if(cell.length > 5)
            result.setCO(cell[5]);
        if(cell.length > 6)
            result.setPM2comma5(cell[6]);
        return result;
    }

}
