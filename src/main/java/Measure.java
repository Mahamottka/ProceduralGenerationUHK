import chunks.ChunkManager;
import model.DiamondSquare;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Store{
    List<Double> data;
    int col;
    public Store(List<Double> data, int col){
        this.data = data;
        this.col = col;
    }
}
public class Measure {
        public static void main(String[] args) {
            List<Store> dataDia = new ArrayList();
            DiamondSquare diamondSquare = new DiamondSquare(0, 2, 0.0f, 0.0f, 0.0f, 0.0f, true);
            int rounds = 4097;
            int s=0;
            for (int size = 0; s < rounds; size++) {
                List<Double> run = new ArrayList();
                s = (1 << size) + 1;
                diamondSquare.setSize(s);
                for (int i = 0; i < 100; i++) {
                    long startTime = System.nanoTime();
                    diamondSquare.generate();
                    long endTime = System.nanoTime();
                    run.add((double)(endTime - startTime)/1e6);
                }
                dataDia.add(new Store(run,s));
                System.out.println("Done dia: "+ s);
            }

            try {
                convertToCSV(dataDia, "output_dia.csv");
                System.out.println("CSV file has been created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            ChunkManager perlin = new ChunkManager(2,1.0f,5318008);

            List<Store> dataPerlin = new ArrayList();
            s = 0;
            for (int size = 0; s < rounds; size++) {
                List<Double> run = new ArrayList();
                s = (1 << size) + 1;
                perlin.setSize(s);
                for (int i = 0; i < 100; i++) {
                    long startTime = System.nanoTime();
                    perlin.generateChunk(0,0, true);
                    long endTime = System.nanoTime();
                    run.add((double)(endTime - startTime)/1e6);
                }
                dataPerlin.add(new Store(run,s));
                System.out.println("Done perlin: "+ s);
            }

            try {
                convertToCSV(dataPerlin, "output_perlin.csv");
                System.out.println("CSV file has been created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    public static void convertToCSV(List<Store> dataDia, String filename) throws IOException {
        FileWriter csvWriter = new FileWriter(filename);

        // Write header
        for (int i = 0; i < dataDia.size(); i++) {
            csvWriter.append(""+dataDia.get(i).col);
            if (i < dataDia.size() - 1) {
                csvWriter.append(";");
            }
        }
        csvWriter.append("\n");

        // Write rows
        int numRows = dataDia.stream().mapToInt(store -> store.data.size()).max().orElse(0);
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < dataDia.size(); col++) {
                List<Double> rowData = dataDia.get(col).data;
                if (row < rowData.size()) {
                    csvWriter.append(String.format("%.10f", rowData.get(row)));
                }
                if (col < dataDia.size() - 1) {
                    csvWriter.append(";");
                }
            }
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

}
