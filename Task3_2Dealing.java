import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class Task3_2Dealing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== File Search (Work Dealing) ===");

        // Input
        File directory = getValidDirectory(scanner);
        System.out.print("Enter letter or word to search in filenames: ");
        String searchTerm = scanner.next();

        // Collect all files to a list for splitting
        System.out.println("Scanning directory structure...");
        List<File> allFiles = new ArrayList<>();
        collectFiles(directory, allFiles);
        int totalFiles = allFiles.size();
        System.out.println("Files to process: " + totalFiles);

        // Execution setup
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Future<Long>> futures = new ArrayList<>();

        long start = System.nanoTime();

        // Static partitioning
        int chunkSize = (int) Math.ceil((double) totalFiles / cores);
        if (chunkSize == 0)
            chunkSize = 1; // Protect against empty dir

        for (int i = 0; i < totalFiles; i += chunkSize) {
            int end = Math.min(i + chunkSize, totalFiles);
            List<File> chunk = allFiles.subList(i, end);

            // Submit chunk task
            futures.add(executor.submit(() -> countInChunk(chunk, searchTerm)));
        }

        // Aggregate results
        long totalCount = 0;
        for (Future<Long> f : futures) {
            try {
                totalCount += f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long end = System.nanoTime();
        executor.shutdown();

        // Output
        System.out.println("Total files found: " + totalCount);
        System.out.printf("Time taken: %.3f ms%n", (end - start) / 1_000_000.0);
    }

    // Logic for processing a chunk of files
    private static long countInChunk(List<File> files, String term) {
        long count = 0;
        for (File file : files) {
            if (file.getName().contains(term)) {
                count++;
            }
        }
        return count;
    }

    // Recursive collection to flat list
    private static void collectFiles(File dir, List<File> list) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectFiles(file, list);
                } else {
                    list.add(file);
                }
            }
        }
    }

    // Validate Directory
    private static File getValidDirectory(Scanner s) {
        while (true) {
            System.out.print("Enter directory path: ");
            String path = s.next();
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                return file;
            }
            System.out.println("Invalid path or not a directory. Try again.");
        }
    }
}