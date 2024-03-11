package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class SortCsvFirstCol {

    public static void sortCsv(File inputFile, File outputFile, int fragmentSize) throws IOException {
        List<File> fragment = splitFile(inputFile, fragmentSize);
        System.out.println(fragment);

        for (var frag : fragment) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(frag))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            Collections.sort(lines);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(frag))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        PriorityQueue<BufferedReader> pq = new PriorityQueue<>((a, b) -> {
            try {
                return Integer.compare(
                        Integer.parseInt(a.readLine().split("\t")[0]),
                        Integer.parseInt(b.readLine().split("\t")[0])
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        for (var frag : fragment) {
            pq.offer(new BufferedReader(new FileReader(frag)));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            while (!pq.isEmpty()) {
                BufferedReader reader = pq.poll();
                String line = reader.readLine();
                if (line != null) {
                    writer.write(line);
                    writer.newLine();
                    pq.offer(reader);
                }
            }
        }

        for (var frag : fragment) {
            frag.delete();
        }
    }

    private static List<File> splitFile(File inputFile, int framentSize) throws IOException {
        List<File> fragment = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                int fragmentNumber = lineNumber / framentSize;
                if (lineNumber % framentSize == 0) {
                    fragment.add(File.createTempFile("fragment", null));
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fragment.get(fragmentNumber), true))) {
                    writer.write(line);
                    writer.newLine();
                }
                lineNumber++;
            }
        }
        return fragment;
    }

    public static void main(String[] args) throws IOException {
        File inputFile = new File("testfile.csv");
        File outputFile = new File("testfile1.csv");
        sortCsv(inputFile, outputFile, 100000);
    }
}



